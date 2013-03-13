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

import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface Response {

    public final static String HTTP = "text/html";
    public final static String IMAGE_JPG = "image/jpg";
    public final static String MULTIPART = "multipart/form-data";
    public final static String XML = "text/xml; charset=UTF-8";

    public enum ContentType {

        HTTP,
        MULTIPART,
        IMAGE_JPG,
        XML;
    }

    public enum ContentEncoding {

        GZIP( "gzip" );
        public String code;

        ContentEncoding( String code ) {
            this.code = code;
        }
    }

    enum Header {

        CACHE_CONTROL( "Cache-Control" ),
        WWW_AUTHENTICATE( "WWW-Authenticate" ),
        CONTENT_LENGTH( "Content-Length" ),
        CONTENT_TYPE( "Content-Type" ),
        CONTENT_ENCODING( "Content-Encoding" ),
        LOCATION( "Location" ),
        ALLOW( "Allow" ),
        DAV( "DAV" ),
        DATE( "Date" ),  // was all-caps , eg DATE, which is wrong
        LAST_MODIFIED( "Last-Modified" ),
        LOCK_TOKEN( "Lock-Token" ),
        EXPIRES( "Expires" ),
        ETAG( "ETag" ),
        VARY( "Vary" ),
        ACCESS_CONTROL_ALLOW_ORIGIN("Access-Control-Allow-Origin"),
        CONTENT_RANGE( "Content-Range" );
        public String code;

        Header( String code ) {
            this.code = code;
        }
    }

    enum CacheControlResponse {

        PUBLIC( "public" ),
        PRIVATE( "private" ), // [ "=" <"> 1#field-name <"> ] ; Section 14.9.1
        NO_CACHE( "no-cache" ), // [ "=" <"> 1#field-name <"> ]; Section 14.9.1
        NO_STORE( "no-store" ), //                             ; Section 14.9.2
        NO_TRANSFORM( "no-transform" ), //                         ; Section 14.9.5
        MUST_REVALIDATE( "must-revalidate" ), //                     ; Section 14.9.4
        PROXY_REVALIDATE( "proxy-revalidate" ), //                   ; Section 14.9.4
        MAX_AGE( "max-age" ), // "=" delta-seconds            ; Section 14.9.3
        S_MAX_AGE( "s-maxage" ), // "=" delta-seconds           ; Section 14.9.3
        CACHE_EXT( "cache-extension" );  //                       ; Section 14.9.6
        public String code;

        CacheControlResponse( String code ) {
            this.code = code;
        }
    }

    enum Status {

        SC_OK( ResponseStatus.SC_OK, "OK" ),
        SC_CREATED( ResponseStatus.SC_CREATED ),
        SC_ACCEPTED( ResponseStatus.SC_ACCEPTED ),
        SC_NO_CONTENT( ResponseStatus.SC_NO_CONTENT ),
        SC_MULTI_STATUS( 207, "Multi-status" ),
        SC_MOVED_PERMANENTLY( ResponseStatus.SC_MOVED_PERMANENTLY ),
        SC_MOVED_TEMPORARILY( ResponseStatus.SC_MOVED_TEMPORARILY ),
		SC_TEMPORARY_REDIRECT( ResponseStatus.SC_TEMPORARY_REDIRECT ),
        SC_NOT_MODIFIED( ResponseStatus.SC_NOT_MODIFIED ),
        SC_BAD_REQUEST( ResponseStatus.SC_BAD_REQUEST ),
        SC_UNAUTHORIZED( ResponseStatus.SC_UNAUTHORIZED ),
        SC_FORBIDDEN( ResponseStatus.SC_FORBIDDEN ),
        SC_NOT_FOUND( ResponseStatus.SC_NOT_FOUND, "Not Found" ),
        SC_INTERNAL_SERVER_ERROR( ResponseStatus.SC_INTERNAL_SERVER_ERROR ),
        SC_NOT_IMPLEMENTED( ResponseStatus.SC_NOT_IMPLEMENTED ),
        SC_BAD_GATEWAY( ResponseStatus.SC_BAD_GATEWAY ),
        SC_SERVICE_UNAVAILABLE( ResponseStatus.SC_SERVICE_UNAVAILABLE ),
        SC_PARTIAL_CONTENT( ResponseStatus.SC_PARTIAL_CONTENT ),
        SC_CONTINUE( 100 ),
        SC_METHOD_NOT_ALLOWED( 405 ),
        SC_CONFLICT( 409 ),
        SC_PRECONDITION_FAILED( 412 ),
        SC_REQUEST_TOO_LONG( 413 ),
        SC_UNSUPPORTED_MEDIA_TYPE( 415 ),
        SC_EXPECTATION_FAILED( ResponseStatus.SC_EXPECTATION_FAILED ), // 417
        SC_UNPROCESSABLE_ENTITY( 418 ),
        SC_INSUFFICIENT_STORAGE( 507 ),
        SC_METHOD_FAILURE( 420 ),
        SC_LOCKED( 423 );
        public int code;
		public String text;

        Status( int code, String text ) {
            this.code = code;
			this.text = text;
        }
        Status( int code) {
            this.code = code;
			this.text = null;
        }
        @Override
        public String toString() {
			if( text != null ) {
				return "HTTP/1.1 " + code + " " + text;
			} else {
				return "HTTP/1.1 " + code;
			}
        }

        public Status fromCode( int i ) {
            for( Status s : this.values() ) {
                if( s.code == i ) return s;
            }
            return null;
        }
    }

    public interface Entity {
        void write(Response response, OutputStream outputStream) throws Exception;
    }

    public Response.Status getStatus();

    public Map<String, String> getHeaders();

    /**
     * 
     * @return - the content length which might have been set by a handler, or null
     * if none has been set
     */
    public Long getContentLength();

    public void setContentEncodingHeader( ContentEncoding encoding );

    public void setExpiresHeader( Date expiresAt );

    public void setLockTokenHeader( String tokenId );

    /**
     * Must set multiple Authenticate headers, one for each challenge
     *
     * This will usually be one each for basic, digest, ntlm, etc
     *
     * Note that order might be significant to some clients, so should be
     * preserved. ie list item zero should be first.
     *
     * @param challenges - a list of http authentication challenges
     */
    void setAuthenticateHeader( List<String> challenges );

    void setStatus( Status status );

    void setEtag( String uniqueId );

    void setContentRangeHeader( long start, long finish, Long totalLength );

    void setContentLengthHeader( Long totalLength );

    void setContentTypeHeader( String string );

    String getContentTypeHeader();

    Entity getEntity();

    void setEntity(Entity entity);

    /**
     * Set the cache control header to allow the resource to be cached
     * for the given number of seconds
     *
     * @param deltaSeconds - must not be null
     */
    void setCacheControlMaxAgeHeader( Long deltaSeconds );

    void setCacheControlPrivateMaxAgeHeader( Long deltaSeconds );

    /**
     * Set the cache control header to indicate that the resource should not
     * be cached
     */
    void setCacheControlNoCacheHeader();

    void setLastModifiedHeader( Date date );

    void setDavHeader( String string );

    void setNonStandardHeader( String code, String value );

    String getNonStandardHeader( String code );

    void setAllowHeader( List<String> methodsAllowed );

    OutputStream getOutputStream();

    void setLocationHeader( String redirectUrl );

    /**
     * Sets the Vary response header, necessary for response compression
     * @param string
     */
    void setVaryHeader( String string );

    void setDateHeader( Date date );

    /**
     * Used for CORS responses
     * 
     * @return 
     */
    String getAccessControlAllowOrigin();
    
    void setAccessControlAllowOrigin(String s);
    
    /**
     * Called to indicate that the request is completed. Some response implementations
     * might choose to close the http connection, while others which implement pipelining
     * might not
     */
    void close();

    /**
     * Called by milton when an error occurs. The underlying HTTP provider should
     * choose to close the HTTP connection, because the amount of data already sent
     * might not be consistent with the content length header which may have already
     * been sent
     */
    void sendError(Status status, String message);
    
    /**
     * Will set the status to moved_temporaruly and set the location header
     * to the given url
     * 
     * @param url
     */
    void sendRedirect( String url );

    /**
     * The <code>setCookie</code> method is used to set a cookie value
     * with the cookie name. This will add a cookie to the response
     * stored under the name of the cookie, when this is committed it
     * will be added as a Set-Cookie header to the resulting response.
     *
     * @param cookie this is the cookie to be added to the response
     *
     * @return returns the cookie that has been set in the response
     */
    Cookie setCookie( Cookie cookie );

    /**
     * The <code>setCookie</code> method is used to set a cookie value
     * with the cookie name. This will add a cookie to the response
     * stored under the name of the cookie, when this is committed it
     * will be added as a Set-Cookie header to the resulting response.
     * This is a convenience method that avoids cookie creation.
     *
     * @param name this is the cookie to be added to the response
     * @param value this is the cookie value that is to be used
     *
     * @return returns the cookie that has been set in the response
     */
    Cookie setCookie( String name, String value );
}
