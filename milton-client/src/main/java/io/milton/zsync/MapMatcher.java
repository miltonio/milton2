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
Copyright (C) 2011 TomÃ¡Å¡ HlavniÄ�ka <hlavntom@fel.cvut.cz>

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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad, original work by TomÃ¡Å¡ HlavniÄ�ka
 */
public class MapMatcher {
	
	private static final Logger log = LoggerFactory.getLogger(MapMatcher.class);
	
    private Generator gen = new Generator();	
	
    /**
     * Reads file and map it's data into the fileMap.
     */
    public double mapMatcher(File inputFile, MetaFileReader mfr, MakeContext mc) {
        int bufferOffset = 0;
		InputStream is = null;
		long fileLength = inputFile.length();
        try {
			is = new FileInputStream(inputFile);
			InputStream inBuf = new BufferedInputStream(is);
            Security.addProvider(new JarsyncProvider());
            Configuration config = new Configuration();
            config.strongSum = MessageDigest.getInstance("MD4");
            config.weakSum = new Rsum();
            config.blockLength = mfr.getBlocksize();
            config.strongSumLength = mfr.getChecksumBytes();
            int weakSum;
            byte[] strongSum;
            byte[] backBuffer = new byte[mfr.getBlocksize()];
            byte[] blockBuffer = new byte[mfr.getBlocksize()];
            byte[] fileBuffer;
            int mebiByte = 1048576;
            if (mfr.getLength() < mebiByte && mfr.getBlocksize() < mfr.getLength()) {
                fileBuffer = new byte[(int) mfr.getLength()];
            } else if (mfr.getBlocksize() > mfr.getLength() || mfr.getBlocksize() > mebiByte) {
                fileBuffer = new byte[mfr.getBlocksize()];
            } else {
                fileBuffer = new byte[mebiByte];
            }
            int n; // number of bytes read from input stream
            byte newByte;
            boolean firstBlock = true;
            int len = fileBuffer.length;
            boolean end = false;
            int blocksize = mfr.getBlocksize();
            
            //
            long lastMatch = 0;
            //
            
            while (mc.fileOffset != fileLength) {
				//System.out.println("Outer loop: " + mc.fileOffset);
                n = inBuf.read(fileBuffer, 0, len);
                if (firstBlock) {
                    weakSum = gen.generateWeakSum(fileBuffer, 0, config);
                    bufferOffset = mfr.getBlocksize();
                    int weak = updateWeakSum(weakSum, mfr);
                    if (hashLookUp(weak, null, blocksize, mc)) {
                        strongSum = gen.generateStrongSum(fileBuffer, 0, blocksize, config);
                        boolean match = hashLookUp(updateWeakSum(weakSum, mfr), strongSum, blocksize, mc);
                        if ( match ) {
							lastMatch = mc.fileOffset;
							//System.out.println("Last match: " + lastMatch);
						}
                    }
                    mc.fileOffset++;
                    firstBlock = false;
                }

                for (; bufferOffset < fileBuffer.length; bufferOffset++) {
                    newByte = fileBuffer[bufferOffset];
                    if (mc.fileOffset + mfr.getBlocksize() > fileLength) {
                        newByte = 0;
                    }
                    weakSum = gen.generateRollSum(newByte, config);
                	//System.out.println("Innner Loop: bufferOffset: " + bufferOffset + " - fileBuffer.length: " + fileBuffer.length + " weakSum: " + weakSum + " mc.fileOffset: " + mc.fileOffset + " - lastMatch: " + lastMatch);
					boolean found = false;
					if( mc.fileOffset >= lastMatch + blocksize ) {
						int wSum =  updateWeakSum(weakSum, mfr);
						if( hashLookUp(wSum, null, blocksize, mc) ) {
							found = true;
						} else {
							//System.out.println("Not found, weaksum: " + wSum);
						}
					} else {
						//System.out.println("Not looking for match because fileOffset not far enough: " + mc.fileOffset + " lastMatch: " + lastMatch + " blockSize: " + blocksize);
					}
                    if ( found ) {
                        if (mc.fileOffset + mfr.getBlocksize() > fileLength) {
                            if (n > 0) {
                                Arrays.fill(fileBuffer, n, fileBuffer.length, (byte) 0);
                            } else {
                                int offset = fileBuffer.length - mfr.getBlocksize() + bufferOffset + 1;
                                System.arraycopy(fileBuffer, offset, blockBuffer, 0, fileBuffer.length - offset);
                                Arrays.fill(blockBuffer, fileBuffer.length - offset, blockBuffer.length, (byte) 0);
                            }
                        }
                        if ((bufferOffset - mfr.getBlocksize() + 1) < 0) {
                            if (n > 0) {
                                System.arraycopy(backBuffer, backBuffer.length + bufferOffset - mfr.getBlocksize() + 1, blockBuffer, 0, mfr.getBlocksize() - bufferOffset - 1);
                                System.arraycopy(fileBuffer, 0, blockBuffer, mfr.getBlocksize() - bufferOffset - 1, bufferOffset + 1);
                            }
                            strongSum = gen.generateStrongSum(blockBuffer, 0, blocksize, config);
							//System.out.println("Look for match: " + new String(blockBuffer));
                            boolean match = hashLookUp(updateWeakSum(weakSum, mfr), strongSum, blocksize, mc);
                            if ( match ) lastMatch = mc.fileOffset;
                        } else {
                            strongSum = gen.generateStrongSum(fileBuffer, bufferOffset - blocksize + 1, blocksize, config);
                            boolean match = hashLookUp(updateWeakSum(weakSum, mfr), strongSum, blocksize, mc);
                            if ( match ) lastMatch = mc.fileOffset;
                        }
                    }
                    
                    mc.fileOffset++;
                    if (mc.fileOffset == fileLength) {
                        end = true;
                        break;
                    }
                }
                System.arraycopy(fileBuffer, fileBuffer.length - mfr.getBlocksize(), backBuffer, 0, mfr.getBlocksize());
                bufferOffset = 0;
                if (end) {
                    break;
                }
            }

            double complete = matchControl(mfr, mc);
            mc.removematch( mc.blockcount() - 1 );
            is.close();
            return complete;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        } finally {
			StreamUtils.close(is);
		}
    }
	

