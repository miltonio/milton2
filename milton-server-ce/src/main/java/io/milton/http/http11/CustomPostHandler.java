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

import io.milton.http.Request;
import io.milton.resource.Resource;
import io.milton.http.Response;

/**
 * Used for when we want to delegate POST handling to something other then the
 * usual processForm method.
 *
 * For example, this can be for handling POST requests to scheduling resources
 * with a content type of text/calendar, in which case we should perform
 * specific scheduling logic instead of artbitrary operations which
 * are usually implemented on POST requests
 *
 * @author brad
 */
public interface CustomPostHandler {
    boolean supports(Resource resource, Request request);

    void process(Resource resource, Request request, Response response);
}
