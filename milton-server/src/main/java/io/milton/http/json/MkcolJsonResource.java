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
