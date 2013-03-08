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

package io.milton.http.http11.auth;

import io.milton.http.Request;
import io.milton.resource.Resource;

/**
 * Provides a source of nonce values to be used in Digest authentication,
 * and a means to validate nonce values.
 *
 * Implementations should ensure that nonce values are available across all
 * servers in a cluster, and that they expire appropriately.
 *
 * Implementations should also ensure that nonce-count values are always
 * increasing, if provided.
 *
 * @author brad
 */
public interface NonceProvider {

   

    public enum NonceValidity {

        OK,
        EXPIRED,
        INVALID
    }

    /**
     * Check to see if the given nonce is known. If known, is it still valid
     * or has it expired.
     *
     * The request may also be considered invalid if the nonceCount value is
     * non-null and is not greater then any previous value for the valid nonce value.
     *
     * @param nonce - the nonce value given by a client to be checked.
     * @param nonceCount - may be null for non-auth requests. otherwise this should
     * be a monotonically increasing value. The server should record the previous
     * value and ensure that this value is greater then any previously given.
     * @return
     */
    NonceValidity getNonceValidity( String nonce, Long nonceCount );

    /**
     * Create and return a nonce value to be used for an authentication session.
     *
     *
     * @param resource - the resource being accessed.
     * @param request - the current request
     * @return - some string to be used as a nonce value.
     */
    String createNonce( Resource resource, Request request );
}
