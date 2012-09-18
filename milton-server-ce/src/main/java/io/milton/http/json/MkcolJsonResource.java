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

package io.milton.http.json;

import io.milton.event.EventManager;
import io.milton.event.NewFolderEvent;
import io.milton.http.FileItem;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.MakeCollectionableResource;
import io.milton.resource.PostableResource;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Forwards the POST request to the createCollection method on the wrapped
 * resource, passin it the "name" request parameter
 *
 * @author brad
 */
public class MkcolJsonResource extends JsonResource implements PostableResource {

    private static final Logger log = LoggerFactory.getLogger( MkcolJsonResource.class );
    private final MakeCollectionableResource wrapped;
    private final String href;
    private final EventManager eventManager;

    public MkcolJsonResource( MakeCollectionableResource makeCollectionableResource, String href, EventManager eventManager ) {
        super( makeCollectionableResource, Request.Method.PUT.code, null );
        this.eventManager = eventManager;
        this.wrapped = makeCollectionableResource;
        this.href = href;
    }

    public String processForm( Map<String, String> parameters, Map<String, FileItem> files ) throws BadRequestException, NotAuthorizedException {
        try {
            CollectionResource col = wrapped.createCollection( parameters.get( "name" ) );
            if( eventManager != null ) {
                eventManager.fireEvent( new NewFolderEvent( col ) );
            }
            return null;
        } catch( ConflictException ex ) {
            throw new BadRequestException( wrapped, "A conflict occured. The folder might already exist" );
        }
    }

    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        // nothing to do
    }

    @Override
    public Method applicableMethod() {
        return Method.MKCOL;
    }
}
