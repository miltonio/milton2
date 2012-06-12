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

package io.milton.resource;

import io.milton.resource.Resource;
import io.milton.http.http11.auth.DigestResponse;

/**
 * Interface to support digest HTTP authentication.
 * <P/>
 * This provides an authentication method compatible with digest. The key
 * difference between this and Basic authentication is that the password
 * is not available in the request. What is sent is a one way hash of
 * several factors. To check the validity of a message, you must calculate
 * the same one way hash on the server
 * <P/>
 * Milton never requires a plain text password so the complete digest is passed
 * on to the resource implementation. You may choose to store the plain text password
 * , or you might choose to store a one hash of a subset of the digest auth
 * factors for greater security.
 * <P/>
 * Either way you SHOULD use the DigestGenerator class to calculate the hash
 *
 * @author brad
 */
public interface DigestResource extends Resource {
    /**
     * Check the given credentials, and return a relevant object if accepted.
     * 
     * Returning null indicates credentials were not accpeted
     *
     * You SHOULD use com.bradmcevoy.http.http11.auth.DigestGenerator to implement
     * digest calculation, and then compare that to the given request digest.
     * 
     * @param digestRequest - the digest authentication information provided by the client
     * @return - if credentials are accepted, some object to attach to the Auth object. otherwise null
     */
    Object authenticate(DigestResponse digestRequest);

    /**
     *
     * @return - true if this resource actually allows digest authentication.
     */
    boolean isDigestAllowed();
}
