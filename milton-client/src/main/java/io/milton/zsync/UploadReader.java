/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.milton.zsync;

import io.milton.common.RangeUtils;
import io.milton.common.StreamUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;
import io.milton.http.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An object that performs the server side operations needed to assemble the
 * file from a ZSync PUT.
 * <p/>
 *
 * These operations consist of copying byte ranges into the new file. The
 * {@link #moveBlocks} method copies ranges from the previous file according to
 * a list of RelocateRanges, while the {@link #sendRanges} method copies the new
 * data received in the upload. Both of these methods are overloaded with
 * versions that accept File rather than byte[] arguments for dealing with large
 * files that should not be loaded into memory all at once.<p/>
 *
 * To assemble the file from a ZSync upload, the server should construct an
 * UploadReader, passing to the constructor the file to be updated and an
 * InputStream containing the upload data. It should then invoke the
 * {@link #assemble()} method, which will return a temporary file that was
 * created.
 *
 * @author Nick
 *
 */
public class UploadReader {

    private static final Logger log = LoggerFactory.getLogger(UploadReader.class);

    /**
     * Copies blocks of data from the in array to the out array.
     *
     * @param in The byte array containing the server's file being replaced
     * @param rlist The List of RelocateRanges received from the upload
     * @param blockSize The block size used in rlist
     * @param out The byte array of the file being assembled
     */
    public static void moveBlocks(byte[] in, List<RelocateRange> rlist, int blockSize, byte[] out) {

        for (RelocateRange reloc : rlist) {

            int startBlock = (int) reloc.getBlockRange().getStart().longValue();
            int finishBlock = (int) reloc.getBlockRange().getFinish().longValue();

            int startByte = startBlock * blockSize;
            int newOffset = (int) reloc.getOffset();
            int numBytes = (finishBlock - startBlock) * blockSize;

            System.arraycopy(in, startByte, out, newOffset, numBytes);

        }
    }

    /**
     * Copies blocks of data from the input File to the output File. For each
     * RelocateRange A-B/C in relocRanges, the block starting at A and ending at
     * B-1 is copied from inFile and written to byte C of outFile.
     *
     * @param inFile The server's File being replaced
     * @param relocRanges The Enumeration of RelocateRanges parsed from the
     * Upload's relocStream
     * @param blocksize The block size used in relocRanges
     * @param outFile The File being assembled
     * @throws IOException
     */
    public static void moveBlocks(File inFile, Enumeration<RelocateRange> relocRanges, int blocksize, File outFile) throws IOException {
        /*
         * Because transferFrom can supposedly throw Exceptions when copying large Files,
         * this method invokes moveRange to copy incrementally
         */

        /*The FileChannels should be obtained from a RandomAccessFile rather than a 
         *Stream, or the position() method will not work correctly
         */
        FileChannel rc = null;
        FileChannel wc = null;
        try {
            rc = new RandomAccessFile(inFile, "r").getChannel();
            wc = new RandomAccessFile(outFile, "rw").getChannel();

            while (relocRanges.hasMoreElements()) {

                moveRange(rc, relocRanges.nextElement(), blocksize, wc);
            }
        } finally {
            Util.close(rc);
            Util.close(wc);
        }
    }

    /**
     * Copies a Range of blocks from rc into a new offset of wc
     *
     * @param rc A FileChannel for the input File
     * @param reloc The RelocateRange specifying the Range to be copied and its
     * new offset
     * @param blockSize The block size used by reloc
     * @param wc The FileChannel for the output File
     * @throws IOException
     */
    private static void moveRange(FileChannel rc, RelocateRange reloc,
            int blockSize, FileChannel wc) throws IOException {

        long MAX_BUFFER = 16384;

        long startBlock = reloc.getBlockRange().getStart();
        long finishBlock = reloc.getBlockRange().getFinish();

        long bytesLeft = (finishBlock - startBlock) * blockSize; //bytes left to copy
        long readAtOnce = 0; //number of bytes to attempt to read
        long bytesRead = 0; //number of bytes actually read
        long currOffset = reloc.getOffset(); //current write position

        if (finishBlock * blockSize > rc.size() || startBlock < 0) {

            throw new RuntimeException("Invalid RelocateRange: Source file does not contain blocks "
                    + reloc.getBlockRange().getRange());
        }

        rc.position(startBlock * blockSize);
        while (bytesLeft > 0) {
            readAtOnce = Math.min(bytesLeft, MAX_BUFFER);

            /*Because transferFrom does not update the write channel's position, 
             * it needs to be set manually
             */
            bytesRead = wc.transferFrom(rc, currOffset, readAtOnce);
            bytesLeft -= bytesRead;
            currOffset += bytesRead;
        }

    }

    /**
     * Copies bytes from the in array into Ranges of the out array. The in array
     * is expected to contain the queued bytes in the same order as the ranges
     * List.
     *
     * @param in An array containing the queued bytes corresponding to the
     * ranges List
     * @param ranges The List of target Ranges
     * @param out The byte array for the file being assembled
     */
    public static void sendRanges(byte[] in, List<Range> ranges, byte[] out) {

        int pos = 0;
        for (Range r : ranges) {

            int length = (int) (r.getFinish() - r.getStart());
            System.arraycopy(in, pos, out, r.getStart().intValue(), length);
            pos += length;
        }
    }

    /**
     * Inserts the data from each DataRange into the output File, at the
     * appropriate offset
     *
     * @param byteRanges The Enumeration of Range/InputStream pairs parsed from
     * the Upload's dataStream
     * @param outFile The output File being assembled
     * @throws IOException
     */
    public static void sendRanges(Enumeration<ByteRange> byteRanges, File outFile)
            throws IOException {

        int BUFFER_SIZE = 16384;
        byte[] buffer = new byte[BUFFER_SIZE];

        RandomAccessFile randAccess = null;
        try {

            randAccess = new RandomAccessFile(outFile, "rw");
            while (byteRanges.hasMoreElements()) {

                ByteRange byteRange = byteRanges.nextElement();
                Range range = byteRange.getRange();
                InputStream data = byteRange.getDataQueue();

                sendBytes(data, range, buffer, randAccess);
            }
        } finally {
            Util.close(randAccess);
        }
    }

    /**
     * Reads a number of bytes from the InputStream equal to the size of the
     * specified Range and writes them into that Range of the RandomAccessFile.
     *
     * @param dataIn The InputStream containing the data to be copied
     * @param range The target location in the RandomAccessFile
     * @param buffer A byte array used to transfer data from dataIn to fileOut
     * @param fileOut A RandomAccessFile for the File being assembled
     * @throws IOException
     */
    private static void sendBytes(InputStream dataIn, Range range, byte[] buffer,
            RandomAccessFile fileOut) throws IOException {

        long bytesLeft = (range.getFinish() - range.getStart());
        int bytesRead = 0;
        int readAtOnce = 0;

        fileOut.seek(range.getStart());

        while (bytesLeft > 0) {

            readAtOnce = (int) Math.min(buffer.length, bytesLeft);
            bytesRead = dataIn.read(buffer, 0, readAtOnce);
            fileOut.write(buffer, 0, bytesRead);
            bytesLeft -= bytesRead;

            if (bytesLeft > 0 && bytesRead < 0) {

                throw new RuntimeException("Unable to copy byte Range: " + range.getRange()
                        + ". End of InputStream reached with " + bytesLeft + " bytes left.");
            }
        }
    }

    /**
     * Copies the contents of the source file to the destination file and sets
     * the destination file's length.
     *
     * @param inFile The source file
     * @param outFile The destination file
     * @param length The desired length of the destination file
     * @throws IOException
     */
    private static void copyFile(File inFile, File outFile, long length) throws IOException {

        InputStream fIn = null;
        OutputStream fOut = null;
        RandomAccessFile randAccess = null;

        try {

            fIn = new FileInputStream(inFile);
            fOut = new FileOutputStream(outFile);
            RangeUtils.sendBytes(fIn, fOut, inFile.length());
        } finally {
            StreamUtils.close(fIn);
            StreamUtils.close(fOut);
        }

        try {

            randAccess = new RandomAccessFile(outFile, "rw");
            randAccess.setLength(length);
        } finally {
            Util.close(randAccess);
        }
    }
    private File serverCopy;
    private File uploadedCopy;
    private Upload uploadData;

    /**
     * Constructor that parses the InputStream into an Upload object and
     * initializes a temporary file that will contain the assembled upload
     *
     * @param serverFile The server file to be updated
     * @param uploadIn A stream containing the ZSync PUT data
     * @throws IOException
     */
    public UploadReader(File serverFile, InputStream uploadIn) throws IOException {

        this.serverCopy = serverFile;
        this.uploadData = Upload.parse(uploadIn);
        this.uploadedCopy = File.createTempFile("zsync-upload", "newFile");
    }

    /**
     * Invokes the methods to put together the uploaded file.
     *
     * @return The assembled File
     * @throws IOException
     */
    public File assemble() throws IOException {

        if (uploadData.getBlocksize() <= 0) {
            throw new RuntimeException("Invalid blocksize specified: " + uploadData.getBlocksize());
        }

        if (uploadData.getFilelength() <= 0) {
            throw new RuntimeException("Invalid file length specified: " + uploadData.getFilelength());
        }

        if (StringUtils.isBlank(uploadData.getSha1())) {
            throw new RuntimeException("No SHA1 checksum provided.");
        }

        InputStream relocIn = null;
        InputStream dataIn = null;
        try {
            relocIn = uploadData.getRelocStream();
            dataIn = uploadData.getDataStream();

            Enumeration<RelocateRange> relocEnum = new RelocateParser(relocIn);
            Enumeration<ByteRange> dataEnum = new ByteRangeParser(dataIn);

            copyFile(serverCopy, uploadedCopy, uploadData.getFilelength());

            moveBlocks(serverCopy, relocEnum, (int) uploadData.getBlocksize(), uploadedCopy);
            sendRanges(dataEnum, uploadedCopy);
        } finally {
            StreamUtils.close(relocIn);
            StreamUtils.close(dataIn);
        }

        return uploadedCopy;
    }

    /**
     * Returns the expected SHA1 checksum String received in the upload
     *
     * @return A SHA1 checksum
     */
    public String getChecksum() {

        return uploadData.getSha1();
    }

    /**
     * An object that wraps the relocate stream of Upload (
     * {@link Upload#getRelocStream} )in an Enumeration of RelocateRanges. The
     * relocate stream is expected to contain a comma separated list of
     * RelocateRanges, e.g.<p/>
     *
     * 10-20/123, 100-200/789
     * <p/>
     *
     * A few whitespaces at the beginning or end of the list are ignored, as are
     * those surrounding the commas.
     *
     * @author Nick
     *
     */
    private static class RelocateParser implements Enumeration<RelocateRange> {

        private InputStream relocIn;
        private String nextToken;
        private byte[] COMMA = new byte[1];

        /**
         * Constructs the Enumeration of RelocateRanges from an InputStream
         *
         * @param relocIn An InputStream obtained from
         * {@link Upload#getRelocStream()}
         */
        public RelocateParser(InputStream relocIn) {
            try {
                this.relocIn = relocIn;
                this.COMMA[0] = ",".getBytes(Upload.CHARSET)[0];
                this.nextToken = Upload.readToken(relocIn, COMMA, 64);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public boolean hasMoreElements() {
            return !StringUtils.isBlank(nextToken);
        }

        @Override
        public RelocateRange nextElement() {

            if (!this.hasMoreElements()) {
                throw new NoSuchElementException("No more RelocateRanges");
            }
            try {

                RelocateRange reloc = RelocateRange.parse(nextToken);
                nextToken = Upload.readToken(relocIn, COMMA, 64);
                return reloc;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * An object that wraps the data stream portion of an Upload in an
     * Enumeration of ByteRanges. </p>
     *
     * In order for the parsing to work, the proper number of bytes must be read
     * from each ByteRange returned by {@link #nextElement()} prior to the next
     * invocation of {@link #hasMoreElements()}.
     *
     * @author Nick
     *
     */
    private static class ByteRangeParser implements Enumeration<ByteRange> {

        /*The dataStream portion of an Upload*/
        private InputStream dataQueue;
        /*The Range of the next ByteRange. A null value means that the next Range has not 
         *been loaded or that the end of the data section has been reached.
         */
        private Range nextRange;
        /*Whether an attempt has been made to read the next Range KV pair*/
        private boolean rangeloaded;
        private byte[] COLON = {":".getBytes(Upload.CHARSET)[0]};

        /**
         * Constructs the Enumeration from the specified InputStream
         *
         * @param in The InputStream obtained from
         * {@link Upload#getDataStream()}
         * @throws UnsupportedEncodingException
         */
        public ByteRangeParser(InputStream in) throws UnsupportedEncodingException {
            this.dataQueue = in;
            this.rangeloaded = false;
        }

        @Override
        public boolean hasMoreElements() {
            /*
             * If rangeloaded == false, attempt to read the next Range KV pair and set rangeloaded = true.
             * If rangeloaded == true and nextRange == null, there are no further ByteRanges.
             * 
             */
            try {

                if (rangeloaded) {
                    return nextRange != null;
                }

                String nextKey = Upload.readToken(dataQueue, COLON, 64).trim();
                if (StringUtils.isBlank(nextKey)) {
                    nextRange = null;
                } else if (!nextKey.equalsIgnoreCase(Upload.RANGE)) {
                    throw new RuntimeException("Invalid key. Expected: " + Upload.RANGE
                            + "\tActual: " + nextKey);
                } else {
                    nextRange = Range.parse(Upload.readValue(dataQueue, 64).trim());
                }

                rangeloaded = true;
                return nextRange != null;

            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public ByteRange nextElement() {

            if (!hasMoreElements()) {
                throw new NoSuchElementException("No more ByteRanges");
            }

            this.rangeloaded = false; //Reset rangeloaded
            return new ByteRange(nextRange, dataQueue);
        }
    }
}
