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

/** Passes the request and response along a series of filters
 *
 *  By default the HttpManager loads a single filter which executes the appropriate
 *  handler for the http method
 *
 *  Additional filters can be added using HttpManager.addFilter
 */
public class FilterChain {
    
    final HttpManager httpManager;
    int pos = 0;
    
    public FilterChain(HttpManager httpManager) {
        this.httpManager = httpManager;
    }

    public void process( Request request, Response response) {        
        Filter filter = httpManager.getFilters().get(pos++);
        filter.process(this,request,response);
    }

    public HttpManager getHttpManager() {
        return httpManager;
    }
       
}
