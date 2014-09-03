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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;


import io.milton.http.Range;

/**
 * An object that performs the client-side operations needed to generate ZSync
 * PUT data.<p/>
 * In order to update a file on a server, the client first needs to download the
 * appropriate .zsync containing the metadata of the server file. The client
 * should then instantiate an
 * <code>UploadMaker</code>, passing to the constructor this .zsync file as well
 * as the local file to be uploaded. On construction, the
 * <code>UploadMaker</code> will determine the data ranges and assembly
 * instructions that need to be sent to the server, and will automatically fill
 * in an internal
 * <code>Upload</code> object. The client can then invoke the
 * <code>getInputStream</code> method, which will return a stream which should
 * be used as the body of a PUT request.<p/>
 *
 * E.g:
 * <p/>
 * <
 * code>
 *
 * UploadMaker um = new UploadMaker(File clientFile, File zsFile);<br/>
 * InputStream putData = um.getInputStream();
 * <p/>
 *
 * </code>
 *
 * Note: This is one of two classes that can be used to create a ZSync upload.
 * The other class,
 * <code>UploadMakerEx</code>, performs the same functions but may perform
 * better for certain rare cases.
 *
 *
 * @author Nick
 *
 * @see {@link Upload}, {@link UploadReader}, {@link UploadMakerEx}
 */
public class UploadMaker {

    /**
     * The local file that will replace the server file
     */
    public final File localCopy;
    /**
     * The .zsync of the server file to be replaced
     */
    public final File serversMetafile;
    private MetaFileReader metaFileReader;
    private MakeContext makeContext;
    private Upload upload;

    /**
     * Constructor that automatically creates and fills in an internal upload
     * object.
     *
     * @param sourceFile The local file to be uploaded
     * @param destMeta The zsync of the server's file
     * @throws IOException
     */
    public UploadMaker(File sourceFile, File destMeta) throws IOException {

        this.localCopy = sourceFile;
        this.serversMetafile = destMeta;
        this.upload = new Upload();
    }

