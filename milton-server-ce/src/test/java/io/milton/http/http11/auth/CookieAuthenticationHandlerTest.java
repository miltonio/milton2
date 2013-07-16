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

import io.milton.common.Utils;
import io.milton.dns.utils.base64;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class CookieAuthenticationHandlerTest extends TestCase {
	public void testEncoding() {
		//CookieAuthenticationHandler c = new CookieAuthenticationHandler(Collections.EMPTY_LIST, null);
		String s = "/users/admin/";
		String encodedUserUrl = base64.toString(s.getBytes(Utils.UTF8));
		encodedUserUrl = Utils.percentEncode(encodedUserUrl);
		System.out.println("encoded=" + encodedUserUrl);
		
		// now unenc
		String s2 = Utils.decodePath(encodedUserUrl);
		System.out.println("decoded1:" + s2);
		byte[] arr = base64.fromString(s2);
		String decoded = new String(arr);		
		System.out.println("decoded2=" + decoded);
	}
}
