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

import io.milton.http.Auth;
import io.milton.resource.GetableResource;
import io.milton.http.Response;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class DefaultCacheControlHelper implements CacheControlHelper {

    private static final Logger log = LoggerFactory.getLogger( DefaultCacheControlHelper.class );
    private boolean usePrivateCache = false;

	@Override
    public void setCacheControl( final GetableResource resource, final Response response, Auth auth ) {
        Long delta = resource.getMaxAgeSeconds( auth );
        if( log.isTraceEnabled() ) {
            log.trace( "setCacheControl: " + delta + " - " + resource.getClass() );
        }
        if( delta != null && delta > 0 ) {
            if( usePrivateCache && auth != null ) {
                response.setCacheControlPrivateMaxAgeHeader( delta );
                //response.setCacheControlMaxAgeHeader(delta);
            } else {
                response.setCacheControlMaxAgeHeader( delta );
            }
            // Disable, might be interfering with IE.. ?
//            Date expiresAt = calcExpiresAt( new Date(), delta.longValue() );
//            if( log.isTraceEnabled() ) {
//                log.trace( "set expires: " + expiresAt );
//            }
//            response.setExpiresHeader( expiresAt );
        } else {
            response.setCacheControlNoCacheHeader();
        }
    }

    public static Date calcExpiresAt( Date modifiedDate, long deltaSeconds ) {
        long deltaMs = deltaSeconds * 1000;
        long expiresAt = System.currentTimeMillis() + deltaMs;
        return new Date( expiresAt );
    }
}
