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

import io.milton.http.Auth;
import io.milton.http.Request.Method;
import io.milton.http.http11.auth.NonceProvider.NonceValidity;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DigestHelper {

    private static final Logger log = LoggerFactory.getLogger( DigestHelper.class );

    private final NonceProvider nonceProvider;

    public DigestHelper(NonceProvider nonceProvider) {
        this.nonceProvider = nonceProvider;
    }
                
    public DigestResponse calculateResponse( Auth auth, String expectedRealm, Method method ) {
		try {
			// Check all required parameters were supplied (ie RFC 2069)
			if( ( auth.getUser() == null ) || ( auth.getRealm() == null ) || ( auth.getNonce() == null ) || ( auth.getUri() == null ) ) {
				log.warn( "missing params" );
				return null;
			}

			// Check all required parameters for an "auth" qop were supplied (ie RFC 2617)
			Long nc;
			if( "auth".equals( auth.getQop() ) ) {
				if( ( auth.getNc() == null ) || ( auth.getCnonce() == null ) ) {
					log.warn( "missing params: nc and/or cnonce" );
					return null;
				}
				nc = Long.parseLong( auth.getNc(), 16); // the nonce-count. hex value, must always increase
			} else {
				nc = null;
			}

			// Check realm name equals what we expected
			if( expectedRealm == null ) throw new IllegalStateException( "realm is null");
			if( !expectedRealm.equals( auth.getRealm() ) ) {
				log.warn( "incorrect realm: resource: " + expectedRealm + " given: " + auth.getRealm() );
				return null;
			}

			// Check nonce was a Base64 encoded (as sent by DigestProcessingFilterEntryPoint)
			if( !Base64.isArrayByteBase64( auth.getNonce().getBytes("UTF-8") ) ) {
				log.warn( "nonce not base64 encoded" );
				return null;
			}

			log.debug( "nc: " + auth.getNc());


			// Decode nonce from Base64
			// format of nonce is
			//   base64(expirationTime + "" + md5Hex(expirationTime + "" + key))
			String plainTextNonce = new String( Base64.decodeBase64( auth.getNonce().getBytes("UTF-8") ) );
			NonceValidity validity = nonceProvider.getNonceValidity( plainTextNonce, nc );
	//        if( NonceValidity.INVALID.equals( validity ) ) {
	//            log.debug( "invalid nonce: " + plainTextNonce );
	//            return null;
	//        } else if( NonceValidity.EXPIRED.equals( validity ) ) {
	//            log.debug( "expired nonce: " + plainTextNonce );
	//            // make this known so that we can add stale field to challenge
	//            auth.setNonceStale( true );
	//            return null;
	//        }

			DigestResponse resp = toDigestResponse( auth, method );
			return resp;
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
    }

    public String getChallenge( String nonceValue, Auth auth, String actualRealm ) {
		try {
			String nonceValueBase64 = new String( Base64.encodeBase64( nonceValue.getBytes("UTF-8") ) );

			// qop is quality of protection, as defined by RFC 2617.
			// we do not use opaque due to IE violation of RFC 2617 in not
			// representing opaque on subsequent requests in same session.
			String authenticateHeader = "Digest realm=\"" + actualRealm
				+ "\", " + "qop=\"auth\", nonce=\"" + nonceValueBase64
				+ "\"";

			if( auth != null ) {
				if( auth.isNonceStale() ) {
					authenticateHeader = authenticateHeader
						+ ", stale=\"true\"";
				}
			}

			return authenticateHeader;
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
    }


    private DigestResponse toDigestResponse( Auth auth, Method m ) {
        DigestResponse dr = new DigestResponse(
            m,
            auth.getUser(),
            auth.getRealm(),
            auth.getNonce(),
            auth.getUri(),
            auth.getResponseDigest(),
            auth.getQop(),
            auth.getNc(),
            auth.getCnonce() );
        return dr;

    }
}
