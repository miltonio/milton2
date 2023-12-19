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

/* MetaFileMaker.java

MetaFileMaker: Metafile making class (jazsyncmake)
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

import io.milton.common.StreamUtils;
import io.milton.zsync.HeaderMaker.Headers;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Date;
import java.util.List;

/**
 * Metafile making class
 *
 * @author Tomáš Hlavnička
 */
public class MetaFileMaker {

    static {
        Security.addProvider(new JarsyncProvider());
    }

    /**
     * Default length of strong checksum (MD4)
     */
    private final int STRONG_SUM_LENGTH = 16;
    private final HeaderMaker headerMaker = new HeaderMaker();
    private final Generator gen = new Generator();

    public MetaFileMaker() {
    }

    public MetaData make(String url, int blocksize, long fileLength, Date lastMod, InputStream fileData) {

        int[] hashLengths = analyzeFile(blocksize, fileLength);

        HeaderMaker.Headers headers = headerMaker.getFullHeader(lastMod, fileLength, url, blocksize, hashLengths, null);


        //appending block checksums into the metafile
        try {
            Configuration config = new Configuration();
            config.strongSum = MessageDigest.getInstance("MD4");
            config.weakSum = new Rsum();
            config.blockLength = blocksize;
            config.strongSumLength = hashLengths[2];
            List<ChecksumPair> list;
            MessageDigest sha1Digest = MessageDigest.getInstance("SHA1");
            list = gen.generateSums(fileData, config, sha1Digest);
            headers.sha1 = SHA1.toString(sha1Digest);
            return new MetaData(headers, list);
        } catch (IOException | NoSuchAlgorithmException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public File make(String url, int blocksize, File file) throws FileNotFoundException {
        MetaData metaData;
        try (FileInputStream fin = new FileInputStream(file)){
            metaData = make(url, blocksize, file.length(), new Date(file.lastModified()), fin);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(file.getAbsolutePath(), e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //File outputFile = new File(file.getName() + ".zsynch");
        File outputFile;
        try {
            outputFile = File.createTempFile(file.getName(), ".zsync");
        } catch (IOException ex) {
            throw new RuntimeException("Failed to create temp file. Please check permissions on the temporary files folder");
        }
        FileOutputStream fos = new FileOutputStream(outputFile);
        try {
            String header = headerMaker.toString(metaData.getHeaders());
            try {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fos));
                header.replaceAll("\n", System.getProperty("line.separator"));
                out.write(header);
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException("Can't create .zsync metafile, check your permissions");
            }

            //appending block checksums into the metafile
            try {
                List<ChecksumPair> list = metaData.getChecksums();
                for (ChecksumPair p : list) {
                    int rsum_bytes = metaData.headers.hashLengths[1];
                    fos.write(intToBytes(p.getWeak(), rsum_bytes));
                    fos.write(p.getStrong());
                }
                return outputFile;
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        } finally {
            StreamUtils.close(fos);
        }

    }

    public void write(MetaData metaData, OutputStream out) {
        String header = headerMaker.toString(metaData.getHeaders());
        try {
            OutputStreamWriter osw = new OutputStreamWriter(out);
            BufferedWriter writer = new BufferedWriter(osw);
            writer.write(header);
            writer.flush();
            osw.flush();
        } catch (IOException e) {
            throw new RuntimeException("Couldnt write zsync headers", e);
        }

        try {
            List<ChecksumPair> list = metaData.getChecksums();
            for (ChecksumPair p : list) {
                int rsum_bytes = metaData.headers.hashLengths[1];
                out.write(intToBytes(p.getWeak(), rsum_bytes));
                out.write(p.getStrong());
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * File analysis, computing lengths of weak and strong checksums and
     * sequence matches, storing the values into the array for easier handle
     * Hash-lengths and number of sequence matches
     * index 0 - seq_matches
     * index 1 - weakSum length
     * index 2 - strongSum length
     */
    private int[] analyzeFile(int blocksize, long fileLength) {
        int[] hashLengths = new int[3];
        hashLengths[2] = STRONG_SUM_LENGTH;
        hashLengths[0] = fileLength > blocksize ? 2 : 1;
        hashLengths[1] = (int) Math.ceil(((Math.log(fileLength)
                + Math.log(blocksize)) / Math.log(2) - 8.6) / 8);

        if (hashLengths[1] > 4) {
            hashLengths[1] = 4;
        }
        if (hashLengths[1] < 2) {
            hashLengths[1] = 2;
        }
        hashLengths[2] = (int) Math.ceil(
                (20 + (Math.log(fileLength) + Math.log(1 + fileLength / blocksize)) / Math.log(2))
                        / hashLengths[0] / 8);

        int strongSumLength2 =
                (int) ((7.9 + (20 + Math.log(1 + fileLength / blocksize) / Math.log(2))) / 8);
        if (hashLengths[2] < strongSumLength2) {
            hashLengths[2] = strongSumLength2;
        }
        return hashLengths;
    }

    /**
     * Converting integer weakSum into byte array that zsync can read
     * (htons byte order)
     *
     * @param number weakSum in integer form
     * @return converted to byte array compatible with zsync (htons byte order)
     */
    private byte[] intToBytes(int number, int rsum_bytes) {
        byte[] rsum = new byte[rsum_bytes];
        switch (rsum_bytes) {
            case 2:
                rsum = new byte[]{(byte) (number >> 24), //[0]
                        (byte) ((number << 8) >> 24)}; //[1]
                break;
            case 3:
                rsum = new byte[]{(byte) ((number << 24) >> 24), //[2]
                        (byte) (number >> 24), //[0]
                        (byte) ((number << 8) >> 24)}; //[1]
                break;
            case 4:
                rsum = new byte[]{(byte) ((number << 16) >> 24), //[2]
                        (byte) ((number << 24) >> 24), //[3]
                        (byte) (number >> 24), //[0]
                        (byte) ((number << 8) >> 24)}; //[1]
                break;
        }
        return rsum;
    }

    /**
     * Calculates optimal blocksize for a file
     */
    public int computeBlockSize(long fileLength) {
        int blocksize = 512;
        int[][] array = new int[10][2];
        array[0][0] = 2048;
        array[0][1] = 2048;
        for (int i = 1; i < array.length; i++) {
            array[i][0] = array[i - 1][0] * 2;
            array[i][1] = array[i][0];
        }
        //zarucime, ze se soubor rozdeli priblize na 50000 bloku
        long constant = fileLength / 50000;
        for (int[] item : array) {
            item[0] = (int) Math.abs(item[0] - constant);
        }
        int min = array[0][0];
        for (int[] item : array) {
            if (item[0] < min) {
                min = item[0];
            }
        }
        for (int[] item : array) {
            if (item[0] == min) {
                blocksize = item[1];
            }
        }
        return blocksize;
    }

    public static class MetaData {

        private final Headers headers;
        private final List<ChecksumPair> list;

        public MetaData(Headers headers, List<ChecksumPair> list) {
            this.headers = headers;
            this.list = list;
        }

        public Headers getHeaders() {
            return headers;
        }

        public List<ChecksumPair> getChecksums() {
            return list;
        }
    }
}
