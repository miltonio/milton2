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

/* HeaderMaker.java

   HeaderMaker: Simple header-maker for metafiles
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
/**
 * Simple class for creating headers for metafile
 * @author Tomáš Hlavnička
 */
public class HeaderMaker {
/*
zsync: 0.6.1
Filename: tinycore.iso
MTime: Sat, 06 Mar 2010 09:33:36 +0000
Blocksize: 2048
Length: 11483136
Hash-Lengths: 2,2,5
URL: http://i.iinfo.cz/files/root/240/tinycore.iso
SHA-1: 5944ec77b9b0f2d6b8212d142970117f5801430a
*/

    /** Dodelat hlavicky tykajici se komprimovanych streamu
     * ++++ Z-URL, Z-Filename, Z-Map2, Recompress, Safe
     */
//    private SHA1 sha1;
//
//    private int seq_num=1;
//    private int rsum_bytes=4;
//    private int checksum_bytes=16;
//    private long mtime=0;

    public HeaderMaker(){

    }
	

    /**
     * Method builds header from key values
     * @return Full header in String format
     */
    public Headers getFullHeader(File file, String url, int blocksize, int[] hashLengths){
		Headers h = new Headers();	
        String sha1 = new SHA1(file.toString()).SHA1sum();		
		if( url == null ) {
			url = file.getName();					
		}
        if (!isPowerOfTwo(blocksize)) {
            throw new IllegalArgumentException("Blocksize must be a power of 2 (512, 1024, 2048, ...)");
        }
		
		init(h, file.lastModified(), file.length(), url, blocksize, hashLengths, sha1);
		return h;
    }	
	
	public Headers getFullHeader(Date lastMod, long fileLength, String url, int blocksize, int[] hashLengths, String sha1){
		Headers h = new Headers();		
		init(h, lastMod.getTime(), fileLength, url, blocksize, hashLengths, sha1);
		return h;		
	}
	
	private void init(Headers h, long lastMod, long fileLength, String url, int blocksize, int[] hashLengths, String sha1) {
        h.version = "jazsync";
        h.mTime = lastMod;
		h.url = url;
		h.length = fileLength;


        if (isPowerOfTwo(blocksize)) {
			h.blocksize = blocksize;			
        } else {
            throw new IllegalArgumentException("Blocksize must be a power of 2 (512, 1024, 2048, ...)");
        }

		h.setSeqNum(hashLengths[0]);
		h.setRsumBytes(hashLengths[1]);
		h.setChecksumBytes(hashLengths[2]);
        h.sha1 = sha1;
		
	}
	
	public String toString(Headers h) {
		String Version="zsync: jazsyncM";
		String MTime="MTime: ";
		String Blocksize="Blocksize: ";
		String Length="Length: ";
		String HashLengths="Hash-Lengths: ";
		String URL="URL: ";
		String SHA1="SHA-1: ";
		
		Blocksize+=h.blocksize;			
        MTime+=setMTime("EEE, dd MMM yyyy HH:mm:ss Z", h.mTime);
        Length+=h.length;
		URL+=h.url;                   //new url
        HashLengths+=(h.hashLengths[0]+","+h.hashLengths[1]+","+h.hashLengths[2]);
        SHA1+=h.sha1;
		
        StringBuilder sb = new StringBuilder("");
        sb.append(Version).append("\n");
        sb.append(MTime).append("\n");
        sb.append(Blocksize).append("\n");
        sb.append(Length).append("\n");
        sb.append(HashLengths).append("\n");
        sb.append(URL).append("\n");
        sb.append(SHA1).append("\n\n");
        String header = sb.toString();
        return header;
		
	}

    /**
     * Checks if <code>number</code> is power of two
     * @param number Number to be checked
     * @return Boolean value
     */
    private boolean isPowerOfTwo(int number){
        boolean isPowerOfTwo = true;
        while(number>1){
            if(number%2 != 0){
                isPowerOfTwo = false;
                break;
            } else {
                number=number/2;
            }
        }
        return isPowerOfTwo;
    }


    /**
     * Converts time (ms) into formated MTime using <code>dateFormat</code>
     * @param dateFormat MTime format
     * @return Formated date of MTime
     */
    private String setMTime(String dateFormat, long mtime) {
        Date date = new Date();
        date.setTime(mtime);
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat,Locale.US);
        return sdf.format(date);
    }
	
	public static class Headers {
        public String version;
        public long mTime; // lasst mod time in milliseconds
        public int blocksize;
        public long length;
        public final int[] hashLengths = new int[3];
        public String url;
        public String sha1;		
		
		public int getSeqNum() {
			return hashLengths[0];
		}
		public int getRsumButes() {
			return hashLengths[1];
		}
		public int getChecksumBytes() {
			return hashLengths[2];
		}		
		public void setSeqNum(int i) {
			hashLengths[0] = i;
		}
		public void setRsumBytes(int i) {
			hashLengths[1] = i;
		}
		public void setChecksumBytes(int i) {
			hashLengths[2] = i;
		}

		
	}

}