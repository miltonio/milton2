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
import io.milton.http.Request.Method;
import io.milton.http.http11.auth.DigestResponse;

/**
 *
 */
public interface SecurityManager {

    /**
     * Authenticate a digest request
     *
     * See com.bradmcevoy.http.http11.auth.DigestGenerator
     *
     * @param digestRequest
     * @return
     */
    Object authenticate( DigestResponse digestRequest );


    /**
     *
     * @param user
     * @param password
     * @return - some object representing the user to associate with the request
     */
    Object authenticate( String user, String password );

    /**
     * Check that the authenticater user can access the given resource for the
     * given method.
     *
     * @param request - the request itself
     * @param method - the request method
     * @param auth - authentication object representing the current user
     * @param resource - the resource being operated on
     * @return - true to indicate that the user is allowed access
     */
    boolean authorise( Request request, Method method, Auth auth, Resource resource );

    /**
     *
     * @param  - host - the host name which has been requested
     * 
     * @return - the name of the security realm this is managing
     */
    String getRealm(String host);
	
	boolean isDigestAllowed();



}
