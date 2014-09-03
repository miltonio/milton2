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

/* FileMaker.java

 FileMaker: File reading and making class
 Copyright (C) 2011 Tomáš Hlavnička <hlavntom@fel.cvut.cz>

 This file is a part of Jazsync.

 Jazsync is free software; you can redistribute it and/or modify it
 under the terms of the GNU General Public License as published by the
 Free Software Foundation; either version 2 of the License, or (at
 your option) any later version.

 Jazsync is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Jazsync; if not, write to the

 Free Software Foundation, Inc.,
 59 Temple Place, Suite 330,
 Boston, MA  02111-1307
 USA
 */
package io.milton.zsync;

import io.milton.httpclient.zsyncclient.RangeLoader;
import io.milton.http.Range;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad, original work by Tomáš Hlavnička
 */
public class FileUpdater {

    private static final Logger log = LoggerFactory.getLogger(FileUpdater.class);
    /**
     * Maximum ranges to download in the range header
     */
    private int maxRanges = 100;

    /**
     * Method for completing file
     */
    public void update(File inputFile, MetaFileReader mfr, RangeLoader rangeLoader, MakeContext mc, File newFile) throws Exception {
        log.trace("fileMaker: input: " + inputFile.getAbsolutePath());
        try {
            double a = 10;
            int range = 0;
            int blockLength = 0;
            List<Range> rangeList = new ArrayList<Range>();
            byte[] data = null;
            FileChannel wChannel = null;

            newFile.createNewFile();
            log.trace("Writing new file: " + newFile.getAbsolutePath());
            wChannel = new FileOutputStream(newFile, true).getChannel();

            ByteBuffer buffer = ByteBuffer.allocate(mfr.getBlocksize());
            log.trace("Reading from file: " + inputFile.getAbsolutePath());
            FileChannel rChannel = new FileInputStream(inputFile).getChannel();
            log.trace("number of map entries: " + mc.fileMap.length);
            for (int i = 0; i < mc.fileMap.length; i++) {
                mc.fileOffset = mc.fileMap[i];
                if (mc.fileOffset != -1) {
                    log.trace("  read block from local file: " + mc.fileOffset);
                    rChannel.read(buffer, mc.fileOffset);
                    buffer.flip();
                    wChannel.write(buffer);
                    buffer.clear();
                } else {
                    log.trace("   read block from remote file");
                    if (!mc.rangeQueue) {
                        rangeList = rangeLookUp(i, mfr.getBlocksize(), mc);
                        range = rangeList.size();
                        data = rangeLoader.get(rangeList);
                        //System.out.println("got data length: " + data.length);
                    } else {
                        log.trace("     already have queued ranges: " + rangeList.size());
                    }
                    blockLength = calcBlockLength(i, mfr.getBlocksize(), (int) mfr.getLength());
                    int offset = (range - rangeList.size()) * mfr.getBlocksize();
                    //System.out.println("blockLength: " + blockLength + " data.length: " + data.length + "  offset: " + offset);
                    buffer.put(data, offset, blockLength);
                    buffer.flip();
                    wChannel.write(buffer);
                    buffer.clear();
                    rangeList.remove(0);
                    if (rangeList.isEmpty()) {
                        mc.rangeQueue = false;
                    }
                }
            }
            log.info("Completed file: " + newFile.getAbsolutePath());
            log.info("Checking checksums...");
            SHA1 sha = new SHA1(newFile);
            String actual = sha.SHA1sum();
            String expected = mfr.getSha1();

            if (actual.equals(expected)) {
                log.info("checksum matches OK");
//				System.out.println("used " + (mfr.getLength() - (mfr.getBlocksize() * missing)) + " " + "local, fetched " + (mfr.getBlocksize() * missing));
//				new File(mfr.getFilename()).renameTo(new File(mfr.getFilename() + ".zs-old"));
//				newFile.renameTo(new File(mfr.getFilename()));
//				allData += mfr.getLengthOfMetafile();
//				System.out.println("really downloaded " + allData);
//				double overhead = ((double) (allData - (mfr.getBlocksize() * missing)) / ((double) (mfr.getBlocksize() * missing))) * 100;
//				System.out.println("overhead: " + df.format(overhead) + "%");
            } else {
                log.error("Checksums don't match - expected: " + expected + "  actual: " + actual);
                throw new RuntimeException("Checksums don't match - expected: " + expected + "  actual: " + actual);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Can't read or write, check your permissions.");
        }
    }

    /**
     * Instead of downloading single blocks, we can look into fieMap and collect
     * amount of missing blocks or end of map accurs. Single ranges are stored
     * in ArrayList
     *
     * @param i Offset in fileMap where to start looking
     * @return ArrayList with ranges for requesting
     */
    private List<Range> rangeLookUp(int i, int blocksize, MakeContext mc) {
        List<Range> ranges = new ArrayList<Range>();
        for (; i < mc.fileMap.length; i++) {
            if (mc.fileMap[i] == -1) {
                ranges.add(new Range((long) i * blocksize, (i * blocksize) + (long) blocksize));
            }
            if (ranges.size() >= maxRanges) {
                break;
            }
        }
        if (!ranges.isEmpty()) {
            mc.rangeQueue = true;
        }
        return ranges;
    }

    private int calcBlockLength(int i, int blockSize, int length) {
        if ((i + 1) * blockSize < length) {
            return blockSize;
        } else {
            return calcBlockLength_b(i, blockSize, length);
        }
    }

    private int calcBlockLength_b(int i, int blockSize, int length) {
        return blockSize + (length - (i * blockSize + blockSize));
    }
}
