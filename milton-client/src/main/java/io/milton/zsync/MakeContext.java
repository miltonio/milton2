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

/**
 * Holds working variables used when applying deltas
 *
 * 
 */
public class MakeContext {
	final ChainingHash hashtable; 
	final long[] fileMap; 
	long fileOffset; 
	boolean rangeQueue;

	public MakeContext(ChainingHash hashtable, long[] fileMap) {
		this.hashtable = hashtable;
		this.fileMap = fileMap;
	}
	
	
	public void put(int blockIndex, long offset){
		
		fileMap[blockIndex] = offset;
	}
	
	public void delete(ChecksumPair key){
		
		hashtable.delete(key);
	}
	
	public boolean matched(int blockIndex) {
		
		return fileMap[blockIndex] > -1;
	}
	
	public void removematch(int blockIndex) {

		fileMap[blockIndex] = -1;
	}
	
	public int blockcount() {
		
		return fileMap.length;
	}
}
