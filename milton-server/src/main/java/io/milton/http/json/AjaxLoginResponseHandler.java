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

import io.milton.http.AbstractWrappingResponseHandler;
import io.milton.http.Request;
import io.milton.http.Response;
import io.milton.http.webdav.WebDavResponseHandler;
import io.milton.resource.Resource;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * To enable ajax authentication we MUST NOT return 401 unauthorised, because
 * this will prompt for a browser login box
 *
 * Instead we respond 400 to allow the javascript to handle it.
 *
 * We only know that the authentication request came from javascript because
 * the resource is an AjaxLoginResource
 *
 * @author brad
 */
public class AjaxLoginResponseHandler extends AbstractWrappingResponseHandler {

    private static final Logger log = LoggerFactory.getLogger( AjaxLoginResponseHandler.class );
    private final List<ResourceMatcher> resourceMatchers;

    public AjaxLoginResponseHandler( WebDavResponseHandler responseHandler, List<ResourceMatcher> resourceMatchers ) {
        super( responseHandler );
        this.resourceMatchers = resourceMatchers;
        log.debug( "created" );
    }

    /**
     * Create with a single resource matcher, which matches on AjaxLoginResource's
     *
     * @param responseHandler
     */
    public AjaxLoginResponseHandler( WebDavResponseHandler responseHandler ) {
        super( responseHandler );
        this.resourceMatchers = new ArrayList<ResourceMatcher>();
        this.resourceMatchers.add( new TypeResourceMatcher( AjaxLoginResource.class ) );
        log.debug( "created" );
    }

    /**
     * if the resource is a AjaxLoginResource then return a 403
     *
     * otherwise just do a normal 401
     *
     * @param resource
     * @param response
     * @param request
     */
    @Override
    public void respondUnauthorised( Resource resource, Response response, Request request ) {
        if( log.isWarnEnabled() ) {
            log.warn( "respondUnauthorised: ", resource.getClass() );
        }
        if( matches( resource ) ) {
            log.warn( "unauthorised on wrapped ajax resource" );
            wrapped.respondForbidden( resource, response, request );
        } else {
            log.warn( "using normal unauth" );
            wrapped.respondUnauthorised( resource, response, request );
        }
    }

    private boolean matches( Resource r ) {
        for( ResourceMatcher rm : resourceMatchers ) {
            if( rm.matches( r ) ) return true;
        }
        return false;
    }
}
