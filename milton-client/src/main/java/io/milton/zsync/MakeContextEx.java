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

import java.util.LinkedList;
import java.util.List;


/**
 * An extension of MakeContext that stores block matches in a way more suitable for upload.<p/>
 * 
 * This object differs from a MakeContext in that it allows multiple local blocks to be matched to a single remote block.
 * It does this by overriding the <code>put</code> method to save matches to an array of Lists, rather than 
 * to <code>fileMap</code>, which only allows one entry per remote block. It also overrides the <code>delete</code> 
 * method to do nothing, instead of removing the ChecksumPair from the ChainingHash.<p/>
 * 
 * MakeContextEx is used internally by UploadMakerEx in place of MakeContext as an argument to MapMatcher.mapMatcher.<p/>
 * 
 * @see UploadMakerEx
 * @author Nick
 *
 */
public class MakeContextEx extends MakeContext {
	
	/* An array storing a List of matches for each remote block */
	private List<OffsetPair>[] matchMap;
	/* The minimum local offset of the next match */
	private long nextOffset;
	private int blocksize;
	
	/**
	 * Constructs a MakeContextEx from an already-initialized ChainingHash.
	 * 
	 * @param hashtable The hashtable obtained from a MetaFileReader
	 * @param blockcount The number of blocks in the remote file
	 * @param blocksize The number of bytes per block
	 */
	public MakeContextEx(ChainingHash hashtable, int blockcount, int blocksize) {
		super(hashtable, null);

		this.matchMap = new LinkedList[blockcount];
		this.blocksize = blocksize;
		this.nextOffset = 0;
	}
	
	/**
	 * Returns a list of OffsetPairs representing the block-matches between the local and
	 * remote file.
	 * 
	 * @return The list of block matches
	 * @see OffsetPair
	 */
	public List<OffsetPair> getReverseMap() {
		/*
		 * Concatenates the Lists of OffsetPairs in matchMap into a single List
		 */
		List<OffsetPair> reverseMap = new LinkedList<OffsetPair>();
		
		for ( int blockIndex = 0; blockIndex < matchMap.length; blockIndex++ ) {
			
			if ( matchMap[blockIndex] != null ) {
				
				reverseMap.addAll( matchMap[blockIndex] );
			}
		}
		return reverseMap;
	}
	
	/**
	 * Adds a match to the list of block matches. Matching of overlapping client-side blocks
	 * is prevented.
	 * 
	 * @param blockIndex Index of the remote block
	 * @param offset Byte offset of the local block
	 */
	@Override
	public void put(int blockIndex, long offset){
		
		/* Currently, a match to the last block of the remote file is not permitted */
		if (blockIndex >= matchMap.length - 1){
			
			return;
		}
		
		/*
		 * Prevents matching a local block that overlaps the previous one, in case MapMatcher.mapMatcher
		 * does not already prevent this.
		 */
		if (offset < nextOffset){
			
			return;
		}
		
		if ( matchMap[blockIndex] == null ) {
			
			matchMap[blockIndex] = new LinkedList<OffsetPair>();
		} 
		matchMap[blockIndex].add( new OffsetPair(offset, blockIndex) );
		nextOffset = offset +  blocksize; //Set nextOffset to past the end of the previous match
	}
	
	/**
	 * Overrides <code>delete</code> in MakeContext to do nothing. Local blocks
	 * with the identical checksums will all then match to the first matching ChecksumPair in ChainingHash
	 * 
	 * @param key Unused in this override. Can be null
	 */
	@Override
	public void delete(ChecksumPair key){
		
		return;
	}

	@Override
	public boolean matched(int blockIndex) {
		
		if (blockIndex > 0 && blockIndex < this.blockcount()) {
			
			return matchMap[blockIndex] != null;
		}
		
		return false;
	}
	
	@Override
	public void removematch(int blockIndex) {
		
		if (blockIndex > 0 && blockIndex < this.blockcount()) {
			
			matchMap[blockIndex] = null;
		}
	}
	
	@Override
	public int blockcount() {
		
		return matchMap.length;
	}
	

}
