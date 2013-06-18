/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mycompany;

import io.milton.config.HttpManagerBuilder;
import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.Response;
import io.milton.http.annotated.AnnotationResourceFactory;
import io.milton.servlet.MiltonFilter;
import io.milton.servlet.MiltonServlet;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is just an example of how to create your own servlet, which is normally
 * not required
 *
 * Creating your own servlet (or filter) allows you to use any configuration
 * framework you choose such as Guice, Pico, etc
 *
 * Milton's core processing is abstracted away from the servlet layer so doing
 * this will not expose you to API volatility. Its a perfectly supported and
 * viable option
 *
 * @author brad
 */
public class MyOwnServlet implements javax.servlet.Servlet {
    private static final Logger log = LoggerFactory.getLogger(MyOwnServlet.class);
    private ServletConfig config;
    private ServletContext servletContext;
    protected HttpManager httpManager;

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.config = config;
        
        // NOTE: Use HttpManagerBuilderEnt for enterprise features!!!
        HttpManagerBuilder builder = new HttpManagerBuilder();
        
        // Set config properties and any instances you need to inject. In this case we'll set up the anno resource factory
        // This is where you can use a dependency injection framework
        builder.setEnableDigestAuth(false); // Example of config property
        AnnotationResourceFactory arf = new AnnotationResourceFactory();
        builder.setMainResourceFactory(arf); // Example of setting dependency        
        Collection<Object> controllers = Collections.EMPTY_LIST; // should have REAL controllers, of course
        arf.setControllers(controllers);
        
        // Init the builder, this will create services that we havent injected
        builder.init(); 
        
        // If you need to adjust properties on the created instances you can do it here
        
        // Now we build the manager. This will wire-up the protocol stack, including connecting
        // inter-protocol services which depend on each other. So dont change anything on the
        // HttpManager once built!
        httpManager = builder.buildHttpManager();
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

    @Override
    public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
        doMiltonProcessing((HttpServletRequest) req, (HttpServletResponse) resp);
    }

    @Override
    public String getServletInfo() {
        return "An example of a custom milton servlet";
    }

    @Override
    public void destroy() {
        
    }

    private void doMiltonProcessing(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Do this part in its own try/catch block, because if there's a classpath
        // problem it will probably be seen here
        Request request;
        Response response;
        try {
            request = new io.milton.servlet.ServletRequest(req, servletContext);
            response = new io.milton.servlet.ServletResponse(resp);
        } catch (Throwable e) {
            // OK, I know its not cool to log AND throw. But we really want to log the error
            // so it goes to the log4j logs, but we also want the container to handle
            // the exception because we're outside the milton response handling framework
            // So log and throw it is. But should never happen anyway...
            log.error("Exception creating milton request/response objects", e);
            throw new IOException("Exception creating milton request/response objects", e);
        }

        try {
            MiltonServlet.setThreadlocals(req, resp);
            httpManager.process(request, response);
        } finally {
            MiltonServlet.clearThreadlocals();
            resp.getOutputStream().flush();
            resp.flushBuffer();
        }
    }
}
