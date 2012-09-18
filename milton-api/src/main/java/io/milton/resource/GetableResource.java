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

package io.milton.resource;

import io.milton.http.Auth;
import io.milton.http.Range;
import io.milton.resource.Resource;
import io.milton.http.exceptions.BadRequestException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;

/**
 * webDAV GET and HEAD
 */
public interface GetableResource extends Resource {
    /**
     * Send the resource's content using the given output stream. Implementations
     * should assume that bytes are being physically transmitted and that headers
     * have already been committed, although this might not be the case with
     * all web containers.
     * <P/>
     * This method will be used to serve GET requests, and also to generate
     * content following POST requests (if they have not redirected)
     * <P/>
     * The Range argument is not-null for partial content requests. In this case
     * implementations should (but are not required) to only send the data
     * range requested.
     * <P/>
     * The contentType argument is that which was resolved by negotiation in
     * the getContentType method. HTTP allows a given resource to have multiple
     * representations on the same URL. For example, a data series could be retrieved
     * as a chart as SVG, PNG, JPEG, or as text as CSV or XML. When the user agent
     * requests the resource is specified what content types it can accept. These
     * are matched against those that can be provided by the server and a preferred
     * representation is selected. That contentType is set in the response header
     * and is provided here so that the resource implementation can render itself
     * appropriately.
     *
     * @param out - the output stream to send the content to
     * @param range - null for normal GET's, not null for partial GET's. May be ignored
     * @param params - request parameters
     * @param contentType - the contentType selected by negotiation
     * @throws java.io.IOException - if there is an exception writing content to the output stream. This
     * indicates that the client has disconnected (as frequently occurs with http transfers). DO NOT
     * throw an IOException if there was an internal error generating the response (eg if reading from a database)
     * @throws com.bradmcevoy.http.exceptions.NotAuthorizedException
     */
    public void sendContent( OutputStream out, Range range, Map<String,String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException;

    /** How many seconds to allow the content to be cached for, or null if caching is not allowed
     *
     * The provided auth object allows this method to determine an appropriate caching
     * time depending on authenticated context. For example, in a CMS in might
     * be appropriate to have a short expiry time for logged in users who might
     * be editing content, as opposed to non-logged in users who are just viewing the site.
     */
    Long getMaxAgeSeconds(Auth auth);

    /** 
     * Given a comma separated listed of preferred content types acceptable for a client,
     * return one content type which is the best.
     * <P/>
     * Returns the most preferred  MIME type. E.g. text/html, image/jpeg, etc
     * <P/>
     *  Must be IANA registered
     * <P/>
     *  accepts is the accepts header. Eg: Accept: text/*, text/html, text/html;level=1
     * <P/>
     *  See - http://www.iana.org/assignments/media-types/ for a list of content types
     *  See - http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html for details about the accept header
     * <P/>
     * See here for a fun discussion of using content type and accepts for XHTML - http://stackoverflow.com/questions/348736/is-writing-self-closing-tags-for-elements-not-traditionally-empty-bad-practice
     * <P/>
     *  If you can't handle accepts interpretation, just return a single content type - E.g. text/html
     * <P/>
     * But typically you should do something like this:
     * <PRE>
     *   String mime = ContentTypeUtils.findContentTypes( this.file );
     *   return ContentTypeUtils.findAcceptableContentType( mime, preferredList );
     * </PRE>
     *  @see com.bradmcevoy.common.ContentTypeUtils
     *
     */
    String getContentType(String accepts);

    /** The length of the content in this resource. If unknown return NULL
     */
    Long getContentLength();
    
}
