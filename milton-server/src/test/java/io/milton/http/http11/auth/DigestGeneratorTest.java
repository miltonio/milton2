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
package io.milton.http.http11.auth;

import io.milton.http.http11.auth.DigestGenerator;
import io.milton.http.Request.Method;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class DigestGeneratorTest extends TestCase {

	DigestGenerator generator;
	String password = "Circle Of Life";
	String user = "Mufasa";
	String realm = "testrealm@host.com";
	String uri = "/dir/index.html";

	@Override
	protected void setUp() throws Exception {
		generator = new DigestGenerator();
	}

	/**
	 * this test matches the example in wikipedia -
	 * http://en.wikipedia.org/wiki/Digest_access_authentication
	 */
	public void testGenerateDigest() {
		DigestResponse dr = new DigestResponse(Method.GET, user, realm, "dcd98b7102dd2f0e8b11d0f600bfb0c093", uri, "", "auth", "00000001", "0a4f113b");
		String resp = generator.generateDigest(dr, password);
		System.out.println("server resp: " + resp);
		System.out.println("expected: " + "6629fae49393a05397450978507c4ef1");
		assertEquals("6629fae49393a05397450978507c4ef1", resp);
	}

	/**
	 * this matches the data in milton-test secure folder
	 */
	public void testGenerateDigest2() {
		System.out.println("testGenerateDigest2");
		DigestResponse dr = new DigestResponse(Method.PROPFIND, user, realm, "ZWY5NTdmZDgtZjg1OC00NzhhLTg4MjctMzBlNzRmMGNjNTE4", "/webdav/secure/", "", "auth", "00000001", "7cfd3b057b80f1d9e2ff691f926c31f5");
		String resp = generator.generateDigest(dr, password);
		System.out.println("server resp: " + resp);
		System.out.println("expected: " + "2bd4ead0c52ff8191c2a0464a6e80fbb");
		assertEquals("2bd4ead0c52ff8191c2a0464a6e80fbb", resp);
		
		String a1md5 = generator.encodePasswordInA1Format(user, realm, password);
		String resp2 = generator.generateDigestWithEncryptedPassword(dr, a1md5);
		assertEquals(resp, resp2);
		
		System.out.println("----");
	}

	public void testGenerateDigest_WithAtSymbol() {
		System.out.println("testGenerateDigest_WithAtSymbol");
		DigestResponse dr = new DigestResponse(Method.PUT, "a2@bradmcevoy.com", "spliffy", "Yzc5ZWM0NmYtMWI5Ny00Mjk1LThjZmMtOWI2NGFhMzlkYmQy", "/_hashes/blobs/b1a0c2d299c7ab730364c4c57ded5148b19fd00d", "", "auth", "00000001", "1de6e471d6f31165");
		// First, check with plain text password
		String resp = generator.generateDigest(dr, "fredfred1");
		System.out.println("server resp: " + resp); //64cdbb06ffb4d809ba3f4c7428af47a6
		System.out.println("expected: " + "a436f09166a4dee57444b2079ea41af6");
		assertEquals("a436f09166a4dee57444b2079ea41af6", resp);

		// Now, check with hashed password
		String checka1md5 = generator.encodePasswordInA1Format(dr.getUser(), dr.getRealm(), "fredfred1");
		String a1md5 = "49b2d093c4741ede5924a392911bd4e3";
		assertEquals(a1md5, checka1md5);
		resp = generator.generateDigestWithEncryptedPassword(dr, a1md5);
		System.out.println("server resp: " + resp); //64cdbb06ffb4d809ba3f4c7428af47a6
		System.out.println("expected: " + "a436f09166a4dee57444b2079ea41af6");
		assertEquals("a436f09166a4dee57444b2079ea41af6", resp);
		
	}

	public void testGenerateDigestWithEncryptedPassword() {
	}

	public void testEncodePasswordInA1Format() {
		String enc = generator.encodePasswordInA1Format(user, realm, password);
		System.out.println("enc: " + enc);
		assertEquals("939e7578ed9e3c518a452acee763bce9", enc);
	}

	public void testencodeMethodAndUri() {
		String actual = generator.encodeMethodAndUri("GET", "/dir/index.html");
		assertEquals("39aff3a2bab6126f332b942af96d3366", actual);
	}

	public void testMD5() {
		String actual = generator.md5("939e7578ed9e3c518a452acee763bce9", "dcd98b7102dd2f0e8b11d0f600bfb0c093", "00000001", "0a4f113b", "auth", "39aff3a2bab6126f332b942af96d3366");
		assertEquals("6629fae49393a05397450978507c4ef1", actual);
	}
}
