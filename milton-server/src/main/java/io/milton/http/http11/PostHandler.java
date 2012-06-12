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
    private final HandlerHelper handlerHelper;
    private final ResourceHandlerHelper resourceHandlerHelper;
    

    public PostHandler( Http11ResponseHandler responseHandler, HandlerHelper handlerHelper ) {
        this.responseHandler = responseHandler;
        this.handlerHelper = handlerHelper;
        this.resourceHandlerHelper = new ResourceHandlerHelper( handlerHelper, responseHandler );
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
