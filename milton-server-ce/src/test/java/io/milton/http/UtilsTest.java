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

package io.milton.http;

import io.milton.common.Utils;
import io.milton.http.webdav.Dest;
import static junit.framework.Assert.assertEquals;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class UtilsTest extends TestCase {

    public UtilsTest( String testName ) {
        super( testName );
    }

    public void testStripServer_NoHttp() {
        assertEquals("abc", Utils.stripServer("abc"));
    }

    public void testStripServer_WithHttp() {
        assertEquals("/abc", Utils.stripServer("http://server/abc"));
    }


    public void testPercentEncode() {
//        for( int i=0; i<180; i++ ) {
//            String s = String.valueOf((char)i);
//            System.out.println(i + " = " + s);
//        }
        assertEquals( "", Utils.percentEncode( "" ) );
        assertEquals( "abc", Utils.percentEncode( "abc" ) );
        System.out.println( "encode space: " + Utils.percentEncode( " " ) );
        assertEquals( "%20", Utils.percentEncode( " " ) );
        assertEquals( "A%26B", Utils.percentEncode( "A&B" ) );
        System.out.println( "encode curly brackets: " + Utils.percentEncode( "{}" ) );
        assertEquals( "%7B%7D", Utils.percentEncode( "{}" ) );

        // check reserved characters
        System.out.println( "? test: " + Utils.percentEncode( "$&+,/:;=?@") );
        assertEquals( "%24%26%2B%2C%2F%3A%3B%3D%3F%40", Utils.percentEncode( "$&+,/:;=?@" ) );

        assertEquals( "a[b]", Utils.decodePath( "a%5Bb%5D" ) );
        assertEquals( "a%5Bb%5D", Utils.percentEncode( "a[b]" ) );

        assertEquals( "ampersand%26", Utils.percentEncode( "ampersand&" ) );
        assertEquals( "0", Utils.percentEncode( "0" ) );
        assertEquals( "2009-01_02", Utils.percentEncode( "2009-01_02" ) );

        // check decode simple cases
        assertEquals( "abc", Utils.decodePath( "abc" ) );
        assertEquals( "/abc", Utils.decodePath( "/abc" ) );

        // this string seems to encode differently on different platforms. this
        // isnt ideal and will hopefully be corrected, but in the mean time
        // its good enough if it 'round trips' Ie encode + decode = original
        String originalUnencoded = "ne�";
        System.out.println( "encoding: " + originalUnencoded );
        String encoded = Utils.percentEncode( originalUnencoded );
        System.out.println( "encoded to: " + encoded );
        String decoded = Utils.decodePath( encoded );
        System.out.println( "decoded to: " + decoded );
        assertEquals( originalUnencoded, decoded );
    }

    public void testDecodeHref() {
        String href = "/";
        String result = Utils.decodePath( href );
        assertEquals( result, href );

        href = "/with%20space";
        result = Utils.decodePath( href );
        assertEquals( "/with space", result );

    }

    public void testDecodeHref_WithSquareBrackets() {
        String href = "/a[b]";
        String result = Utils.decodePath( href );
        assertEquals( "/a[b]", result );

    }

    
    public void testDecodeHref_DifferentCodes_SameCharacter() {
        String href = "/%C3%A4";
        String result = Utils.decodePath( href );
        showBytes(result);
        assertEquals( "/ä", result );

//        href = "/%E4";
//        result = Utils.decodePath( href );
//        showBytes(result);
//        assertEquals( "/ä", result );
    }
	
	public void testDecodeHref_AtSymbol() {
		String href = "/a%40b";
		String result = Utils.decodePath( href );
		assertEquals( "/a@b", result );
	}

    private void showBytes(String result) {
        System.out.print("ShowBytes: ");
        for( int i=0; i<result.getBytes().length; i++) {
            byte b = result.getBytes()[i];
            System.out.print(" " + b);
        }
        System.out.println("");
    }

	public void testGetDecodedDestination_Simple() {
		String testDest = "http://xyz.com/folder1/folder2/name";
		Dest dest = Utils.getDecodedDestination(testDest);
		assertEquals("xyz.com", dest.host);
		assertEquals("/folder1/folder2", dest.url);
		assertEquals("name", dest.name);
	}
	
	public void testGetDecodedDestination_WithEncodedChars() {
		String testDest = "http://xyz.com/folder1/folder2/na%20me";
		Dest dest = Utils.getDecodedDestination(testDest);
		assertEquals("xyz.com", dest.host);
		assertEquals("/folder1/folder2", dest.url);
		assertEquals("na me", dest.name);
	}		
	
	public void testGetDecodedDestination_Https() {
		String testDest = "https://xyz.com/folder1/folder2/name";
		Dest dest = Utils.getDecodedDestination(testDest);
		System.out.println("testGetDecodedDestination_Https --> " + dest);
		assertEquals("xyz.com", dest.host);
		assertEquals("/folder1/folder2", dest.url);
		assertEquals("name", dest.name);
	}	
}
