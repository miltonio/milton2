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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import io.milton.http.Range;

import java.io.FileOutputStream;
import org.apache.commons.io.IOUtils;

/**
 * A slight variation of <code>UploadMaker</code> that accommodates updating of potentially
 * redundant files, ie files containing blocks that repeat at multiple offsets. <p/>
 * 
 * It is not necessary to use this class rather than UploadMaker. This class simply 
 * decreases the amount of data that needs to be transmitted for certain cases of redundant files. The
 * sole difference from UploadMaker is that this class uses methods that expect a reverse block-matching in the
 * form of an array where f[serverIndex] = localOffset (actually implemented as a List of OffsetPairs), 
 * whereas UploadMaker expects f[localOffset] = serverIndex.<p/>  
 * 
 * The main advantage of the reverse mapping is that it allows multiple identical block ranges in the local
 * file to be mapped to a single range in the server file. If a given block occurs more times on the local file than
 * on the server file, the f[serverIndex] array will not identify all of those local occurences, and the unmatched ones
 * will be transmitted needlessly.<p/>
 * 
 *
 * @author Nick
 * 
 */
public class UploadMakerEx {

	
	/**
	 * The local file that will replace the server file
	 */
	public final File localCopy;
	
	/**
	 * The .zsync of the server file to be replaced
	 */
	public final File serversMetafile;

	private MetaFileReader metaFileReader;
	private MakeContextEx uploadContext;
	private Upload upload;
	
	/**
	 * Constructor that initializes an Upload object and invokes methods to parse
	 * the zsync file.
	 * 
	 * @param sourceFile The client file to be uploaded
	 * @param zsFile The zsync of the server's file
	 * @throws IOException
	 */
	public UploadMakerEx(File sourceFile, File zsFile) throws IOException{
		
		this.localCopy = sourceFile;
		this.serversMetafile = zsFile;
		this.upload = new Upload();
		this.initMetaData();
	}
	
	private void initMetaData(){
		
		this.metaFileReader = new MetaFileReader( serversMetafile );
		this.uploadContext = new MakeContextEx( metaFileReader.getHashtable(), metaFileReader.getBlockCount(), metaFileReader.getBlocksize() );
	}
	
	/**
	 * Invokes the methods to generate the information that needs to be sent to the server
	 * and fills in the internal Upload object.
	 * 
	 * @throws IOException
	 */
	private void initUpload() throws IOException{
	
		upload.setVersion( "testVersion" );
		upload.setBlocksize( metaFileReader.getBlocksize() );
		upload.setFilelength( localCopy.length() );
		upload.setSha1(  new SHA1( localCopy ).SHA1sum()  );
		
		/*
		if ( upload.getSha1().equals( metaFileReader.getSha1() ) ) {
			
			return;
		}*/
		
		InputStream dataRanges = serversMissingRangesEx( uploadContext.getReverseMap(), localCopy, metaFileReader.getBlocksize() );
		InputStream relocRanges = serversRelocationRangesEx( uploadContext.getReverseMap(),  metaFileReader.getBlocksize(), localCopy.length(), true );
		
		upload.setRelocStream( relocRanges );
		upload.setDataStream( dataRanges );
	
	}
	
	/**
	 * Determines the byte ranges from the client file that need to be uploaded to the server.
	 * 
	 * @param reverseMap The List of block-matches obtained from MakeContextEx
	 * @param local The local file being uploaded
	 * @param blockSize The block size used in reverseMap
	 * @return An InputStream containing the dataStream portion of an Upload
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 * @see UploadMaker#serversMissingRanges
	 */
	public static InputStream serversMissingRangesEx(List<OffsetPair> reverseMap, File local, int blockSize) throws UnsupportedEncodingException, IOException{
		
		ByteRangeWriter dataWriter = new ByteRangeWriter( 16384 );
		RandomAccessFile randAccess = null;
		
		Collections.sort(reverseMap, new OffsetPair.LocalSort()); 
		reverseMap.add(new OffsetPair(local.length(), -1)); 

		try {
			
			randAccess = new RandomAccessFile( local, "r" );
			long prevEnd = 0;
			
			for (OffsetPair pair: reverseMap){
				
				long offset = pair.localOffset;
				if (offset - prevEnd > 0){
					
					dataWriter.add(new Range(prevEnd, offset), randAccess );
				}
				prevEnd = offset + blockSize;
				
			}
			
			return dataWriter.getInputStream();
			
		} finally {
			Util.close( randAccess );
		}

	}
	
