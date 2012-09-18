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

import io.milton.http.Request.Method;

/**
 * Resources may implement this, to allow them to decide dynamically whether
 * to support particular HTTP methods.
 *
 * Note that this must not be used for authorisation, use the authorise method
 * on Resource instead.
 *
 * This should only be used to determine whether a resource permits a certain
 * http method regardless of user or application state. Ie is should reflect
 * a configuration choice, and as such be static for the lifetime of the application
 *
 * @author brad
 */
public interface ConditionalCompatibleResource {
    /**
     * Return whether or not this resource might be compatible with the given
     * HTTP method.
     *
     * Note that a resource MUST also implement the corresponding milton interface
     * (E.g. GetableResource)
     *
     * @param m - the HTTP method in the current request
     * @return - false to say that this resource must not handle this request, true
     * to indicate that it might, if it also implements the appropriate method interface
     */
    boolean isCompatible(Method m);
}
