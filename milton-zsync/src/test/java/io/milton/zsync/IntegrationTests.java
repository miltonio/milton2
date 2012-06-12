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

import io.milton.common.Path;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import io.milton.httpclient.Host;
import io.milton.httpclient.HttpResult;

/**
 * Tests the complete ZSync upload procedure. 
 * 
 * @author Nick
 *
 */
public class IntegrationTests {

	File localcopy;
	File servercopy;
	String filepath;
	Host host;
	int blocksize;
	long startUsed; // memory
	
	/**
	 * Initializes localcopy and servercopy. These should be changed to reflect actual location of the client
	 * and server files.
	 */
	@Before
	public void setUp() {
		
		filepath = "src" + File.separator + "test" + File.separator + "resources" + File.separator; 
		
		blocksize = 1024;
		//blocksize = 64;
		//blocksize = 1024 * 8;		
		host = new Host("localhost", "webdav", 8080, "user1", "pwd1", null, null);
	}

	/**
	 * Sends a ZSync PUT request and asserts whether the response is 204.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testUpload_Large_Text() throws Exception {
		System.out.println("XXXXXXXXXXXXXXXXXXXXx  testUpload_Large_Text   XXXXXXXXXXXXXXXXXXXXXXXXxx ");
		localcopy = new File( filepath + "large-text-local.txt" );
		servercopy = new File( filepath + "large-text-server.txt" );		
				
		//String baseUrl = host.getHref( Path.path( servercopy.getName() ) );
		Path baseUrl = Path.path(servercopy.getName());
		host.doPut(baseUrl , servercopy, null);
		
		File zsyncFile = createMetaFile( "servercopy.zsync", blocksize, servercopy );
		File uploadFile = makeAndSaveUpload( localcopy, zsyncFile, filepath + "localcopy2.UPLOADZS" );
		System.out.println("Created upload file: " + uploadFile.getAbsolutePath() + " of " + formatBytes(uploadFile.length()) );
	
		//Change to correct url of servercopy.txt
		String url = host.getHref( Path.path( servercopy.getName() + "/.zsync" ) );
		InputStream uploadIn = new FileInputStream( uploadFile );
        HttpResult httpResult = host.doPut(url, uploadIn, uploadFile.length(), null, null );
		uploadIn.close();
		Assert.assertEquals( 204, httpResult.getStatusCode() );
		System.out.println("");
		System.out.println("");
	}
	
	@Test
	public void testMakeAndReadSmallTextUpload() throws Exception, ParseException{
		System.out.println("XXXXXXXXXXXXXXXXXXXXXX testMakeAndReadSmallTextUpload XXXXXXXXXXXXXXXXXXXXXXXXxxxxxx");
		servercopy = new File(filepath + "small-text-server.txt");
		localcopy = new File(filepath + "small-text-local.txt");
		if( !servercopy.exists()) {
			throw new RuntimeException("Couldnt find: " + servercopy.getAbsolutePath());
		}
		if( !localcopy.exists()) {
			throw new RuntimeException("Couldnt find: " + localcopy.getAbsolutePath());
		}
		
		//String baseUrl = host.getHref( Path.path( servercopy.getName() ) );
		Path baseUrl = Path.path(servercopy.getName());
		host.doPut(baseUrl , servercopy, null);		
		
		File zsyncFile = createMetaFile("small-text.zsync", 16, servercopy ); // use small blocksize
		File uploadFile = makeAndSaveUpload( localcopy, zsyncFile, filepath + "small-text-local.UPLOADZS" );
		File assembledFile = readSavedUpload( uploadFile, filepath + "small-text-assembled.txt", servercopy );
		
		String localSha1 =  new SHA1( localcopy ).SHA1sum();
		String assembledSha1 = new SHA1( assembledFile ).SHA1sum();
		
		Assert.assertEquals( localSha1, assembledSha1 );
		System.out.println("");
		System.out.println("");
	}	
	
	@Test
	public void testMakeAndRead_Large_CSV() throws Exception, ParseException{
		System.out.println("XXXXXXXXXXXXXXXXXXXXXxxx    testMakeAndRead_Large_CSV    XXXXXXXXXXXXXXXXXXXXXXXXXXX");
		servercopy = new File(filepath + "large-csv-server.csv");
		localcopy = new File(filepath + "large-csv-local.csv");
		if( !servercopy.exists()) {
			throw new RuntimeException("Couldnt find: " + servercopy.getAbsolutePath());
		}
		if( !localcopy.exists()) {
			throw new RuntimeException("Couldnt find: " + localcopy.getAbsolutePath());
		}
		
		//String baseUrl = host.getHref( Path.path( servercopy.getName() ) );
		Path baseUrl = Path.path(servercopy.getName());
		host.doPut(baseUrl , servercopy, null);
		
		
		File zsyncFile = createMetaFile("large-csv.zsync", 256, servercopy ); // use small blocksize
		File uploadFile = makeAndSaveUpload( localcopy, zsyncFile, filepath + "large-csv.UPLOADZS" );
		File assembledFile = readSavedUpload( uploadFile, filepath + "large-csv-assembled.xls", servercopy );
		
		String localSha1 =  new SHA1( localcopy ).SHA1sum();
		String assembledSha1 = new SHA1( assembledFile ).SHA1sum();
		
		Assert.assertEquals( localSha1, assembledSha1 );
		System.out.println("");
		System.out.println("");
	}		
	
	@Test
	public void testMakeAndRead_Large_Excel() throws Exception, ParseException{
		System.out.println("XXXXXXXXXXXXXXXXXXXXXxxx    Large Excel    XXXXXXXXXXXXXXXXXXXXXXXXXXX");
		servercopy = new File(filepath + "large-excel-server.xls");
		localcopy = new File(filepath + "large-excel-local.xls");
		if( !servercopy.exists()) {
			throw new RuntimeException("Couldnt find: " + servercopy.getAbsolutePath());
		}
		if( !localcopy.exists()) {
			throw new RuntimeException("Couldnt find: " + localcopy.getAbsolutePath());
		}
		
		//String baseUrl = host.getHref( Path.path( servercopy.getName() ) );
		Path baseUrl = Path.path(servercopy.getName());
		host.doPut(baseUrl , servercopy, null);
		
		
		File zsyncFile = createMetaFile("large-excel.zsync", 256, servercopy ); // use small blocksize
		File uploadFile = makeAndSaveUpload( localcopy, zsyncFile, filepath + "large-csv.UPLOADZS" );
		File assembledFile = readSavedUpload( uploadFile, filepath + "large-csv-assembled.xls", servercopy );
		
		String localSha1 =  new SHA1( localcopy ).SHA1sum();
		String assembledSha1 = new SHA1( assembledFile ).SHA1sum();
		
		Assert.assertEquals( localSha1, assembledSha1 );
		System.out.println("");
		System.out.println("");
	}		
	
	/**
	 * Reads the ZSync upload data that was saved to uploadFile, constructs an UploadReader
	 * to assemble the new file, and saves (and returns) the assembled file as fileName
	 * 
	 * @param uploadFile A file containing the data from an upload
	 * @param fileName The pathname String (with file name included)
	 * @return The assembled File
	 * @throws IOException
	 * @throws ParseException
	 */
	private File readSavedUpload( File uploadFile, String fileName, File serverFile ) throws IOException, ParseException {
		
		InputStream uploadIn = new FileInputStream( uploadFile );
		UploadReader um = new UploadReader( serverFile, uploadIn );
		
		File assembledFile = new File( fileName );
		if( assembledFile.exists() ) {
			if( !assembledFile.delete() ) {
				throw new RuntimeException("Couldnt delete previous assembled file: " + assembledFile.getAbsolutePath());
			}
		}
		long tm = System.currentTimeMillis();
		File tempAssembled = um.assemble();
		tm = System.currentTimeMillis() - tm;
		System.out.println("Assembled file in: " + tm + "ms");
		FileUtils.moveFile( tempAssembled, assembledFile );
		
		
		uploadIn.close();
		System.out.println("Assesmbled to: " + assembledFile.getAbsolutePath());		
		return assembledFile;
		
	}
	
