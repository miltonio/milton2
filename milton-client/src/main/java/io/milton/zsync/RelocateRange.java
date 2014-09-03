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

import java.text.ParseException;

import io.milton.http.Range;

/**
*An object consisting of a <code>Range</code> and a <code>long</code> offset.<p/>
*
*A <code>RelocateRange</code> is an instruction sent as part of a ZSync PUT upload, 
*which informs the server of the new offset of a single range of blocks from the original file. The String 
*format for this object as it appears in the upload is currently "A-B/C", where A-B
*indicates the block range starting at A and ending at B-1, and C represents the location
*of this sequence of blocks in the updated (client-side) file.
 * 
 * @author Nick 
 * 
 */
public final class RelocateRange {
	
	/**
	 * The String separating the Range from the offset in the String representation 
	 * of this object.
	 */
	public final static String DIV = "/";

	private final Range blockRange;
	private final long newOffset;
	
	/**
	 * Constructs a RelocateRange from a Range and offset. The object is immutable; the fields
	 * cannot be changed after construction.
	 * 
	 * @param range A range of block indices in the remote file
	 * @param offset The new byte position of <b>range</b>
	 */
	public RelocateRange(Range range, long offset){
		this.blockRange = range;
		this.newOffset = offset;
	}


	/**
	 * Returns a String description of this object, not meant to be inserted into a ZSync PUT.
	 * For the proper PUT formatting, use {@link #getRelocation()}
	 * 
	 * @return A brief description of this object
	 * 
	 */
	@Override
	public String toString(){
		return "Relocate blocks " + blockRange.getRange() + " to " + newOffset;
	}
	
	/**
	 * Returns a String representing this object, e.g. "10-20/1234",
	 * ready to be inserted into a ZSync PUT upload request.
	 * 
	 * @return A String representing this object
	 */
	public String getRelocation(){
		return blockRange.getRange() + DIV + newOffset;
	}
	
	/**
	 * Constructs and returns a RelocateRange object from the input String. The String
	 * should be in the format A-B/C. <p/>
	 * 
	 * Note: Leading and trailing whitespaces in the argument are ignored, but there should be no 
	 * spaces between the non-ws chars.
	 * 
	 * @param relocString The String to be parsed
	 * @return The RelocateRange object corresponding to <b>relocString</b>  
	 * @throws ParseException If the format of the input String is incorrect or the numbers cannot 
	 * be properly parsed
	 * 
	 */
	public static RelocateRange parse(String relocString) throws ParseException{
		
		String[] parts = relocString.split(DIV);
		
		if (parts.length != 2){
			
			throw new ParseException("Couldn't parse: \"" + relocString 
					+ "\" as RelocateRange. String contains " + (parts.length - 1) 
					+ " occurences of " + DIV, -1 );
		}
		
		try{
			Range range = Range.parse(parts[0].trim());
			long loc = Long.parseLong(parts[1].trim());
			return new RelocateRange(range, loc);
		}
		catch(Exception ex){
			
			ParseException parseEx = 
				new ParseException("Couldn't parse \"" + relocString + "\" " +
				"into a RelocateRange. Format should be A-B/C, with no whitespaces", -1);
			parseEx.initCause(ex);
			throw parseEx;
		}

	}

	/**
	 * Returns the range of blocks to be relocated. <p>
	 * 
	 * The numbers in the Range are indices of the blocks in the server's copy
	 * of the file. The Range consists of the pair (firstBlock, lastBlock + 1).
	 * 
	 * @return	A Range object containing the block range
	 */
	public Range getBlockRange() {
		return blockRange;
	}

	/**
	 * Returns the byte position to which the Range should be relocated.
	 * 
	 * @return The new offset of the Range
	 */
	public long getOffset() {
		return newOffset;
	}
}
