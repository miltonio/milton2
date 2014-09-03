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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.List;

//import jazsync.jazsync.Rsum;
//
//import org.jarsync.ChecksumPair;
//import org.jarsync.Configuration;
//import org.jarsync.Generator;

/**
 *
 * @author brad
 */
public class ZSyncService {
	
//	private final Generator generator;
//
//	public ZSyncService(Generator generator) {
//		this.generator = generator;
//	}
//		
//	
//	public void write(OutputStream outChecksums, int blocksize, long fileLength, InputStream inSourceData) {
//
//		int[] hashLengths = new int[3];
//		
//		/**
//		 * zde provedeme analyzu souboru a podle toho urcime velikost hash length
//		 * a pocet navazujicich bloku
//		 */
//		analyzeFile(blocksize, hashLengths, fileLength);
//
//		//appending block checksums into the metafile
//		try {
//			Configuration config = new Configuration();
//			config.strongSum = MessageDigest.getInstance("MD4");
//			config.weakSum = new Rsum();
//			config.blockLength = blocksize;
//			config.strongSumLength = hashLengths[2];
//			Generator gen = new Generator(config);
//			List<ChecksumPair> list = new ArrayList<ChecksumPair>((int) Math.ceil((double) fileLength / (double) blocksize));
//			list = gen.generateSums(fileLength, inSourceData);
//			for (ChecksumPair p : list) {
//				System.out.println("checksum: seq:" + p.getSequence() + " - length" + p.getLength() + " - weak:" + p.getWeakHex() + " - strong:" + p.getStrongHex());
//				outChecksums.write(intToBytes(p.getWeak(), hashLengths[1]));
//				outChecksums.write(p.getStrong());
//			}
//		} catch (IOException ioe) {
//			throw new RuntimeException(ioe);
//		} catch (NoSuchAlgorithmException nae) {
//			System.out.println("Problem with MD4 checksum");
//			throw new RuntimeException(nae);
//		}
//		
//	}
//
//	/**
//	 * File analysis, computing lengths of weak and strong checksums and 
//	 * sequence matches, storing the values into the array for easier handle
//	 */
//	private void analyzeFile(int blocksize, int[] hashLengths, long fileLength) {
//		hashLengths[0] = fileLength > blocksize ? 2 : 1;
//		hashLengths[1] = (int) Math.ceil(((Math.log(fileLength)
//				+ Math.log(blocksize)) / Math.log(2) - 8.6) / 8);
//
//		if (hashLengths[1] > 4) {
//			hashLengths[1] = 4;
//		}
//		if (hashLengths[1] < 2) {
//			hashLengths[1] = 2;
//		}
//		hashLengths[2] = (int) Math.ceil(
//				(20 + (Math.log(fileLength) + Math.log(1 + fileLength / blocksize)) / Math.log(2))
//				/ hashLengths[0] / 8);
//
//		int strongSumLength2 =
//				(int) ((7.9 + (20 + Math.log(1 + fileLength / blocksize) / Math.log(2))) / 8);
//		if (hashLengths[2] < strongSumLength2) {
//			hashLengths[2] = strongSumLength2;
//		}
//	}
//
//	/**
//	 * Converting integer weakSum into byte array that zsync can read
//	 * (htons byte order)
//	 * @param number weakSum in integer form
//	 * @return converted to byte array compatible with zsync (htons byte order)
//	 */
//	private byte[] intToBytes(int number, int rsum_bytes) {
//		byte[] rsum = new byte[rsum_bytes];
//		switch (rsum_bytes) {
//			case 2:
//				rsum = new byte[]{(byte) (number >> 24), //[0]
//					(byte) ((number << 8) >> 24)}; //[1]
//				break;
//			case 3:
//				rsum = new byte[]{(byte) ((number << 24) >> 24), //[2]
//					(byte) (number >> 24), //[0]
//					(byte) ((number << 8) >> 24)}; //[1]
//				break;
//			case 4:
//				rsum = new byte[]{(byte) ((number << 16) >> 24), //[2]
//					(byte) ((number << 24) >> 24), //[3]
//					(byte) (number >> 24), //[0]
//					(byte) ((number << 8) >> 24)}; //[1]
//				break;
//		}
//		return rsum;
//	}
//	
//	
//	/**
//	 * Calculates optimal blocksize for a file
//	 */
//	public  int computeBlockSize(long fileLength) {
//		int[][] array = new int[10][2];
//		array[0][0] = 2048;
//		array[0][1] = 2048;
//		for (int i = 1; i < array.length; i++) {
//			array[i][0] = array[i - 1][0] * 2;
//			array[i][1] = array[i][0];
//		}
//		//zarucime, ze se soubor rozdeli priblize na 50000 bloku
//		long constant = fileLength / 50000;
//		for (int i = 0; i < array.length; i++) {
//			array[i][0] = (int) Math.abs(array[i][0] - constant);
//		}
//		int min = array[0][0];
//		for (int i = 0; i < array.length; i++) {
//			if (array[i][0] < min) {
//				min = array[i][0];
//			}
//		}
//                int bs = 300;
//		for (int i = 0; i < array.length; i++) {
//			if (array[i][0] == min) {
//				bs = array[i][1];
//			}
//		}
//                return bs;
//	}	
}
