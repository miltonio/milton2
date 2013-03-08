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
import java.util.List;

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
     * formatted as per http://tools.ietf.org/html/rfc2617 and appended to the
	 * given list of challenges. It is allowable to append more then one challenge
	 * if appropriate
     * 
     * @param resource
     * @param request
     * @return
     */
    void appendChallenges( Resource resource, Request request, List<String> challenges );

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

	/**
	 * Determine if there are login credentials present. Should not attempt to 
	 * validate credentials. Should only determine if something has been provided
	 * 
	 * @param request
	 * @return 
	 */
	boolean credentialsPresent(Request request);
}
