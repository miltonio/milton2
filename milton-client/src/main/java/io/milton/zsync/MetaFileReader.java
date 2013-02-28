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

/* MetafileReader.java

MetafileReader: Metafile reader class
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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import io.milton.zsync.HeaderMaker.Headers;
import io.milton.zsync.MetaFileMaker.MetaData;
import java.io.FileNotFoundException;


/**
 * Class used to read metafile
 * @author Tomáš Hlavnička
 */
public class MetaFileReader {

	private ChainingHash hashtable;
	private int fileOffset;
	private int blockNum;
	
	/** Variables for header information from .zsync metafile */
	//------------------------------
	private Headers headers;


	public MetaFileReader(File metafile) {
		readMetaFile(metafile);
		blockNum = (int) Math.ceil((double) headers.length / (double) headers.blocksize);
		readChecksums(metafile) ;	
	}

	public MetaFileReader(MetaData metaData) {
		this.headers = metaData.getHeaders();		
		blockNum = (int) Math.ceil((double) headers.length / (double) headers.blocksize);
		fillHashTable(metaData.getChecksums());
		
	}

	/**
	 * Parsing method for metafile headers, saving each value into separate variable.
	 * @param s String containing metafile
	 * @return Boolean value notifying whether header ended or not (true = end of header)
	 */
	private boolean parseHeader(String s) {
		String subs;
		int colonIndex;
		if (s.equals("")) {
			//timto prazdnym radkem skoncil header, muzeme prestat cist
			return true;
		}
		colonIndex = s.indexOf(":");
		subs = s.substring(0, colonIndex);
		if (subs.equalsIgnoreCase("zsync")) {
			headers.version = s.substring(colonIndex + 2);
			//zkontrolujeme kompatibilitu
			if (headers.version.equals("0.0.4") || headers.version.equals("0.0.2")) {
				throw new RuntimeException("This version is not compatible with zsync streams in versions up to 0.0.4");
			}
		} else if (subs.equalsIgnoreCase("Blocksize")) {
			headers.blocksize = Integer.parseInt(s.substring(colonIndex + 2));
		} else if (subs.equalsIgnoreCase("Length")) {
			headers.length = Long.parseLong(s.substring(colonIndex + 2));
		} else if (subs.equalsIgnoreCase("Hash-Lengths")) {
			int comma = s.indexOf(",");
			int seqNum = Integer.parseInt(s.substring((colonIndex + 2), comma));
			headers.setSeqNum(seqNum);
			int nextComma = s.indexOf(",", comma + 1);
			headers.setRsumBytes( Integer.parseInt(s.substring(comma + 1, nextComma)) );
			headers.setChecksumBytes( Integer.parseInt(s.substring(nextComma + 1)) );
			if ((headers.getSeqNum() < 1 || headers.getSeqNum() > 2)
					|| (headers.getRsumButes() < 1 || headers.getRsumButes() > 4)
					|| (headers.getChecksumBytes() < 3 || headers.getChecksumBytes() > 16)) {
				throw new RuntimeException("Nonsensical hash lengths line " + s.substring(colonIndex + 2));
			}

		} else if (subs.equalsIgnoreCase("URL")) {
			headers.url = s.substring(colonIndex + 2);
		} else if (subs.equalsIgnoreCase("Z-URL")) {
			//not implemented yet
		} else if (subs.equalsIgnoreCase("SHA-1")) {
			headers.sha1 = s.substring(colonIndex + 2);
		} else if (subs.equalsIgnoreCase("Z-Map2")) {
			//not implemented yet
		}
		return false;
	}

	/**
	 * Method reads metafile from file and reads
	 * it line by line, sending line String to parser.
	 */
	private void readMetaFile(File metafile) {
		headers = new Headers(); 
		try {
			BufferedReader in = new BufferedReader(new FileReader(metafile));
			String s;
			while ((s = in.readLine()) != null) {
				if (parseHeader(s)) {
					break;
				}
			}
			in.close();
		} catch(FileNotFoundException e) {
			throw new RuntimeException("File not found: " + metafile.getAbsolutePath(), e);
		} catch (IOException e) {
			throw new RuntimeException("IO problem in metafile header reading", e);
		}
	}

