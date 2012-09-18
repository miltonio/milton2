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


package io.milton.http.webdav;

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

public class UnlockHandler implements ExistingEntityHandler {

    private Logger log = LoggerFactory.getLogger(UnlockHandler.class);

    private final ResourceHandlerHelper resourceHandlerHelper;

    private final WebDavResponseHandler responseHandler;

    public UnlockHandler( ResourceHandlerHelper resourceHandlerHelper, WebDavResponseHandler responseHandler ) {
        this.resourceHandlerHelper = resourceHandlerHelper;
        this.responseHandler = responseHandler;
    }


    public void process( HttpManager httpManager, Request request, Response response ) throws ConflictException, NotAuthorizedException, BadRequestException {
        resourceHandlerHelper.process( httpManager, request, response, this );
    }

    public void processResource( HttpManager manager, Request request, Response response, Resource r ) throws NotAuthorizedException, ConflictException, BadRequestException {
        resourceHandlerHelper.processResource( manager, request, response, r, this );
    }

    public void processExistingResource( HttpManager manager, Request request, Response response, Resource resource ) throws NotAuthorizedException, BadRequestException, ConflictException {
        LockableResource r = (LockableResource) resource;
        String sToken = request.getLockTokenHeader();        
        sToken = LockHandler.parseToken(sToken);
        
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
    
    public String[] getMethods() {
        return new String[]{Method.UNLOCK.code};
    }
    
    @Override
    public boolean isCompatible( Resource handler ) {
        return handler instanceof LockableResource;
    }

    
}
