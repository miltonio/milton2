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

package io.milton.http.webdav;

import io.milton.http.HrefStatus;
import io.milton.http.Request;
import io.milton.resource.Resource;
import io.milton.http.Response;
import io.milton.http.http11.Http11ResponseHandler;
import io.milton.http.quota.StorageChecker.StorageErrorReason;
import java.util.List;

/**
 *
 * @author brad
 */
public interface WebDavResponseHandler extends Http11ResponseHandler{
    void responseMultiStatus(Resource resource, Response response, Request request, List<HrefStatus> statii);

    /**
     * Generate the response for a PROPFIND or a PROPPATCH
     *
     * @param propFindResponses
     * @param response
     * @param request
     * @param r - the resource
     */
    void respondPropFind( List<PropFindResponse> propFindResponses, Response response, Request request, Resource r );

    void respondInsufficientStorage( Request request, Response response, StorageErrorReason storageErrorReason );

    void respondLocked( Request request, Response response, Resource existingResource );

    /**
     * Generate a 412 response, 
     * 
     * @param request
     * @param response
     * @param resource
     */
    void respondPreconditionFailed( Request request, Response response, Resource resource );
}
