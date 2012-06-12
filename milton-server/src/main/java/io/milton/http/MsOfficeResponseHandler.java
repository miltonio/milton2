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

package io.milton.http;

import io.milton.resource.Resource;
import io.milton.http.http11.DefaultHttp11ResponseHandler;

import io.milton.http.webdav.DefaultWebDavResponseHandler;
import io.milton.http.webdav.WebDavResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Disables locking, as required for MS office support
 *
 */
public class MsOfficeResponseHandler extends AbstractWrappingResponseHandler {

    private static final Logger log = LoggerFactory.getLogger( DefaultHttp11ResponseHandler.class );

    public MsOfficeResponseHandler( WebDavResponseHandler wrapped ) {
        super( wrapped );
    }

    public MsOfficeResponseHandler( AuthenticationService authenticationService ) {
        super( new DefaultWebDavResponseHandler( authenticationService ) );
    }

    /**
     * Overrides the default behaviour to set the status to Response.Status.SC_NOT_IMPLEMENTED
     * instead of NOT_ALLOWED, so that MS office applications are able to open
     * resources
     *
     * @param res
     * @param response
     * @param request
     */
    @Override
    public void respondMethodNotAllowed( Resource res, Response response, Request request ) {
        wrapped.respondMethodNotImplemented( res, response, request );
    }
}
