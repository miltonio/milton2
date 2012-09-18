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
