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
    public OptionsHandler( Http11ResponseHandler responseHandler, ResourceHandlerHelper resourceHandlerHelper, HandlerHelper handlerHelper, boolean enableAuthorisation ) {
        this.responseHandler = responseHandler;
        this.handlerHelper = handlerHelper;
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
