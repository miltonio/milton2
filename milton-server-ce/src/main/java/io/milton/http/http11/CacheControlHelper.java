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

package io.milton.http.http11;

import io.milton.http.Auth;
import io.milton.resource.GetableResource;
import io.milton.http.Response;

/**
 * Generates the cache-control header on the response
 *
 * @author brad
 */
public interface CacheControlHelper {
    /**
     *
     * @param resource
     * @param response
     * @param auth
     * @param notMod - true means we're sending a not modified response
     */
    void setCacheControl( final GetableResource resource, final Response response, Auth auth);
}
