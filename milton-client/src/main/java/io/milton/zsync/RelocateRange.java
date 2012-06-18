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
