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

import io.milton.common.BufferingOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;


/**
 * A container for the information transmitted in a ZSync PUT upload. The information currently consists of some
 * headers (file length, block size, etc...), an InputStream containing a list of RelocateRanges for relocating matching blocks, 
 * and an InputStream containing a sequence of data chunks (along with their ranges). The Upload class also contains methods for 
 * translating to/from a stream (getInputStream and parse, respectively).
 * 
 * @author Nick
 *
 */
public class Upload {

	/**
	 * The character encoding used to convert Strings to bytes. The default is US-ASCII.
	 * The methods involved in parsing assume one byte per character.
	 */
	public final static String CHARSET = "US-ASCII";
	/**
	 * The character marking the end of a line. The default is '\n'
	 */
	public final static char LF = '\n';
	/**
	 * A String that marks the beginning of a range of uploaded bytes. Currently unused.
	 */
	public String DIV = "--DIVIDER";

	public final static String VERSION = "zsync";
	
	public final static String BLOCKSIZE = "Blocksize";

	public final static String FILELENGTH = "Length";
	/**
	 * The total number of bytes of new data to be transmitted. Currently Unused.
	 */
	public final static String NEWDATA = "ContentLength";

	public final static String SHA_1 = "SHA-1";

	public final static String RELOCATE = "Relocate";
	 
	public final static String RANGE = "Range";
	
	private String version;
	private String sha1;
	private long blocksize;
	private long filelength;

	private InputStream relocStream;
	private InputStream dataStream;
	
	/**
	 * Returns the list of headers in String format, in the proper format for upload. The
	 * list is terminated by the LF character.
	 *
	 * @return A String containing the headers
	 */
	public String getParams(){
		
		StringBuilder sbr = new StringBuilder();
		
		sbr.append( paramString( VERSION, version ) );
		sbr.append( paramString( FILELENGTH, filelength ) );
		sbr.append( paramString( BLOCKSIZE, blocksize ) );
		sbr.append( paramString( SHA_1, sha1 ) );
		
		return sbr.toString();
	}
	
	public static String paramString( String key, Object value ){
		
		return key + ": " + value + LF;
	}
	
	/**
	 * Constructs an empty Upload object. Its fields need to be set individually.
	 */
	public Upload(){
		
		//this.relocList = new ArrayList<RelocateRange>();
		//this.dataList = new ArrayList<DataRange>();
	}

	/**
	 * Parses the InputStream into an Upload object.<p/>
	 * 
	 * The method initially parses the headers from the InputStream by reading the sequence of keys (the String preceding the first colon in each line) 
	 * and values ( the String following the colon and terminated by the LF character ) and invoking {@link #parseParam} on each key value pair. 
	 * If the key is RELOCATE, then the value is not read, but is copied into a BufferingOutputStream and stored in the relocStream field. Parsing of headers
	 * continues until a "blank" line is reached, ie a line that is null or contains only whitespace, which indicates the beginning of the data section.
	 * A reference to the remaining InputStream is then stored in the dataStream field.<p/>
	 * 
	 * @param in The InputStream containing the ZSync upload
	 * @return A filled in Upload object
	 */
	public static Upload parse(InputStream in) {
	
		Upload um = new Upload();
		int bytesRead = 0; //Enables a ParseException to specify the offset

		try{
			//Maximum number of bytes to search for delimiters
			int MAX_SEARCH = 1024; 

			String key;
			//Parse headers until a null/all-whitespace line is encountered
			while ( !StringUtils.isBlank( ( key = readKey( in, MAX_SEARCH ) ) ) ) {
				
				/*
				 * Add one to bytesRead since the delimiter was read but omitted from the String. 
				 * The final value of bytesRead may end up off by one if the end of input is reached, since no 
				 * delimiter is read in that case.
				 */
				bytesRead += key.length() + 1;
				key = key.trim();
				
				if ( key.equalsIgnoreCase( RELOCATE ) ) {
					/*
					 * Copies the Relocate values to a BufferingOutputStream
					 */
					BufferingOutputStream relocOut = new BufferingOutputStream( 16384 );
					bytesRead += copyLine( in, 1024*1024*64, relocOut );
					relocOut.close();
					
					um.setRelocStream( relocOut.getInputStream() );
					
				} else {
					/*
					 * Key is not "Relocate", so parse header
					 */
					String value = readValue( in, MAX_SEARCH );
					bytesRead += value.length() + 1;
					value = value.trim();
					
					um.parseParam( key, value );
				}
			}
			
			/*
			 * A blank line has been read, indicating the end of the headers, so the unread
			 * portion of the InputStream is the byte range section. 
			 */
			
			um.setDataStream( in );

		} catch ( IOException e ) {
			throw new RuntimeException( "Couldn't parse upload, IOException.", e );
			
		} catch( ParseException e ){
			
			//Set the offset of the ParseException to bytesRead
			ParseException ex = new ParseException( e.getMessage(), bytesRead );
			throw new RuntimeException(  ex );
		} 

		return um;
	}
	
