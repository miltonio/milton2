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


package io.milton.http.webdav2;

import io.milton.http.ExistingEntityHandler;
import io.milton.http.HttpManager;
import io.milton.resource.Resource;
import io.milton.http.Response;
import io.milton.resource.LockableResource;
import io.milton.http.Request;
import io.milton.http.ResourceHandlerHelper;
import io.milton.http.exceptions.PreConditionFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.webdav.WebDavResponseHandler;
import io.milton.webdav.utils.LockUtils;

public class UnlockHandler implements ExistingEntityHandler {

    private Logger log = LoggerFactory.getLogger(UnlockHandler.class);

    private final ResourceHandlerHelper resourceHandlerHelper;

    private final WebDavResponseHandler responseHandler;

    public UnlockHandler( ResourceHandlerHelper resourceHandlerHelper, WebDavResponseHandler responseHandler ) {
        this.resourceHandlerHelper = resourceHandlerHelper;
        this.responseHandler = responseHandler;
    }


    @Override
    public void process( HttpManager httpManager, Request request, Response response ) throws ConflictException, NotAuthorizedException, BadRequestException {
        resourceHandlerHelper.process( httpManager, request, response, this );
    }

    @Override
    public void processResource( HttpManager manager, Request request, Response response, Resource r ) throws NotAuthorizedException, ConflictException, BadRequestException {
        resourceHandlerHelper.processResource( manager, request, response, r, this );
    }

    @Override
    public void processExistingResource( HttpManager manager, Request request, Response response, Resource resource ) throws NotAuthorizedException, BadRequestException, ConflictException {
        LockableResource r = (LockableResource) resource;
        String sToken = request.getLockTokenHeader();        
        sToken = LockUtils.parse(sToken);
        
        // this should be checked in processResource now
        
//       	if( r.getCurrentLock() != null &&
//       			!sToken.equals( r.getCurrentLock().tokenId) &&
//       			isLockedOut( request, resource ))
//    	{
//       		//Should this be unlocked easily? With other tokens?
//    		response.setStatus(Status.SC_LOCKED);
//    	    log.info("cant unlock with token: " + sToken);
//    		return;
//    	}

        
        log.debug("unlocking token: " + sToken);
        try {
            r.unlock( sToken );
            responseHandler.respondNoContent( resource, response, request );
        } catch( PreConditionFailedException ex ) {
            responseHandler.respondPreconditionFailed( request, response, resource );
        }
    }
    
    @Override
    public String[] getMethods() {
        return new String[]{Method.UNLOCK.code};
    }
    
    @Override
    public boolean isCompatible( Resource handler ) {
        return handler instanceof LockableResource;
    }

    
}
