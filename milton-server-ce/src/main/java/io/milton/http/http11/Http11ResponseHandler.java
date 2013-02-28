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

package io.milton.http.http11;

import io.milton.resource.GetableResource;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.resource.Resource;
import io.milton.http.Response;
import io.milton.http.Response.Status;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import java.util.List;
import java.util.Map;

/**
 *  The ResponseHandler should handle all responses back to the client.
 *
 *  Methods are provided for each significant response circumstance with respect
 *  to Milton.
 *
 *  The intention is that implementations may be provided or customised to support
 *  per implementation requirements for client compatibility.
 *
 *  In other words, hacks to support particular client programs should be implemented
 *  here
 *
 *  Extends ETagGenerator to facillitate wrapping, although generatlly it will
 *  contain an instance and delegate to it.
 */
public interface Http11ResponseHandler extends ETagGenerator {
    /**
     * Invoked when an operation is successful, but there is no content, and
     * there is nothing more specific to return (E.g. created)
     *
     * For example, as a result of a PUT when a resouce has been updated)
     *
     * @param resource
     * @param response
     * @param request
     */
    void respondNoContent(Resource resource, Response response,Request request);
    void respondContent(Resource resource, Response response, Request request, Map<String,String> params) throws NotAuthorizedException, BadRequestException, NotFoundException;
    void respondPartialContent(GetableResource resource, Response response, Request request, Map<String,String> params, Range range) throws NotAuthorizedException, BadRequestException, NotFoundException;
    void respondCreated(Resource resource, Response response, Request request);
    void respondUnauthorised(Resource resource, Response response, Request request);
    void respondMethodNotImplemented(Resource resource, Response response, Request request);
    void respondMethodNotAllowed(Resource res, Response response, Request request);
    void respondConflict(Resource resource, Response response, Request request, String message);
    void respondRedirect(Response response, Request request, String redirectUrl);
    void respondNotModified(GetableResource resource, Response response, Request request);
    void respondNotFound(Response response, Request request);
    void respondWithOptions(Resource resource, Response response,Request request, List<String> methodsAllowed);

    /**
     * Generate a HEAD response
     *
     * @param resource
     * @param response
     * @param request
     */
    void respondHead( Resource resource, Response response, Request request );

    /**
     * Response with a 417
     */
    void respondExpectationFailed(Response response, Request request);

    /**
     * Respond with a 400 status
     *
     * @param resource
     * @param response
     * @param request
     * @param params
     */
    void respondBadRequest( Resource resource, Response response, Request request);


    /**
     * Respond with a 403 status - forbidden
     *
     * @param resource
     * @param response
     * @param request
     * @param params
     */
    void respondForbidden( Resource resource, Response response, Request request);


    /**
     * Called when a delete has failed, including the failure status.
     *
     * Note that webdav implementations will respond with a multisttus, while
     * http 1.1 implementations will simply set the response status
     *
     * @param request
     * @param response
     * @param resource - the resource which could not be deleted
     * @param status - the status which has caused the delete to fail.
     */
    void respondDeleteFailed( Request request, Response response, Resource resource, Status status );

    /**
     * Usually a 500 error. Some error occured processing the request. Note
     * that you might not be able to assume that this will generate all 500
     * errors since a runtime exception might result in code outside of milton's
     * control generating the 500 response.
     * 
     * @param request
     * @param response
     * @param reason
     */
    void respondServerError( Request request, Response response, String reason);


    /**
     * Generate a 412 response, 
     * 
     * @param request
     * @param response
     * @param resource
     */
    void respondPreconditionFailed( Request request, Response response, Resource resource );      
}
