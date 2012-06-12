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

package io.milton.http;

import io.milton.common.StringSplitUtils;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds authentication information for a request
 *
 * There are two sets of information:
 *   - that which is present in the request
 *   - that which is determined as part of performing authentication
 *
 * Note that even if authentication fails, this object will still be available
 * in the request - DO NOT USE THE PRESENCE OF THIS OBJECT TO CHECK FOR A VALID LOGIN!!!
 *
 * Instead use the tag property. This will ONLY be not null after a successful
 * authentication
 *
 * @author brad
 */
public class Auth {

    private static final Logger log = LoggerFactory.getLogger( Auth.class );

    /**
     * Holds application specific user data, as returned from the authenticate
     * method on Resource
     *
     * This should be used to test for a valid login.
     */
    private Object tag;

    /**
     * Common HTTP authentication schemes, and some non-http specified but common
     * ones
     */
    public enum Scheme {
        BASIC,
        DIGEST,
        NEGOTIATE,
        FORM,
        SESSION,
		NTLM,
		OAUTH
    };
    private Scheme scheme;
    private String user;
    private String password;

    private String realm;
    private String nonce;
    private String uri;
    private String responseDigest;
    private String qop;
    private String nc;
    private String cnonce;
    private boolean nonceStale; // set by digest auth handler


    public Auth( String sAuth ) {
//        log.debug( "parse: " + sAuth);
        int pos = sAuth.indexOf( " " );
        String schemeCode;
        String enc;
        if( pos >= 0 ) {
            schemeCode = sAuth.substring( 0, pos );
            scheme = Scheme.valueOf( schemeCode.toUpperCase() );
            enc = sAuth.substring( pos + 1 );
        } else {
            // assume basic
            scheme = Scheme.BASIC;
            enc = sAuth;
        }
        if( scheme.equals( Scheme.BASIC ) ) {
            parseBasic( enc );
        } else if( scheme.equals( Scheme.DIGEST ) ) {
            parseDigest( enc );
        }
    }

    public Auth( String user, Object userTag ) {
        this.scheme = Scheme.BASIC;
        this.user = user;
        this.password = null;
        this.tag = userTag;
    }

    public Auth( Scheme scheme, String user, Object userTag ) {
        this.scheme = scheme;
        this.user = user;
        this.password = null;
        this.tag = userTag;
    }


    /**
     *
     * @return - the user property in the request. This MIGHT NOT be an
     * actual user
     */
    public String getUser() {
        return user;
    }

    /**
     * Set after a successful authenticate method with a not-null value
     *
     * The actual value will be application dependent
     */
    public void setTag( Object authTag ) {
        tag = authTag;
    }

    /**
     * Holds application specific user data, as returned from the authenticate
     * method on Resource
     *
     * This should be used to test for a valid login.
     */
    public Object getTag() {
        return tag;
    }

    public String getPassword() {
        return password;
    }

    public Scheme getScheme() {
        return scheme;
    }

    public String getCnonce() {
        return cnonce;
    }

    public String getNc() {
        return nc;
    }

    public String getNonce() {
        return nonce;
    }

    public String getQop() {
        return qop;
    }

    public String getRealm() {
        return realm;
    }

    public String getResponseDigest() {
        return responseDigest;
    }

    public String getUri() {
        return uri;
    }

    public boolean isNonceStale() {
        return nonceStale;
    }

    /**
     * set by digest auth processing. Used to add stale nonce flag to challenge
     * 
     * @param nonceStale
     */
    public void setNonceStale( boolean nonceStale ) {
        this.nonceStale = nonceStale;
    }


    




    private void parseBasic( String enc ) {
        byte[] bytes = Base64.decodeBase64( enc.getBytes() );
        String s = new String( bytes );
        int pos = s.indexOf( ":" );
        if( pos >= 0 ) {
            user = s.substring( 0, pos );
            password = s.substring( pos + 1 );
        } else {
            user = s;
            password = null;
        }
    }

    private void parseDigest( String s ) {
        String[] headerEntries = StringSplitUtils.splitIgnoringQuotes( s, ',' );
        Map headerMap = StringSplitUtils.splitEachArrayElementAndCreateMap( headerEntries, "=", "\"" );

//        log.debug( "headerMap: " + headerMap);

        user = (String) headerMap.get( "username" );
        realm = (String) headerMap.get( "realm" );
        nonce = (String) headerMap.get( "nonce" );
        uri = (String) headerMap.get( "uri" );
        responseDigest = (String) headerMap.get( "response" );
        qop = (String) headerMap.get( "qop" ); // RFC 2617 extension
        nc = (String) headerMap.get( "nc" ); // RFC 2617 extension
        cnonce = (String) headerMap.get( "cnonce" ); // RFC 2617 extension
    }

    @Override
    public String toString() {
        return "scheme: " + scheme + " user:" + user + " tag:" + tag;
    }



}