	/**
	 * Returns the next String terminated by one of the specified delimiters or the end of the InputStream.<p/>
	 * 
	 * This method simply reads from an InputStream one byte at a time, up to maxsearch bytes, until it reads a byte equal to one of the delimiters
	 * or reaches the end of the stream. It uses the CHARSET encoding to translate the bytes read into a String, which it returns with delimiter excluded, 
	 * or it throws a ParseException if maxSearch bytes are read without reaching a delimiter or the end of the stream.<p/>
	 * 
	 * A non-buffering method is used because a buffering reader would likely pull in part of the binary data
	 * from the InputStream. An alternative is to use a BufferedReader with a given buffer size and use
	 * mark and reset to get back binary data pulled into the buffer.
	 * 
	 * @param in The InputStream to read from
	 * @param delimiters A list of byte values, each of which indicates the end of a token
	 * @param maxsearch The maximum number of bytes to search for a delimiter
	 * @return The String containing the CHARSET decoded String with delimiter excluded
	 * @throws IOException
	 * @throws ParseException If a delimiter byte is not found within maxsearch reads
	 */
	public static String readToken( InputStream in, byte[] delimiters, int maxsearch ) throws ParseException, IOException {
		
		if ( maxsearch <= 0 ) {
			throw new RuntimeException( "readToken: Invalid maxsearch " + maxsearch );
		}
		
		ByteBuffer bytes = ByteBuffer.allocate( maxsearch );
		byte nextByte;
		
		try {
			
			read:
			while ( ( nextByte = (byte) in.read() ) > -1 ) {
				
				for ( byte delimiter : delimiters ) {		
					if ( nextByte == delimiter ) {
						break read;
					} 
				}
				bytes.put( nextByte );
			}
		
			bytes.flip();
			return Charset.forName( CHARSET ).decode( bytes ).toString();
			
		} catch ( BufferOverflowException ex ) {
			
			throw new ParseException( "Could not find delimiter within " +  
					maxsearch + " bytes.", 0 );
		}
	}
	
	/**
	 * Helper method that reads the String preceding the first colon or newline in the InputStream.
	 * 
	 * @param in The InputStream to read from
	 * @param maxsearch The maximum number of bytes allowed in the key
	 * @return The CHARSET encoded String that was read
	 * @throws ParseException If a colon, newline, or end of input is not reached within maxsearch reads
	 * @throws IOException
	 */
	private static String readKey ( InputStream in, int maxsearch ) throws ParseException, IOException {
		
		byte NEWLINE = Character.toString( LF ).getBytes( CHARSET )[0];
		byte COLON = ":".getBytes( CHARSET )[0];
		byte[] delimiters = { NEWLINE, COLON };
		
		return readToken( in, delimiters, maxsearch );
	}
	
	/**
	 * Helper method that reads the String preceding the first newline in the InputStream.
	 * 
	 * @param in The InputStream to read from
	 * @param maxsearch The maximum number of bytes allowed in the value
	 * @return The CHARSET encoded String that was read
	 * @throws ParseException If a newline or end of input is not reached within maxsearch reads
	 * @throws IOException
	 */
	public static String readValue ( InputStream in, int maxsearch ) throws ParseException, IOException {
		
		byte NEWLINE = Character.toString( LF ).getBytes( CHARSET )[0];
		byte[] delimiters = { NEWLINE };
		
		return readToken( in, delimiters, maxsearch );
	}
	
