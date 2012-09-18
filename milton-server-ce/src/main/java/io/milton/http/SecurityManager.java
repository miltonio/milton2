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
