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

package io.milton.http.http11.auth;

import io.milton.http.Auth;
import io.milton.http.Request.Method;
import io.milton.http.http11.auth.NonceProvider.NonceValidity;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 *
 */
public class DigestHelper {

    private static final Logger log = LoggerFactory.getLogger(DigestHelper.class);

    private final NonceProvider nonceProvider;

    public DigestHelper(NonceProvider nonceProvider) {
        this.nonceProvider = nonceProvider;
    }

    public DigestResponse calculateResponse(Auth auth, String expectedRealm, Method method) {
        try {
            // Check all required parameters were supplied (ie RFC 2069)
            if ((auth.getUser() == null) || (auth.getRealm() == null) || (auth.getNonce() == null) || (auth.getUri() == null)) {
                log.warn("missing params");
                return null;
            }

            // Check all required parameters for an "auth" qop were supplied (ie RFC 2617)
            Long nc;
            if ("auth".equals(auth.getQop())) {
                if ((auth.getNc() == null) || (auth.getCnonce() == null)) {
                    log.warn("missing params: nc and/or cnonce");
                    return null;
                }
                nc = Long.parseLong(auth.getNc(), 16); // the nonce-count. hex value, must always increase
            } else {
                nc = null;
            }

            // Check realm name equals what we expected
            if (expectedRealm == null) throw new IllegalStateException("realm is null");
            if (!expectedRealm.equals(auth.getRealm())) {
                log.warn("incorrect realm: resource: " + expectedRealm + " given: " + auth.getRealm());
                return null;
            }

            // Check nonce was a Base64 encoded (as sent by DigestProcessingFilterEntryPoint)
            if (!Base64.isBase64(auth.getNonce().getBytes("UTF-8"))) {
                log.warn("nonce not base64 encoded");
                return null;
            }

            log.debug("nc: " + auth.getNc());


            // Decode nonce from Base64
            // format of nonce is
            //   base64(expirationTime + "" + md5Hex(expirationTime + "" + key))
            String plainTextNonce = new String(Base64.decodeBase64(auth.getNonce().getBytes("UTF-8")));
            NonceValidity validity = nonceProvider.getNonceValidity(plainTextNonce, nc, auth.getUser());
            //        if( NonceValidity.INVALID.equals( validity ) ) {
            //            log.debug( "invalid nonce: " + plainTextNonce );
            //            return null;
            //        } else if( NonceValidity.EXPIRED.equals( validity ) ) {
            //            log.debug( "expired nonce: " + plainTextNonce );
            //            // make this known so that we can add stale field to challenge
            //            auth.setNonceStale( true );
            //            return null;
            //        }

            return toDigestResponse(auth, method);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getChallenge(String nonceValue, Auth auth, String actualRealm) {
        try {
            String nonceValueBase64 = new String(Base64.encodeBase64(nonceValue.getBytes("UTF-8")));

            // qop is quality of protection, as defined by RFC 2617.
            // we do not use opaque due to IE violation of RFC 2617 in not
            // representing opaque on subsequent requests in same session.
            String authenticateHeader = "Digest realm=\"" + actualRealm
                    + "\", " + "qop=\"auth\", nonce=\"" + nonceValueBase64
                    + "\"";

            if (auth != null) {
                if (auth.isNonceStale()) {
                    authenticateHeader = authenticateHeader
                            + ", stale=\"true\"";
                }
            }

            return authenticateHeader;
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }


    private DigestResponse toDigestResponse(Auth auth, Method m) {
        return new DigestResponse(
                m,
                auth.getUser(),
                auth.getRealm(),
                auth.getNonce(),
                auth.getUri(),
                auth.getResponseDigest(),
                auth.getQop(),
                auth.getNc(),
                auth.getCnonce());

    }
}
