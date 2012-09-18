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

import io.milton.http.Handler;
import io.milton.http.HandlerHelper;
import io.milton.http.HttpExtension;
import io.milton.http.ResourceHandlerHelper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author brad
 */
public class Http11Protocol implements HttpExtension{

    private final Set<Handler> handlers;

    private final HandlerHelper handlerHelper;

    private List<CustomPostHandler> customPostHandlers;

    public Http11Protocol(Http11ResponseHandler responseHandler, HandlerHelper handlerHelper, ResourceHandlerHelper resourceHandlerHelper, boolean enableOptionsAuth, MatchHelper matchHelper, PartialGetHelper partialGetHelper) {
        this.handlers = new HashSet<Handler>();
        this.handlerHelper = handlerHelper;
        handlers.add(new OptionsHandler(responseHandler, resourceHandlerHelper, handlerHelper, enableOptionsAuth));
        handlers.add(new GetHandler(responseHandler, resourceHandlerHelper, matchHelper, partialGetHelper));
        handlers.add(new PostHandler(responseHandler, resourceHandlerHelper));
        handlers.add(new DeleteHandler(responseHandler, resourceHandlerHelper, handlerHelper));
		PutHelper putHelper = new PutHelper();
        handlers.add(new PutHandler(responseHandler, handlerHelper, putHelper, matchHelper));
    }

	@Override
    public Set<Handler> getHandlers() {
        return handlers;
    }

    public HandlerHelper getHandlerHelper() {
        return handlerHelper;
    }

	@Override
    public List<CustomPostHandler> getCustomPostHandlers() {
        return customPostHandlers;
    }
}
