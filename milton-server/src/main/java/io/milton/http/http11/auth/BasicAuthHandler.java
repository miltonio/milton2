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

package io.milton.http.http11.auth;

import io.milton.http.Auth;
import io.milton.http.Auth.Scheme;
import io.milton.http.AuthenticationHandler;
import io.milton.http.Request;
import io.milton.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class BasicAuthHandler implements AuthenticationHandler {

    private static final Logger log = LoggerFactory.getLogger( BasicAuthHandler.class );

	@Override
    public boolean supports( Resource r, Request request ) {
        Auth auth = request.getAuthorization();
        if( auth == null ) {
            return false;
        }
        log.trace( "supports: {}", auth.getScheme() );
        return auth.getScheme().equals( Scheme.BASIC );
    }

	@Override
    public Object authenticate( Resource resource, Request request ) {
        log.trace( "authenticate" );
        Auth auth = request.getAuthorization();
        Object o = resource.authenticate( auth.getUser(), auth.getPassword() );
        log.trace( "result: {}", o );
        return o;
    }

	@Override
    public String getChallenge( Resource resource, Request request ) {
		if( resource == null ) {
			throw new RuntimeException("Can't generate challenge because resource is null, so can't get realm");
		}
        return "Basic realm=\"" + resource.getRealm() + "\"";
    }

	@Override
    public boolean isCompatible( Resource resource ) {
        return true;
    }
}