	/**
	 * Determines the instructions needed by the server to relocate blocks of data already contained
	 * in its version of the file.
	 * 
	 * @param reverseMap The List of block-matches obtained from MakeContextEx
	 * @param blockSize The block size used to generate reverseMap
	 * @param fileLength The length of the client file being uploaded
	 * @param combineRanges Whether to combine consecutive block matches into a single RelocateRange
	 * @return The InputStream of RelocateRanges that need to be sent to the server
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 * @see {@link UploadMaker#serversRelocationRanges}
	 */
	public static InputStream serversRelocationRangesEx(List<OffsetPair> reverseMap, int blockSize, long fileLength, boolean combineRanges) throws UnsupportedEncodingException, IOException{
		
		RelocWriter relocRanges = new RelocWriter( 16384 );
		Collections.sort(reverseMap, new OffsetPair.RemoteSort());
		
		for (ListIterator<OffsetPair> iter = reverseMap.listIterator(); iter.hasNext(); ){
			
			OffsetPair pair = iter.next();
			long localOffset = pair.localOffset;
			long blockIndex = pair.remoteBlock;
			
			/*If the local offset and server offset of a given matching block 
			 * are the same, then no instruction is sent.
			 */
			if (localOffset >= 0 && localOffset != blockIndex*blockSize){
				
				if (localOffset > fileLength - blockSize){
					//out of range
					continue;
				}
				
				Range blockRange;
				if (combineRanges == true){
					
					blockRange = consecMatchesEx(iter, localOffset, blockIndex, blockSize);
				} else {
					
					blockRange = new Range(blockIndex, blockIndex + 1);
				}
				
				RelocateRange relocRange = new RelocateRange(blockRange, localOffset);
				//System.out.println("new relocate range: " + relocRange);
				relocRanges.add( relocRange );
			}
		}
		
		return relocRanges.getInputStream();
	}
	
	/**
	 * Returns a Range representing a sequence of contiguous server blocks, beginning at blockIndex, that 
	 * are to be relocated as a single chunk.
	 * 
	 * @param iter An iterator positioned immediately after the first match of the sequence
	 * @param localOffset The local byte offset of the first matching block of the sequence
	 * @param blockIndex The server block index of the first matching block of the sequence
	 * @param blockSize The number of bytes in a block
	 * @return A Range of contiguous blocks that are to be relocated to localOffset
	 */
	private static Range consecMatchesEx(ListIterator<OffsetPair> iter, long localOffset,
			long blockIndex, int blockSize){

		long currBlock = blockIndex;
		long currByte = localOffset;

		while (iter.hasNext()){ 
			
			OffsetPair pair = iter.next();
			
			currByte += blockSize;
			currBlock++;
			
			if (pair.localOffset != currByte || 
					pair.remoteBlock != currBlock){
				
				iter.previous();
				return new Range( blockIndex, currBlock );
				
			} 
			
		}
		return new Range(blockIndex, currBlock + 1 );
	}
	
	
	/**
	 * Constructs the List of DataRange objects containing the portions of the client file
	 * to be uploaded to the server. Currently unused.
	 * 
	 * @param ranges The List of Ranges from the client file needed by the server, which can be 
	 * obtained from {@link #serversMissingRangesEx(List, long, int)}
	 * @param local The client file to be uploaded
	 * @return An InputStream containing the dataStream portion of an Upload
	 * @throws IOException
	 */
	public static InputStream getDataRanges (List<Range> ranges, File local) throws IOException{
		
		int MAX_BUFFER = 1024*1024;
		
		ByteRangeWriter byteRanges = new ByteRangeWriter( MAX_BUFFER );
		RandomAccessFile randAccess = new RandomAccessFile( local, "r" );

		for ( Range range : ranges ) {
			
			byteRanges.add( range, randAccess );
		}

		return byteRanges.getInputStream();
	}
	
	/**
	 * Returns the stream of bytes to be used as the body of a ZSync PUT.<p/>
	 * 
	 * Note: Any temporary files used to store the data for the stream will be deleted after
	 * the stream is closed, so a second invocation of this method may not work.
	 * 
	 * @return The InputStream containing the data for a ZSync PUT
	 * @throws UnsupportedEncodingException 
	 * @throws IOException
	 */
	public InputStream makeUpload() throws IOException{
		
		try {
			
			System.out.print( "Matching client and server blocks..." );
			long t0 = System.currentTimeMillis();
			
			MapMatcher matcher = new MapMatcher();
			matcher.mapMatcher( localCopy, metaFileReader, uploadContext );
			long t1 = System.currentTimeMillis();
			
//			System.out.println( " " + ( t1 - t0 ) + " milliseconds" );
//			System.out.print( "Creating Upload..." );
			long t2 = System.currentTimeMillis();
			
			this.initUpload();
			long t3 = System.currentTimeMillis();
			
//			System.out.println(" " + ( t3 - t2 ) + " milliseconds");
			
			return upload.getInputStream();
		} catch ( IOException ex ) {
			throw new RuntimeException(  ex  );
		} 
	}
	
	
	/**
	 * Generates the upload content to a temp file.
	 * 
	 * @return
	 * @throws IOException 
	 */
	public File getUploadFile() throws IOException {
		
		InputStream uploadIn = makeUpload();
		
		File uploadFile = File.createTempFile("zsync-upload", localCopy.getName());
		FileOutputStream uploadOut = new FileOutputStream( uploadFile );
		
		IOUtils.copy( uploadIn, uploadOut );
		uploadIn.close();
		uploadOut.close();
		
		return uploadFile;				
	}	
}
