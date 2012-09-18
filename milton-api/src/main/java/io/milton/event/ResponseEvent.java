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

package io.milton.event;

import io.milton.http.Request;
import io.milton.http.Response;

/**
 * Fired after response is complete
 *
 * @author brad
 */
public class ResponseEvent implements Event {
    private final Request request;
    private final Response response;
    private final long duration;

    public ResponseEvent(Request request, Response response, long duration) {
        this.request = request;
        this.response = response;
        this.duration = duration;
    }


    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }        

    public long getDuration() {
        return duration;
    }        
}
