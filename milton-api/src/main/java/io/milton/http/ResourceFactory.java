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