    /**
     * Shorten the calculated weakSum according to variable length of weaksum
     * @param weak Generated full weakSum
     * @return Shortened weakSum
     */
    private int updateWeakSum(int weak, MetaFileReader mfr) {
        byte[] rsum;
        switch (mfr.getRsumBytes()) {
            case 2:
                rsum = new byte[]{(byte) 0,
                    (byte) 0,
                    (byte) (weak >> 24), //1
                    (byte) ((weak << 8) >> 24) //2
                };
                break;
            case 3:
                rsum = new byte[]{(byte) ((weak << 8) >> 24), //2
                    (byte) 0, //3
                    (byte) ((weak << 24) >> 24), //0
                    (byte) (weak >> 24) //1
                };
                break;
            case 4:
                rsum = new byte[]{(byte) (weak >> 24), //1
                    (byte) ((weak << 8) >> 24), //2
                    (byte) ((weak << 16) >> 24), //3
                    (byte) ((weak << 24) >> 24) //0
                };
                break;
            default:
                rsum = new byte[4];
        }
        int weakSum = 0;
        weakSum += (rsum[0] & 0x000000FF) << 24;
        weakSum += (rsum[1] & 0x000000FF) << 16;
        weakSum += (rsum[2] & 0x000000FF) << 8;
        weakSum += (rsum[3] & 0x000000FF);
        return weakSum;
    }	
	

    /**
     * Looks into hash table and check if got a hit
	 * 
     * @param weakSum Weak rolling checksum
     * @param strongSum Strong MD4 checksum
     * @return True if we got a hit
     */
    private boolean hashLookUp(int weakSum, byte[] strongSum, int blocksize, MakeContext mc) {
		//System.out.println("hashLookup: " + weakSum);
        ChecksumPair p;
        if (strongSum == null) {
            p = new ChecksumPair(weakSum);
            ChecksumPair link = mc.hashtable.find(p);
            if (link != null) {
				//System.out.println(" found weak match link: " + link);
                return true;
            }
        } else {
            p = new ChecksumPair(weakSum, strongSum);
            ChecksumPair link = mc.hashtable.findMatch(p);
            int seq;
            if (link != null) {
                seq = link.getSequence();
				//System.out.println(" found matching block, block index: " + seq + " fileoffset: " + mc.fileOffset + " block size: " + blocksize);
                //mc.fileMap[seq] = mc.fileOffset;
                mc.put(seq, mc.fileOffset);
                //mc.hashtable.delete(new ChecksumPair(weakSum, strongSum, blocksize * seq, blocksize, seq));
                mc.delete(new ChecksumPair(weakSum, strongSum, blocksize * seq, blocksize, seq));
                return true;
            }
        }
		//System.out.println("No matching block: " + strongSum);
        return false;
    }	
	
	
    /**
     * Clears non-matching blocks and returns percentage
     * value of how complete is our file
     * @return How many percent of file we have already
     */
    private double matchControl(MetaFileReader mfr, MakeContext mc) {
        int missing = 0;
        int blockCount = mc.blockcount();
        //long[] fileMap = mc.fileMap;
        for (int i = 0; i < blockCount; i++) {
            if (mfr.getSeqNum() == 2) { //pouze pokud kontrolujeme matching continuation
                if (i > 0 && i < blockCount - 1) {
                    if (!mc.matched( i - 1 ) && !mc.matched( i + 1 )) {
                        mc.removematch( i );
                    }
                } else if (i == 0) {
                    if (!mc.matched( i + 1 )) {
                        mc.removematch( i );
                    }
                } else if (i == blockCount - 1) {
                    if (!mc.matched( i - 1 )) {
                    	mc.removematch( i );
                    }
                }
            }
            if (!mc.matched(i)) {
                missing++;
            }
        }
        log.trace("matchControl: fileMap.length: " + blockCount + " - missing: " + missing);
        return ((((double) blockCount - missing) / (double) blockCount) * 100);
    }
	
}
