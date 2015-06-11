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
	private final boolean usePrivateCache = false;

	@Override
    public void setCacheControl( final GetableResource resource, final Response response, Auth auth ) {
        Long delta = resource.getMaxAgeSeconds( auth );
        if( log.isTraceEnabled() ) {
            log.trace("setCacheControl: {} - {}", delta, resource.getClass() );
        }
        if( delta != null && delta > 0 ) {
            if( usePrivateCache && auth != null ) {
                response.setCacheControlPrivateMaxAgeHeader( delta );
                //response.setCacheControlMaxAgeHeader(delta);
            } else {
//				long l = System.currentTimeMillis() + (delta*1000);
//				Date expiryDate = new Date(l);
//				response.setExpiresHeader(expiryDate);
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