	/**
	 * Constructs an UploadMaker/UploadMakerEx, saves the Upload stream to a new File with
	 * name uploadFileName, and returns that File.
	 * 
	 * @param localFile The local file to be uploaded
	 * @param zsFile The zsync of the server file
	 * @param uploadFileName The name of the File in which to save the upload stream
	 * @return
	 * @throws IOException
	 */
	private File makeAndSaveUpload(File localFile, File zsFile, String uploadFileName) throws IOException {
		System.out.println("------------- makeAndSaveUpload --------------------");
		
		System.gc();
		Runtime rt = Runtime.getRuntime();
				
		UploadMaker umx = new UploadMaker( localFile, zsFile );
		InputStream uploadIn = umx.makeUpload();
		
		File uploadFile = new File( uploadFileName );

		if( uploadFile.exists()) {
			if( !uploadFile.delete()) {
				throw new RuntimeException("Couldnt delete: " + uploadFile.getAbsolutePath());
			}
		}
		FileOutputStream uploadOut = new FileOutputStream( uploadFile );
		
		System.gc();
		System.out.println("Memory stats: " + formatBytes(rt.maxMemory()) + " - " + formatBytes(rt.totalMemory()) + " - " + formatBytes(rt.freeMemory()));
		long endUsed = (rt.totalMemory() - rt.freeMemory());
		System.out.println("Start used memory: " + formatBytes(startUsed) + " end used memory: " + formatBytes(endUsed) + " - delta: " + formatBytes(endUsed - startUsed));
		System.out.println("");

		IOUtils.copy( uploadIn, uploadOut );
		uploadIn.close();
		uploadOut.close();
		
		System.out.println("Created upload of size: " + formatBytes(uploadFile.length()) + " from local file: " + formatBytes(localFile.length()));		
		
		return uploadFile;
		
		
	}
	
