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

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface Request {

    enum Depth {

        ZERO,
        ONE,
        INFINITY
    }

    enum CacheControlRequest {

        NO_CACHE("no-cache"),
        NO_STORE("no-store"),
        MAX_AGE("max-age"), // =delta-seconds
        MAX_STALE("max-stale"), // =delta-seconds
        MIN_FRESH("min-fresh"), // =delta-seconds
        NO_TRANSFORM("no-transform"),
        ONLY_IF_CACHED("only-if-cached"),
        CACHE_EXT("cache-extension");
        public String code;

        CacheControlRequest(String code) {
            this.code = code;
        }
    }

    enum Header {

        CACHE_CONTROL("Cache-Control"),
        WWW_AUTHENTICATE("WWW-Authenticate"),
        IF("If"),
        IF_RANGE("If-Range"),
        IF_MATCH("If-Match"),
        IF_NONE_MATCH("If-None-Match"),
        IF_MODIFIED("If-Modified-Since"),
        IF_NOT_MODIFIED("If-Unmodified-Since"),
        CONTENT_LENGTH("Content-Length"),
        CONTENT_TYPE("Content-Type"),
        CONTENT_RANGE("Content-Range"),
        DEPTH("Depth"),
        HOST("Host"),
        DESTINATION("Destination"),
        REFERER("Referer"),
        ACCEPT("Accept"),
        ACCEPT_LANGUAGE("Accept-Language"),
        RANGE("Range"),
        ACCEPT_ENCODING("Accept-Encoding"),
        TIMEOUT("Timeout"),
        LOCK_TOKEN("Lock-Token"),
        EXPECT("Expect"),
        OVERWRITE("Overwrite"),
        ORIGIN("Origin"),
        USER_AGENT("User-Agent"),
        /**
         * For compatibility with macOS finder from 10.5.3
         */
        X_EXPECTED_ENTITY_LENGTH("X-Expected-Entity-Length"),
        AUTHORIZATION("Authorization");
        public String code;

        Header(String code) {
            this.code = code;
        }
    }

    enum Method {

        HEAD("HEAD", false),
        PROPFIND("PROPFIND", false),
        PROPPATCH("PROPPATCH", true),
        MKCOL("MKCOL", true),
        MKCALENDAR("MKCALENDAR", true),
        COPY("COPY", true),
        MOVE("MOVE", true),
        LOCK("LOCK", true),
        UNLOCK("UNLOCK", true),
        DELETE("DELETE", true),
        GET("GET", false),
        OPTIONS("OPTIONS", false),
        POST("POST", true),
        PUT("PUT", true),
        TRACE("TRACE", false),
        ACL("ACL", true),
        CONNECT("CONNECT", true),
        REPORT("REPORT", false);
        public String code;
        public boolean isWrite;

        Method(String code, boolean isWrite) {
            this.code = code;
            this.isWrite = isWrite;
        }
    };

    Map<String, String> getHeaders();

    /**
     * equivalent to Servlet Request getRemoteHost
     * 
     * Returns the fully qualified name of the client or the last proxy that sent the request.
     * 
     * @return 
     */
    String getFromAddress();

    String getLockTokenHeader();

    String getRequestHeader(Request.Header header);

    Method getMethod();

    Auth getAuthorization();

    /**
     * Maybe called by the milton framework after successful non-http
     * authentication
     *
     * @param auth - the new auth object
     */
    void setAuthorization(Auth auth);

    String getRefererHeader();

    String getTimeoutHeader();

    String getIfHeader();

    String getIfRangeHeader();
    
    String getIfMatchHeader();

    String getIfNoneMatchHeader();

    Date getIfModifiedHeader();

    int getDepthHeader();

    /**
     * Return the complete URL, including protocol, host and port (if specified)
     * and path
     */
    String getAbsoluteUrl();

    /**
     * Return the path portion of the url. This is everything following the host
     * and port. Will always begin with a leading slash
     */
    String getAbsolutePath();

    String getHostHeader();

    String getDestinationHeader();

    String getExpectHeader();

    InputStream getInputStream() throws IOException;

    void parseRequestParameters(Map<String, String> params, Map<String, FileItem> files) throws RequestParseException;

    String getContentTypeHeader();

    Long getContentLengthHeader();

    String getAcceptHeader();

    String getAcceptEncodingHeader();

    /**
     * Get the user-agents preferred languages. 
     * 
     * Eg: en-ca,en;q=0.8,en-us;q=0.6,de-de;q=0.4,de;q=0.2
     * 
     * 
     * @return 
     */
    String getAcceptLanguage();
    
    /**
     *
     * @return a range header, for partial gets
     */
    String getRangeHeader();

    /**
     * Used for partial PUTs
     *
     * @return
     */
    String getContentRangeHeader();

    /**
     * Used for MOVE and COPY methods. If true it indicates that any existing
     * resource should be deleted before the move.
     *
     * @return - null if no value, true indicates that any existing resource
     * should be deleted
     */
    Boolean getOverwriteHeader();

    
    String getOriginHeader();
    
    /**
     *
     * @return - the user agent header field
     */
    String getUserAgentHeader();

    /**
     * Return a writable map of arbitrary values to be associated with the
     * request
     *
     * @return a writable map of arbitrary values to be associated with the
     * request
     */
    Map<String, Object> getAttributes();

    /**
     * Note to implementors: the parameters will be created by the core handler
     * classes and added to the attributes map. If you're extending
     * AbstractRequest this method will already be implemented for you by
     * returning that attribute
     *
     * If you are not extending AbstractRequest you should implement this as:
     *
     * return attributes.get( "_params");
     *
     * @return - map of querystring or POST parameters, keyed by name
     */
    Map<String, String> getParams();

    /**
     * Note to implementors: the parameters will be created by the core handler
     * classes and added to the attributes map. If you're extending
     * AbstractRequest this method will already be implemented for you by
     * returning that attribute
     *
     * If you are not extending AbstractRequest you should return implement this
     * as: return attributes.get( "_files");
     *
     * @return - a map of files from an upload request, keyed by file name
     */
    Map<String, FileItem> getFiles();

    /**
     * This is used to acquire a cookie using the name of that cookie. If the
     * cookie exists within the HTTP header then it is returned as a
     * <code>Cookie</code> object. Otherwise this method will return null. Each
     * cookie object will contain the name, value and path of the cookie as well
     * as the optional domain part.
     *
     * @param name this is the name of the cookie object to acquire
     *
     * @return this returns a cookie object from the header or null
     */
    Cookie getCookie(String name);

    /**
     * This is used to acquire all cookies that were sent in the header. If any
     * cookies exists within the HTTP header they are returned as
     * <code>Cookie</code> objects. Otherwise this method will an empty list.
     * Each cookie object will contain the name, value and path of the cookie as
     * well as the optional domain part.
     *
     * @return this returns all cookie objects from the HTTP header
     */
    List<Cookie> getCookies();

    /**
     * Returns the IP of the remote client. This attempts to find the originating
     * client, ie not the last proxy, by using X-Forwarded-For etc
     *
     * @return
     */
    String getRemoteAddr();
    
    /**
     * Use the Accept-Language header to derive a java Locale
     * 
     * @return 
     */
    Locale getLocale();
    
}
