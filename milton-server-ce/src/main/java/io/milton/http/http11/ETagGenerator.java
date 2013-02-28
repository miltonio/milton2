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

import io.milton.resource.Resource;

/**
 * Generates ETags, ie entity tags.
 *
 * Custom implementations must be injected into:
 *  - DefaultHttp11ResponseHandler
 *  - DefaultWebDavPropertySource
 *
 * .. or whatever you're using in their place
 *
 * Note that the Http11ResponseHandler interface extends this, since it response
 * handlers logically must know how to generate etags (or that they shouldnt be
 * generated) and it assists with wrapping to expose that functionality, without
 * exposing the dependency directly.
 *
 *
 * HTTP/1.1 origin servers:
      - SHOULD send an entity tag validator unless it is not feasible to
        generate one.

      - MAY send a weak entity tag instead of a strong entity tag, if
        performance considerations support the use of weak entity tags,
        or if it is unfeasible to send a strong entity tag.

      - SHOULD send a Last-Modified value if it is feasible to send one,
        unless the risk of a breakdown in semantic transparency that
        could result from using this date in an If-Modified-Since header
        would lead to serious problems.
 *
 * @author brad
 */
public interface ETagGenerator {
    /**
     * ETag's serve to identify a particular version of a particular resource.
     *
     * If the resource changes, or is replaced, then this value should change
     *
     * @param r - the resource to generate the ETag for
     * @return - an ETag which uniquely identifies this version of this resource
     */
    String generateEtag( Resource r );
}
