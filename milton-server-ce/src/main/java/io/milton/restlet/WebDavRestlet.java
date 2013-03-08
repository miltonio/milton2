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

package io.milton.restlet;

import io.milton.config.HttpManagerBuilder;
import io.milton.http.HttpManager;
import io.milton.http.ResourceFactory;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.engine.adapter.HttpRequest;
import org.restlet.engine.adapter.HttpResponse;

/**
 * Calls Milton HttpManager#process(request, response), adapting Restlet#handle(request, response)
 * <p>
 * This is only a frontend adapter, like Milton Servlet. Resources are still obtained with a
 * Milton <code>ResourceFactory</code>, which you have to provide to construct this adapter.
 * </p>
 * <p>
 * Resource implementations are not Restlet resources and have no access to the Restlet environment.
 * </p>
 * <p>
 * Limitation: A <code>GetableResource</code> can not access the global <code>HttpManager.request()</code>
 * and <code>HttpManager.response()</code> variables in its <code>sendContent()</code> method! Writing content
 * is deferred in a Restlet environment and not in the scope of <code>HttpManager.process()</code>.
 * </p>
 * <p>
 * Limitation: Exceptions thrown by <code>GetableResource#sendContent()</code> may not have the desired
 * effect. Restlet will log an INFO message for an <code>IOException</code> i,f it was caused by a broken
 * client connection. Other exceptions, even NotAuthorizedException, BadRequestException, NotFoundException,
 * will be logged SEVERE, and an attempt is made to send an internal error response to the client (might be
 * too late, if the status was already sent). In other words, don't authorize, check for bad requests, or
 * produce "not found" in the <code>GetableResource#sendContent()</code> method. Do it earlier.
 * </p>
 */
public class WebDavRestlet extends Restlet {

    final protected ResourceFactory resourceFactory;
    protected HttpManager httpManager;

    public WebDavRestlet(ResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
    }

    public WebDavRestlet(Context context, ResourceFactory resourceFactory) {
        super(context);
        this.resourceFactory = resourceFactory;
    }

    public ResourceFactory getResourceFactory() {
        return resourceFactory;
    }

    public HttpManager getHttpManager() {
        return httpManager;
    }

    @Override
    public synchronized void start() throws Exception {
        if (!isStarted()) {
            HttpManagerBuilder config = new HttpManagerBuilder();
            config.setMainResourceFactory(getResourceFactory());
            config.setEntityTransport(new RestletEntityTransport());
            httpManager = config.buildHttpManager();
        }
        super.start();
    }

    @Override
    public synchronized void stop() throws Exception {
        if (isStarted()) {
            httpManager.shutdown();
        }
        super.stop();
    }

    @Override
    public void handle(Request request, Response response) {
        if (!isStarted()) return;

        if (!(request instanceof HttpRequest)) {
            throw new IllegalArgumentException(
                    "HttpRequest required for WebDAV, not: " + request
            );
        }

        if (!(response instanceof HttpResponse)) {
            throw new IllegalArgumentException(
                    "HttpResponse required for WebDAV, not: " + response
            );
        }

        httpManager.process(
                new RequestAdapter((HttpRequest) request),
                new ResponseAdapter((HttpResponse) response)
        );
    }

}
