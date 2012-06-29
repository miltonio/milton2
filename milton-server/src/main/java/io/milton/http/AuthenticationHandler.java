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

/**
 * Implementations of this interface are authentication methods for use
 * with HTTP.
 *
 * These include basic, digest, ntlm etc
 *
 * @author brad
 */
public interface AuthenticationHandler {
    /**
     * Returns true if this supports authenticating with the given Auth data
     * on the given resource.
     *
     * Only the first AuthenticationHandler which returns true for supports
     * will be used for authentication. Ie supports implementations should be
     * mutually exclusive
     *
     * @param r
     * @param auth
     * @return
     */
    boolean supports(Resource r, Request request);

    /**
     * Authenticate the details in the request for access to the given
     * resource.
     *
     * @param resource
     * @param request
     * @return
     */
    Object authenticate( Resource resource, Request request);

    
    /**
     * Create a challenge for this authentication method. This should be completely
     * formatted as per http://tools.ietf.org/html/rfc2617
     * 
     * @param resource
     * @param request
     * @return
     */
    String getChallenge( Resource resource, Request request );

    /**
     * Returns true if this authentication handler is compatible with the given
     * resource
     *
     * This is used when authorisation has failed, in generating challenge responses
     *
     * If you don't want to add a challenge response, return false
     * 
     * @param resource
     * @return - true if this can authenticate the resource, and it should issue a
     * http challenge
     */
    boolean isCompatible( Resource resource, Request request );
}
