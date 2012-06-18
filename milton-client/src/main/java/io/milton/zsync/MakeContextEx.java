/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
