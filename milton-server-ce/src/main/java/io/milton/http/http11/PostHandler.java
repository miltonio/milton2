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

import io.milton.http.ResourceHandlerHelper;
import io.milton.http.Response;
import io.milton.resource.Resource;
import io.milton.http.HandlerHelper;
import io.milton.http.ExistingEntityHandler;
import io.milton.http.HttpManager;
import io.milton.resource.PostableResource;
import io.milton.http.Request;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;

import io.milton.http.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostHandler implements ExistingEntityHandler {

    private Logger log = LoggerFactory.getLogger( PostHandler.class );
    private final Http11ResponseHandler responseHandler;
    private final ResourceHandlerHelper resourceHandlerHelper;
    

    public PostHandler( Http11ResponseHandler responseHandler, ResourceHandlerHelper resourceHandlerHelper ) {
        this.responseHandler = responseHandler;
        this.resourceHandlerHelper = resourceHandlerHelper;
    }

    @Override
    public String[] getMethods() {
        return new String[]{Request.Method.POST.code};
    }

    @Override
    public boolean isCompatible( Resource handler ) {
        return ( handler instanceof PostableResource );
    }

    @Override
    public void process( HttpManager manager, Request request, Response response ) throws NotAuthorizedException, ConflictException, BadRequestException {
        this.resourceHandlerHelper.process( manager, request, response, this );
    }

    @Override
    public void processResource( HttpManager manager, Request request, Response response, Resource r ) throws NotAuthorizedException, ConflictException, BadRequestException {
        manager.onPost( request, response, r, request.getParams(), request.getFiles() );
        resourceHandlerHelper.processResource( manager, request, response, r, this, true, request.getParams(), request.getFiles() );
    }

    @Override
    public void processExistingResource( HttpManager manager, Request request, Response response, Resource resource ) throws NotAuthorizedException, BadRequestException, ConflictException, NotFoundException {
        PostableResource r = (PostableResource) resource;
        for(CustomPostHandler h : manager.getCustomPostHandlers()) {
            if(h.supports(resource, request)) {
                log.trace("Found CustomPostHandler supporting this resource and request");
                h.process(resource, request, response);
                return ;
            }
        }
        String url = r.processForm( request.getParams(), request.getFiles() );
        if( url != null ) {
            log.debug("redirect: " + url );
            responseHandler.respondRedirect( response, request, url );
        } else {
            log.debug("respond with content");
            responseHandler.respondContent( resource, response, request, request.getParams() );
        }
    }    
}
