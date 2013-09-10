/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.milton.http.http11.auth;

import io.milton.http.AbstractRequest;
import io.milton.http.Auth;
import io.milton.http.Cookie;
import io.milton.http.FileItem;
import io.milton.http.RequestParseException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class CookieAuthenticationHandlerTest extends TestCase {
//	public void testEncoding() {
//		//CookieAuthenticationHandler c = new CookieAuthenticationHandler(Collections.EMPTY_LIST, null);
//		String s = "/users/admin/";
//		String encodedUserUrl = base64.toString(s.getBytes(Utils.UTF8));
//		encodedUserUrl = Utils.percentEncode(encodedUserUrl);
//		System.out.println("encoded=" + encodedUserUrl);
//		
//		// now unenc
//		String s2 = Utils.decodePath(encodedUserUrl);
//		System.out.println("decoded1:" + s2);
//		byte[] arr = base64.fromString(s2);
//		String decoded = new String(arr);		
//		System.out.println("decoded2=" + decoded);
//	}
	
	private List<String> keys = Arrays.asList("abc");
	
	private SimpleMemoryNonceProvider nonceProvider = new SimpleMemoryNonceProvider(100);
	
	private static final String nonce = "cbc86104-aaf4-455d-8937-b095a21481cf";

	public CookieAuthenticationHandlerTest() {
		UUID id = UUID.fromString(nonce);
		nonceProvider.getNonces().put(id, new Nonce(id, new Date()) );
	}
	
	
	
	public void test_GenerateHash() {
		CookieAuthenticationHandler c = new CookieAuthenticationHandler(nonceProvider, Collections.EMPTY_LIST, null, keys);
		String s = "/users/Reviewer/";
		MockRequest request = new MockRequest();
		String hash = c.getUrlSigningHash(s, request);
		System.out.println("hash=" + hash);
	}
	
	public void test_ValidatePlain() {
		CookieAuthenticationHandler c = new CookieAuthenticationHandler(nonceProvider, Collections.EMPTY_LIST, null, keys);
		String s = "/users/Reviewer/";
		String hash = nonce + ":aDIEijllPTjBcj7R4wskncoaZ3s";
		MockRequest request = new MockRequest();
		request.params.put(c.getCookieNameUserUrl(), s);
		request.params.put(c.getCookieNameUserUrlHash(), hash);
		String validatedUrl = c.getUserUrl(request);
		assertNotNull(validatedUrl);
		assertEquals(s, validatedUrl);
	}	
	
	public void xtest_ValidateBase64() {
		CookieAuthenticationHandler c = new CookieAuthenticationHandler(nonceProvider, Collections.EMPTY_LIST, null, keys);
		String s = "/users/Reviewer/";
		String encodedUserUrl = c.encodeUserUrl(s);
		assertTrue(encodedUserUrl.startsWith("b64"));
		String hash = "4114ce15b1843401065f9ce06ee2a635";
		MockRequest request = new MockRequest();
		request.params.put(c.getCookieNameUserUrl(), encodedUserUrl);
		request.params.put(c.getCookieNameUserUrlHash(), hash);
		String validatedUrl = c.getUserUrl(request);
		assertNotNull(validatedUrl);
		assertEquals(s, validatedUrl);
	}		
	
	public class MockRequest extends AbstractRequest {
		private final Map<String,Cookie> cookies = new HashMap<String, Cookie>();
		private final Map<String,String> headers = new HashMap<String, String>();
		private final Map<String,String> params = new HashMap<String, String>();
		private Auth auth;

		@Override
		public String getRequestHeader(Header header) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Map<String, String> getHeaders() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public String getFromAddress() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Method getMethod() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Auth getAuthorization() {
			return auth;
		}

		@Override
		public void setAuthorization(Auth auth) {
			this.auth = auth;
		}

		@Override
		public String getAbsoluteUrl() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public InputStream getInputStream() throws IOException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void parseRequestParameters(Map<String, String> params, Map<String, FileItem> files) throws RequestParseException {
			
		}

		@Override
		public Map<String, String> getParams() {
			return params;
		}
		
		

		@Override
		public Cookie getCookie(String name) {
			return cookies.get(name);
		}

		@Override
		public List<Cookie> getCookies() {
			return new ArrayList<Cookie>(cookies.values());
		}

		@Override
		public String getRemoteAddr() {
			return null;
		}
		
	}
}
