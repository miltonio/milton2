/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.milton.restlet;

import io.milton.config.HttpManagerConfig;
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
            HttpManagerConfig config = new HttpManagerConfig();
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
