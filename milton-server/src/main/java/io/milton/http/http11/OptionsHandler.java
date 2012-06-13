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

import io.milton.http.HttpManager;
import io.milton.resource.Resource;
import io.milton.http.Response;
import io.milton.http.ResourceHandler;
import io.milton.http.Handler;
import io.milton.http.HandlerHelper;
import io.milton.http.Request;
import io.milton.http.ResourceHandlerHelper;
import java.util.ArrayList;
import java.util.List;

import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support the OPTIONS http method.
 *
 * Note that windows 7 appears to require un-authenticated access to OPTIONS
 * requests, so this handler now supports configurable selection of allowing
 * un-authenticated access.
 *
 * @author brad
 */
public class OptionsHandler implements ResourceHandler {

    private static final Logger log = LoggerFactory.getLogger( OptionsHandler.class );
    private final Http11ResponseHandler responseHandler;
    private final HandlerHelper handlerHelper;
    private final ResourceHandlerHelper resourceHandlerHelper;
    private boolean enableAuthorisation;

    /**
     * Creates an OptionHandler with no authorisation
     * 
     * @param responseHandler
     */
    public OptionsHandler( Http11ResponseHandler responseHandler, ResourceHandlerHelper resourceHandlerHelper, boolean enableAuthorisation ) {
        this.responseHandler = responseHandler;
        this.handlerHelper = null;
        this.resourceHandlerHelper = resourceHandlerHelper;
        this.enableAuthorisation = false;
		this.enableAuthorisation = enableAuthorisation;
    }


    @Override
    public void process( HttpManager manager, Request request, Response response ) throws NotAuthorizedException, ConflictException, BadRequestException {
        resourceHandlerHelper.process( manager, request, response, this );
    }

	@Override
    public void processResource( HttpManager manager, Request request, Response response, Resource resource ) throws NotAuthorizedException, ConflictException, BadRequestException {
        long t = System.currentTimeMillis();
        try {
            if( enableAuthorisation) {
                if( !handlerHelper.checkAuthorisation( manager, resource, request ) ) {
                    responseHandler.respondUnauthorised( resource, response, request );
                    return;
                }
            }

            manager.onProcessResourceStart( request, response, resource );

            List<String> methodsAllowed = determineMethodsAllowed( manager, resource );
            responseHandler.respondWithOptions( resource, response, request, methodsAllowed );

        } finally {
            t = System.currentTimeMillis() - t;
            manager.onProcessResourceFinish( request, response, resource, t );
        }
    }

	@Override
    public String[] getMethods() {
        return new String[]{Method.OPTIONS.code};
    }

    @Override
    public boolean isCompatible( Resource handler ) {
        return true;
    }

    private List<String> determineMethodsAllowed( HttpManager manager, Resource res ) {
        List<String> list = new ArrayList<String>();
        for( Handler f : manager.getAllHandlers() ) {
            if( f.isCompatible( res ) ) {
                for( String m : f.getMethods() ) {
                    Method httpMethod = Method.valueOf( m );
                    if( !handlerHelper.isNotCompatible( res, httpMethod) ) {
                        list.add( m );
                    }
                }
            }
        }
        return list;
    }

    public boolean isEnableAuthorisation() {
        return enableAuthorisation;
    }

    public void setEnableAuthorisation( boolean enableAuthorisation ) {
        this.enableAuthorisation = enableAuthorisation;
        if( enableAuthorisation && (handlerHelper == null)) {
            throw new RuntimeException( "enableAuthorisation set to true, but no handlerHelper has been provided. You WILL get NullPointerException's");
        }
    }
}
