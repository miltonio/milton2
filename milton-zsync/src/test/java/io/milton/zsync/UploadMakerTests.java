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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests for both UploadMaker and UploadMakerEx classes
 * 
 * @author Nick
 *
 */
public class UploadMakerTests {
	
	long[] fileMap = {-1, -1, 20, 90, 100, 110, -1, 70, 80, -1};
	long fileLength = 200;
	int blockSize = 10;
	
	@Test
	public void testServersMissingRanges() {
		
//		List<Range> expRanges = new ArrayList<Range>();
//		expRanges.add(new Range(0, 20));
//		expRanges.add(new Range(30, 70));
//		expRanges.add(new Range(120, 200));
//		
//		List<Range> actRanges = UploadMaker.serversMissingRanges(fileMap,fileLength, blockSize);
//		
//		
//		String expString = "", actString = "";
//		for (Range expRange: expRanges){
//			expString += expRange.getRange() + " ";
//		}
//		for (Range actRange: actRanges){
//			actString += actRange.getRange() + " ";
//		}
//		
//		Assert.assertEquals( expString, actString );
	}
	
	@Test
	public void testServersRelocationRanges() {
		
//		List<RelocateRange> expRelocs = new ArrayList<RelocateRange>();
//		expRelocs.add(new RelocateRange(new Range(3, 6), 90));
//		
//		List<RelocateRange> actRelocs = UploadMaker.serversRelocationRanges(fileMap, 
//				blockSize, fileLength, true);
//		
//		String expString = Arrays.toString( expRelocs.toArray() );
//		String actString = Arrays.toString( actRelocs.toArray() );
//		
//		Assert.assertEquals( expString , actString );
		
	}
	
	@Test
	public void testServersMissingRangesEx() throws UnsupportedEncodingException, IOException {
		
		List<OffsetPair> reverseMap = new ArrayList<OffsetPair>();
		reverseMap.add(new OffsetPair(20, 2 ));
		reverseMap.add(new OffsetPair(90, 3 ));
		reverseMap.add(new OffsetPair(100, 4 ));
		reverseMap.add(new OffsetPair(110, 5 ));
		reverseMap.add(new OffsetPair(70, 7 ));
		reverseMap.add(new OffsetPair(80, 8 ));
		
		File testFile = createFile( 200 );
		InputStream actStream = UploadMakerEx.serversMissingRangesEx( reverseMap,
				testFile, blockSize );
		
		String expString = "Range: 0-20\n"
			+ "ABCDEFGHIJKLMNOPQRST\n"
			+ "Range: 30-70\n"
			+ "EFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQR\n"
			+ "Range: 120-200\n"
			+ "QRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQR";
		
		String actString = IOUtils.toString( actStream, "US-ASCII" );
		System.out.println( "serversMissingRangesEx: \n" + actString );
		Assert.assertEquals( expString, actString );
	}
	
	@Test
	public void testServersRelocationRangesEx() throws UnsupportedEncodingException, IOException {
		
		List<OffsetPair> reverseMap = new ArrayList<OffsetPair>();
		reverseMap.add(new OffsetPair(20, 2 ));
		reverseMap.add(new OffsetPair(90, 3 ));
		reverseMap.add(new OffsetPair(100, 4 ));
		reverseMap.add(new OffsetPair(110, 5 ));
		reverseMap.add(new OffsetPair(70, 7 ));
		reverseMap.add(new OffsetPair(80, 8 ));
		
		InputStream actStream = UploadMakerEx.serversRelocationRangesEx(reverseMap,
				blockSize, fileLength, true);
			
		String expString = "3-6/90\n";
		String actString = IOUtils.toString( actStream, "US-ASCII" );
		
		System.out.println( "serversRelocationRangesEx: \n" + actString );
		Assert.assertEquals( expString , actString );
	}
	
	/**
	 * Creates a File of the specified length containing A-Z repeated
	 * 
	 * @param length
	 * @return
	 * @throws IOException
	 */
	private File createFile( int length ) throws IOException {
		
		File testFile = File.createTempFile("Test", "File");
		FileOutputStream fOut = new FileOutputStream( testFile );
		StringBuffer sbr = new StringBuffer( length );
		
		for ( int index = 0; index < length; index++ ) {
			
			char nextchar = (char) ('A' + index%26);
			sbr.append( nextchar );
		}
		
		fOut.write( sbr.toString().getBytes( "US-ASCII" ) );
		fOut.close();
		
		return testFile;
	}

}