	/**
	 * A helper method that reads from an InputStream and copies to an OutputStream until the LF character is read (The LF is not
	 * copied to the OutputStream). An exception is thrown if maxsearch bytes are read without encountering LF. This is used by {@link #parse} 
	 * to copy the relocate values into a BufferingOutputStream. 
	 * 
	 * @param in The InputStream to read from
	 * @param maxsearch The maximum number of bytes to search for a newline
	 * @param out The OutputStream to copy into
	 * @return The number of bytes read from in
	 * @throws IOException
	 * @throws ParseException If a newline is not found within maxsearch reads
	 */
	private static int copyLine( InputStream in, int maxsearch, OutputStream out ) throws IOException, ParseException {
		
		if ( maxsearch <= 0 ) {
			throw new RuntimeException( "copyLine: Invalid maxsearch " + maxsearch );
		}
		
		byte nextByte, bytesRead = 0;
		byte NEWLINE = Character.toString( LF ).getBytes( CHARSET )[0];
		
		while ( (nextByte = (byte) in.read()) > -1 ) {
			
			if ( ++bytesRead > maxsearch ) {
				throw new ParseException( "Could not find delimiter within " +  
						maxsearch + " bytes.", 0 );
			}
			if ( nextByte == NEWLINE ) {
				break;
			}
			out.write( nextByte );
		}
		
		return bytesRead;
	}
	
	/**
	 * Parses a String header by setting the appropriate field in upload if the key is recognized 
	 * and ignoring keys that are not recognized.
	 * 
	 * @param key The key String with leading/trailing whitespace omitted
	 * @param value The value String with leading/trailing whitespace omitted
	 * @throws ParseException if the value of a recognized key cannot be properly parsed
	 */
	private void parseParam( String key, String value  ) throws ParseException {
		
		if (StringUtils.isBlank( key ) || StringUtils.isBlank( value )) {
			
			return;
		}
		try{
			if (key.equalsIgnoreCase(VERSION)){
				this.setVersion(value);
			} else if (key.equalsIgnoreCase(FILELENGTH)){
				this.setFilelength(Long.parseLong(value));
			} else if (key.equalsIgnoreCase(BLOCKSIZE)){
				this.setBlocksize(Long.parseLong(value));
			} else if (key.equalsIgnoreCase(SHA_1)){
				this.setSha1( value );
			} 
		} catch (NumberFormatException ex) {
			
			throw new ParseException( "Cannot parse " + value + " into a long.", -1 );
		}
	}
	
	/**
	 * Returns an InputStream containing a complete ZSync upload (Params, Relocate stream, and ByteRange stream), 
	 * ready to be sent as the body of a PUT request. <p/>
	 * 
	 * Note: In this implementation, any temporary file used to store the RelocateRanges will be automatically deleted when this stream
	 * is closed, so a second invocation of this method on the same Upload object is likely to throw an exception.
	 * Therefore, this method should be used only once per Upload object.
	 * 
	 * @return The complete ZSync upload
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public InputStream getInputStream() throws UnsupportedEncodingException, IOException{

		List<InputStream> streamList = new ArrayList<InputStream>();
		
		/*
		 * The getParams and getRelocStream must be terminated by a single LF character.
		 */
		streamList.add( IOUtils.toInputStream( getParams() , CHARSET ) );
		streamList.add( IOUtils.toInputStream( RELOCATE + ": ", CHARSET ) );
		streamList.add( getRelocStream() ); 
		/* Prepend the data portion with a blank line. */
		streamList.add( IOUtils.toInputStream( Character.toString( LF ), CHARSET) ); 
		streamList.add( getDataStream() );