    public InputStream makeUpload() throws IOException {

        this.initMetaData();

        try {

            System.out.print("Matching client and server blocks...");
            long t0 = System.currentTimeMillis();

            /* Rolling checksum procedure */
            MapMatcher matcher = new MapMatcher();
            matcher.mapMatcher(localCopy, metaFileReader, makeContext);
            long t1 = System.currentTimeMillis();

//			System.out.println( " " + ( t1 - t0 ) + " milliseconds" );
//			System.out.print( "Creating Upload..." );
            long t2 = System.currentTimeMillis();

            /* Computing upload and writing to BufferingOutputStreams */
            this.initUpload();
            long t3 = System.currentTimeMillis();

//			System.out.println(" " + ( t3 - t2 ) + " milliseconds");

            return upload.getInputStream();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void init() throws IOException {

        initMetaData();
        initUpload();
    }

    /**
     * Initializes MetaFileReader and MakeContext objects from the .zsync file
     * and finds and maps the matching blocks.
     *
     */
    private void initMetaData() {

        metaFileReader = new MetaFileReader(serversMetafile);
        makeContext = new MakeContext(metaFileReader.getHashtable(),
                new long[metaFileReader.getBlockCount()]);
        Arrays.fill(makeContext.fileMap, -1);

        //MapMatcher matcher = new MapMatcher();
        //matcher.mapMatcher( localCopy, metaFileReader, makeContext );

    }

    /**
     * Invokes the sequence of methods to generate the upload data and fill in
     * the internal Upload object.
     *
     * @throws IOException
     */
    private void initUpload() throws IOException {

        InputStream ranges = serversMissingRanges(makeContext.fileMap,
                localCopy, metaFileReader.getBlocksize());

        InputStream relocRanges = serversRelocationRanges(makeContext.fileMap,
                metaFileReader.getBlocksize(), localCopy.length(), true);

        upload.setVersion("testVersion");
        upload.setBlocksize(metaFileReader.getBlocksize());
        upload.setFilelength(localCopy.length());
        upload.setSha1(new SHA1(localCopy).SHA1sum());

        upload.setRelocStream(relocRanges);
        upload.setDataStream(ranges);
    }

    /**
     * Determines the byte ranges of new data that need to be sent to the server
     * to update its file.<p/>
     *
     * The
     * <code>fileMap</code> argument should be an array that maps matching
     * blocks from the server's file (the side that sent the metadata) to those
     * in the client file, such that
     * <code>fileMap[seq] == off</code> means that block number
     * <code>seq</code> in the server's file matches the block in the local file
     * beginning at byte
     * <code>off</code>. An invalid offset is ignored and should be used to
     * indicate that the local file contains no match for that block. The
     * <code>fileMap</code> array can be obtained from the MakeContext
     * class.<p/>
     *
     * @param fileMap An array mapping blocks in server file to their offsets in
     * local file
     * @param fileLength The length of the local file to be uploaded
     * @param blockSize The size of a block. Must correspond to block size used
     * in <code>fileMap</code>
     *
     * @return The List of byte Ranges that need to be sent
     * @throws IOException
     */
    public static InputStream serversMissingRanges(long[] fileMap,
            File local, int blockSize) throws IOException {

        /*
         * The ranges are determined by sorting the offset values in the fileMap array, 
         * i.e. sorting matching blocks according to their start byte in the local file. The method checks
         * the space between consecutive blocks, and if it is >= 0, adds that space to the list of ranges.
         */

        LinkedList<Long> localOffsets = new LinkedList<Long>(); // List of local matching block offsets
        //ArrayList<Range> rangeList = new ArrayList<Range>(); // output List
        ByteRangeWriter rangeList = new ByteRangeWriter(16384);
        RandomAccessFile randAccess = null;

        long fileLength = local.length();

        try {

            randAccess = new RandomAccessFile(local, "r");

            for (long offset : fileMap) {
                if (offset > -1 && offset < fileLength - blockSize) {
                    localOffsets.add(offset);
                }
            }

            localOffsets.add(fileLength); //Marks the end of the file
            Collections.sort(localOffsets); //Sort the blocks by their local offsets

            //Remove duplicate offsets
            Long prev = null;
            for (ListIterator<Long> iter = localOffsets.listIterator(); iter.hasNext();) {

                Long curr = iter.next();
                if (prev != null && curr.equals(prev)) {

                    iter.remove();
                } else {
                    prev = curr;
                }
            }

            /*Add the Range between the end of the previous block and the start of the 
             * current one, if that Range is > 0
             */
            long prevEnd = 0;
            for (Long offset : localOffsets) {
                if (offset - prevEnd > 0) {
                    rangeList.add(new Range(prevEnd, offset), randAccess);
                }
                prevEnd = offset + blockSize;
            }

        } finally {
            Util.close(randAccess);
        }

        return rangeList.getInputStream();
    }

    /**
     * Returns the assembly instructions needed by the server to relocate the
     * blocks it already has.
     * <p/>
     *
     * The
     * <code>combineRanges</code> argument determines whether contiguous
     * matching blocks should be combined into a single range, e.g. given a
     * blockSize of 100, whether 0-10/500, 10-20/600, 20-30/700 should be
     * combined into the single RelocateRange of 0-30/500.
     *
     * @param fileMap An array mapping blocks in the server file to their
     * matches in the local file
     * @param blockSize The block size used by fileMap
     * @param fileLength The length of the local file to be uploaded
     * @param combineRanges Whether consecutive matches should be combined into
     * a single RelocateRange
     * @return A list of RelocateRange instructions to be sent to the server
     * @throws IOException
     *
     */
    public static InputStream serversRelocationRanges(long[] fileMap,
            int blockSize, long fileLength, boolean combineRanges) throws IOException {

        //ArrayList<RelocateRange> ranges = new ArrayList<RelocateRange>();
        RelocWriter relocList = new RelocWriter(16384);

        for (int blockIndex = 0; blockIndex < fileMap.length; blockIndex++) {

            long localOffset = fileMap[blockIndex];
            if (localOffset >= 0 && localOffset != blockIndex * blockSize) {

                if (localOffset > fileLength - blockSize) {
                    //out of range
                    continue;
                }

                Range blockRange;
                if (combineRanges == true) {

                    //blockRange = null;
                    blockRange = consecMatches(fileMap, blockSize, blockIndex);
                    blockIndex += blockRange.getFinish() - blockRange.getStart() - 1;
                } else {

                    blockRange = new Range((long)blockIndex, blockIndex + 1l);
                }

                RelocateRange relocRange = new RelocateRange(blockRange, localOffset);
                relocList.add(relocRange);
            }
        }
        return relocList.getInputStream();
    }

    /**
     * Combines a sequence of contiguous matching blocks into a single Range
     *
     * @param fileMap The array mapping matching blocks, obtained from
     * MakeContext
     * @param blockSize The number of bytes in a block
     * @param blockIndex The index of the first block of the sequence
     * @return A Range beginning at blockIndex that is to be relocated as a
     * single chunk
     */
    private static Range consecMatches(long[] fileMap, int blockSize, int blockIndex) {

        int startBlock = blockIndex++;
        long currByte = fileMap[startBlock];

        for (; blockIndex < fileMap.length; blockIndex++) {

            if (fileMap[blockIndex] != currByte + blockSize) {

                break;
            }
            currByte += blockSize;
        }

        return new Range(startBlock, blockIndex);
    }

    /**
     * Returns the List of DataRange objects containing the portions of the
     * client file to be uploaded to the server. Currently unused.
     *
     * @param ranges The List of Ranges from the client file needed by the
     * server, which can be obtained from
     * {@link #serversMissingRanges(long[], long, int)}
     * @param local The client file to be uploaded
     * @return The List of DataRange objects containing client file portions to
     * be uploaded
     * @throws IOException
     */
    public static List<DataRange> getDataRanges(List<Range> ranges, File local) throws IOException {

        List<DataRange> dataRanges = new ArrayList<DataRange>();
        RandomAccessFile randAccess = new RandomAccessFile(local, "r");

        for (Range range : ranges) {

            dataRanges.add(new DataRange(range, randAccess));
        }

        return dataRanges;
    }

    /**
     * Returns the stream of bytes to be used as the body of a ZSync PUT.<p/>
     *
     * Note: Any temporary files used to store the data for the stream will be
     * deleted once the stream is closed, so a second invocation of this method
     * may not work.
     *
     * @return The InputStream containing the data for a ZSync PUT
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public InputStream getInputStream() throws UnsupportedEncodingException, IOException {

        return upload.getInputStream();
    }

    /**
     * Generates the relocStream portion of an Upload from a List of
     * RelocateRanges.
     *
     * @param relocList The List of RelocateRanges
     * @return An InputStream containing the relocStream portion of an Upload
     * @throws IOException
     */
    public static InputStream getRelocStream(List<RelocateRange> relocList) throws IOException {

        RelocWriter relocWriter = new RelocWriter(16384);
        for (RelocateRange reloc : relocList) {
            relocWriter.add(reloc);
        }
        return relocWriter.getInputStream();
    }

    /**
     * Generates the dataStream portion of an Upload from the local file and a
     * List of Ranges
     *
     * @param ranges The List of byte ranges
     * @param local The local file being uploaded
     * @return The InputStream containing the dataStream portion of an Upload
     * @throws IOException
     */
    public static InputStream getDataStream(List<Range> ranges, File local) throws IOException {

        ByteRangeWriter dataWriter = new ByteRangeWriter(16384);
        RandomAccessFile randAccess = null;

        try {

            randAccess = new RandomAccessFile(local, "r");
            for (Range range : ranges) {
                dataWriter.add(range, randAccess);
            }
            return dataWriter.getInputStream();
        } finally {
            Util.close(randAccess);
        }
    }
}