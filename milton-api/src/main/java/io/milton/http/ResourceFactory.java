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
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;

/**
 * Implementations of ResourceFactory translate URLs to instances of Resource
 * 

 * 
 * @author brad
 */
public interface ResourceFactory {
    
    /**
     * Locate an instance of a resource at the given url and on the given host.
     * <P/>
     * The host argument can be used for applications which implement virtual
     * domain hosting. But portable applications (ie those which do not depend on the host
     * name) should ignore the host argument. 
     * <P/>
     * Note that the host will include the port number if it was specified in
     * the request
     * <P/>
     * The path argument is just the part of the request url with protocol, host, port
     * number, and request parameters removed
     * <P/>
     * E.g. for a request <PRE>http://milton.ettrema.com:80/downloads/index.html?ABC=123</PRE>
     * the corresponding arguments will be:
     * <PRE>
     *   host: milton.ettrema.com:80
     *   path: /downloads/index.html
     * </PRE>
     * Note that your implementation should not be sensitive to trailing slashes
     * E.g. these paths should return the same resource /apath and /apath/
     * <P/>
     * Return null if there is no associated {@see Resource} object.
     * <P/>
     * You should generally avoid using any request information other then that
     * provided in the method arguments. But if you find you need to you can access the
     * request and response objects from HttpManager.request() and HttpManager.response()
     * 
     * @param host  Full host name with port number, e.g. milton.ettrema.com:80
     * @param path  Relative path on server, e.g. /downloads/index.html
     * @return the associated Resource object, or null if there is none.
     */
    Resource getResource(String host, String path) throws NotAuthorizedException, BadRequestException;
    
    
}