		return new SequenceInputStream( new IteratorEnum<InputStream>( streamList ) );
	}

	/**
	 * Gets the zsync version of the upload sender (client)
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the zsync version of the upload sender (client)
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Gets the checksum for the entire source file
	 */
	public String getSha1() {
		return sha1;
	}

	/**
	 * Sets the checksum for the entire source file, which allow the server to validate the new file
	 * after assembling it.
	 */
	public void setSha1(String sha1) {
		this.sha1 = sha1;
	}

	/**
	 * Gets the blocksize used in the upload. 
	 */
	public long getBlocksize() {
		return blocksize;
	}

	/**
	 * Sets the blocksize used in the upload. The server needs this to translate block ranges into byte ranges
	 */
	public void setBlocksize(long blocksize) {
		//System.out.println("Upload: setBlockSize: " + blocksize);
		this.blocksize = blocksize;
	}

	/**
	 * Gets the length of the (assembled) source file being uploaded
	 */
	public long getFilelength() {
		return filelength;
	}

	/**
	 * Sets the length of the (assembled) source file being uploaded
	 */
	public void setFilelength(long filelength) {
		this.filelength = filelength;
	}

	/**
	 * 	
	 * Gets the list of RelocateRanges, which tells the server which blocks of the previous
	 * file to keep, and where to place them in the new file. The current format is a comma 
	 * separated list terminated by LF.
	 *
	 */
	public InputStream getRelocStream() {
		return relocStream;
	}

	/**
	 * 	
	 * Sets the list of RelocateRanges, which tells the server which blocks of the previous
	 * file to keep, and where to place them in the new file. The current format is a comma 
	 * separated list terminated by LF.
	 *
	 * @param relocStream 
	 */
	public void setRelocStream(InputStream relocStream) {
		this.relocStream = relocStream;
	}
	
	/**
	 * Gets the list of uploaded data chunks ( byte Ranges and their associated data ). 
	 */
	public InputStream getDataStream() {
		return dataStream;
	}

	/**
	 * Sets the list of data chunks to be uploaded ( byte Ranges and their associated data ).  The stream
	 * should contain no leading whitespace.
	 * 
	 */
	public void setDataStream(InputStream dataStream) {
		this.dataStream = dataStream;
	}

	/**
	 * An <code>Enumeration</code> wrapper for an Iterator. This is needed in order to construct
	 * a <code>SequenceInputStream</code> (used to concatenate upload sections), which takes an <code>Enumeration</code> argument.
	 * 
	 * @author Nick
	 *
	 * @param <T> The type of object being enumerated
	 */
	public static class IteratorEnum <T> implements Enumeration<T>{

		Iterator<T> iter;
		
		public IteratorEnum( List<T> list ) {
			
			this.iter = list.iterator();
		}

		@Override
		public boolean hasMoreElements() {
			
			return iter.hasNext();
		}

		@Override
		public T nextElement() {
			
			return iter.next();
		}
	}
	
	/**
	 * An object representing a (Key, Value) pair of Strings. Currently unused.
	 * 
	 * @author Nick
	 *
	 */
	public static class KeyValue {
		
		public String KEY;
		public String VALUE;
		
		public KeyValue ( String key, String value ) {
			
			this.KEY = key;
			this.VALUE = value;
		}
		
		/**
		 * Parses a String of the form "foo: bar" into a KeyValue object whose KEY is the
		 * String preceding the first colon and VALUE is the String following the first colon
		 * ( leading and trailing whitespaces are removed from KEY and VALUE ). A ParseException is
		 * thrown if the input String does not contain a colon.
		 * 
		 * @param kv A String of the form "foo: bar"
		 * @return A KeyValue object with a KEY of "foo" and a VALUE of "bar"
		 * @throws ParseException If no colon is found in <b>kv</b>
		 */
		public static KeyValue parseKV( String kv ) throws ParseException  {
			
			int colonIndex = kv.indexOf(':');
			if (colonIndex == -1){
				
				throw new ParseException("No colon found in \"" + kv + "\"", colonIndex);
			}
			
			String key = kv.substring(0, colonIndex).trim();
			String value = kv.substring(colonIndex + 1).trim();
			
			return new KeyValue( key, value );
		}
	}
	


}