	/**
	 * Method that reads metafile from file and stores its content into byte array
	 * and saves offset where headers end and blocksums starts.
	 */
	private void readChecksums(File metafile) {
		long length = metafile.length();
		if (metafile.length() > Integer.MAX_VALUE) {
			throw new RuntimeException("Metafile is too large");
		}
		byte[] bytes = new byte[(int) length];

		try {
			InputStream is = new FileInputStream(metafile);
			int offset = 0;
			int n = 0;
			while (offset < bytes.length && (n = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += n;
			}

			// Presvedcime se, ze jsme precetli cely soubor
			if (offset < bytes.length) {
				throw new IOException("Could not completely read file " + metafile.getName());
			} 

			is.close();
		} catch (IOException e) {
			throw new RuntimeException("IO problem in metafile reading", e);
		}
		// urci offset, kde konci hlavicka a zacinaji kontrolni soucty
		fileOffset = 0;
		for (int i = 2; i < bytes.length; i++) {
			if (bytes[i - 2] == 10 && bytes[i - 1] == 10) {
				fileOffset = i;
				break;
			}
		}
		fillHashTable(bytes);
	}

	private void fillHashTable(List<ChecksumPair> list) {
		int i = 16;
		//spocteme velikost hashtable podle poctu bloku dat
		while ((2 << (i - 1)) > blockNum && i > 4) {
			i--;
		}
		//vytvorime hashtable o velikosti 2^i (max. 2^16, min. 2^4)
		hashtable = new ChainingHash(2 << (i - 1));
		for( ChecksumPair pair : list ) {
			hashtable.insert(pair);
		}
	}
	
	/**
	 * Fills a chaining hash table with ChecksumPairs
	 * @param checksums Byte array with bytes of whole metafile
	 */
	private void fillHashTable(byte[] checksums) {
		int i = 16;
		//spocteme velikost hashtable podle poctu bloku dat
		while ((2 << (i - 1)) > blockNum && i > 4) {
			i--;
		}
		//vytvorime hashtable o velikosti 2^i (max. 2^16, min. 2^4)
		hashtable = new ChainingHash(2 << (i - 1));
		ChecksumPair p = null;
		//Link item;
		int offset = 0;
		int weakSum = 0;
		int seq = 0;
		int off = fileOffset;

		byte[] weak = new byte[4];
		byte[] strongSum = new byte[headers.getChecksumBytes()];

		while (seq < blockNum) {

			for (int w = 0; w < headers.getRsumButes(); w++) {
				weak[w] = checksums[off];
				off++;
			}

			for (int s = 0; s < strongSum.length; s++) {
				strongSum[s] = checksums[off];
				off++;
			}

			weakSum = 0;
			weakSum += (weak[2] & 0x000000FF) << 24;
			weakSum += (weak[3] & 0x000000FF) << 16;
			weakSum += (weak[0] & 0x000000FF) << 8;
			weakSum += (weak[1] & 0x000000FF);

			p = new ChecksumPair(weakSum, strongSum.clone(), offset, headers.blocksize, seq);
			offset += headers.blocksize;
			seq++;
			//item = new Link(p);
			hashtable.insert(p);
		}
	}

	/**
	 * Returns hash table cotaining block checksums
	 * @return Hash table
	 */
	public ChainingHash getHashtable() {
		return hashtable;
	}

	/**
	 * Returns number of blocks in complete file
	 * @return Number of blocks
	 */
	public int getBlockCount() {
		return blockNum;
	}

	/**
	 * Returns size of block
	 * @return Size of the data block
	 */
	public int getBlocksize() {
		return headers.blocksize;
	}

	/**
	 * Length of used strong sum
	 * @return Length of strong sum
	 */
	public int getChecksumBytes() {
		return headers.getChecksumBytes();
	}

	/**
	 * Returns length of complete file
	 * @return Length of the file
	 */
	public long getLength() {
		return headers.length;
	}


	/**
	 * Length of used weak sum
	 * @return Length of weak sum
	 */
	public int getRsumBytes() {
		return headers.getRsumButes();
	}

	/**
	 * Number of consequence blocks
	 * @return Number of consequence blocks
	 */
	public int getSeqNum() {
		return headers.getSeqNum();
	}

	/**
	 * Returns SHA1sum of complete file
	 * @return String containing SHA1 sum of complete file
	 */
	public String getSha1() {
		return headers.sha1;
	}
}