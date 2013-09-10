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

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author brad
 */
public class HmacUtils {

	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	
	public static String calcShaHash(String data, String key) {		
		String result = null;
		try {
			Key signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);
			byte[] rawHmac = mac.doFinal(data.getBytes());
			result = Base64.encodeBase64URLSafeString(rawHmac);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(HMAC_SHA1_ALGORITHM, e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException(HMAC_SHA1_ALGORITHM, e);
		} catch (IllegalStateException e) {
			throw new RuntimeException(HMAC_SHA1_ALGORITHM, e);
		}

		return result;
	}
	

}