	/**
	 * Creates the zsync File for servercopy, and saves it to a File with name fileName
	 * @param fileName The name of the file in which to save the zsync data
	 * @param blocksize The block size to use in MetaFileMaker
	 * @return The created zsync File
	 * @throws FileNotFoundException
	 */
	private File createMetaFile(String fileName, int blocksize, File serverFile) throws FileNotFoundException{
		System.out.println("---------------- createMetaFile -------------------" );
		
		System.gc();
		Runtime rt = Runtime.getRuntime();
		startUsed = rt.totalMemory() - rt.freeMemory();
		
		MetaFileMaker mkr = new MetaFileMaker();
		File zsfile = mkr.make( null , blocksize, serverFile );
		System.gc();
		System.out.println("Memory stats: " + formatBytes(rt.maxMemory()) + " - " + formatBytes(rt.totalMemory()) + " - " + formatBytes(rt.freeMemory()));
		long endUsed = (rt.totalMemory() - rt.freeMemory());
		System.out.println("Start used memory: " + formatBytes(startUsed) + " end used memory: " + formatBytes(endUsed) + " - delta: " + formatBytes(endUsed - startUsed));
		
		File dest = new File ( filepath + fileName );
		if( dest.exists() ) {
			if( !dest.delete()) {
				throw new RuntimeException("Failed to delete previous meta file: " + dest.getAbsolutePath());
			}
		}
		System.out.println("rename meta file to: " + dest.getAbsolutePath());
		if( !zsfile.renameTo( dest ) ) {
			throw new RuntimeException("Failed to rename to: " + dest.getAbsolutePath());
		}
		System.out.println("Created meta file of size: " + formatBytes(dest.length()) + " from source file of size: " + formatBytes(serverFile.length()) );
		System.out.println("");
		return dest;
	}
	
	/**
	 * Writes/reads the upload stream to/from a File, and asserts whether the assembled File
	 * has the same checksum as the client File.
	 * 
	 * @throws IOException
	 * @throws ParseException
	 */
	//@Test
	public void testMakeAndReadUpload() throws IOException, ParseException{
		
		File zsyncFile = createMetaFile("serverfile.zsync", blocksize, servercopy );
		File uploadFile = makeAndSaveUpload( localcopy, zsyncFile, filepath + "localcopy.UPLOADZS" );
		File assembledFile = readSavedUpload( uploadFile, filepath + "assembledcopy.pdf", servercopy );
		
		String localSha1 =  new SHA1( localcopy ).SHA1sum();
		String assembledSha1 = new SHA1( assembledFile ).SHA1sum();
		
		Assert.assertEquals( localSha1, assembledSha1 );
	}
		
	
	private String formatBytes(long l) {
		if( l < 1000 ) {
			return l + " bytes";
		} else if( l < 1000000) {
			return l/1000 + "KB";
		} else {
			return l/1000000 + "MB";
		}
	}			
}
