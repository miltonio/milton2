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
import io.milton.http.AuthenticationHandler;
import io.milton.resource.DigestResource;
import io.milton.http.Request;
import io.milton.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class DigestAuthenticationHandler implements AuthenticationHandler {

    private static final Logger log = LoggerFactory.getLogger( DigestAuthenticationHandler.class );
    private final NonceProvider nonceProvider;
    private final DigestHelper digestHelper;


    public DigestAuthenticationHandler( NonceProvider nonceProvider ) {
        this.nonceProvider = nonceProvider;
        this.digestHelper = new DigestHelper(nonceProvider);
    }

	@Override
    public boolean supports( Resource r, Request request ) {
        Auth auth = request.getAuthorization();
        if( auth == null ) {
            return false;
        }
        boolean b;
        if( r instanceof DigestResource ) {
            DigestResource dr = (DigestResource) r;
            if( dr.isDigestAllowed()) {
                b = Auth.Scheme.DIGEST.equals( auth.getScheme() );
            } else {
                log.trace("digest auth is not allowed");
                b = false;
            }
        } else {
            log.trace( "resource is not an instanceof DigestResource" );
            b = false;
        }
        return b;
    }

	@Override
    public Object authenticate( Resource r, Request request ) {
        DigestResource digestResource = (DigestResource) r;
        Auth auth = request.getAuthorization();
        DigestResponse resp = digestHelper.calculateResponse(auth, r.getRealm(), request.getMethod());
        if( resp == null ) {
            log.info("requested digest authentication is invalid or incorrectly formatted");
            return null;
        } else {
            Object o = digestResource.authenticate( resp );
			if( o == null ) {
				log.info("digest authentication failed from resource: " + digestResource.getClass() + " - " + digestResource.getName() + " for user: " + resp.getUser());
			}
            return o;
        }
    }

	@Override
    public String getChallenge( Resource resource, Request request ) {

        String nonceValue = nonceProvider.createNonce( resource, request );
        return digestHelper.getChallenge(nonceValue, request.getAuthorization(), resource.getRealm());
    }

	@Override
    public boolean isCompatible( Resource resource ) {
        if ( resource instanceof DigestResource ) {
			DigestResource dr = (DigestResource) resource;
			return dr.isDigestAllowed();
		} else {
			return false;
		}
    }
}

