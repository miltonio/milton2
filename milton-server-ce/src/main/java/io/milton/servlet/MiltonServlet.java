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

package io.milton.servlet;

import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.Response;
import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * MiltonServlet is a thin wrapper around HttpManager. It takes care of
 * initialisation and delegates requests to the HttpManager
 *
 * The servlet API is hidden by the Milton API, however you can get access to
 * the underlying request and response objects from the static request and
 * response methods which use ThreadLocal variables
 *
 * @author brad
 */
public class MiltonServlet implements Servlet {

    private Logger log = LoggerFactory.getLogger(MiltonServlet.class);
    private static final ThreadLocal<HttpServletRequest> originalRequest = new ThreadLocal<HttpServletRequest>();
    private static final ThreadLocal<HttpServletResponse> originalResponse = new ThreadLocal<HttpServletResponse>();
    private static final ThreadLocal<ServletConfig> tlServletConfig = new ThreadLocal<ServletConfig>();

    public static HttpServletRequest request() {
        return originalRequest.get();
    }

    public static HttpServletResponse response() {
        return originalResponse.get();
    }

    /**
     * Make the servlet config available to any code on this thread.
     *
     * @return
     */
    public static ServletConfig servletConfig() {
        return tlServletConfig.get();
    }

    public static void forward(String url) {
        try {
            request().getRequestDispatcher(url).forward(originalRequest.get(), originalResponse.get());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (ServletException ex) {
            throw new RuntimeException(ex);
        }
    }
    private ServletConfigWrapper config;
    private ServletContext servletContext;
    protected HttpManager httpManager;
    protected MiltonConfigurator configurator;

    @Override
    public void init(ServletConfig config) throws ServletException {
        try {
            this.config = new ServletConfigWrapper(config);
            this.servletContext = config.getServletContext();
            
            String configuratorClassName = config.getInitParameter("milton.configurator");
            if( configuratorClassName != null ) {
                configurator = DefaultMiltonConfigurator.instantiate(configuratorClassName);
            } else {
                configurator = new DefaultMiltonConfigurator();
            }
            httpManager = configurator.configure(this.config);
            
        } catch (ServletException ex) {
            log.error("Exception starting milton servlet", ex);
            throw ex;
        } catch (Throwable ex) {
            log.error("Exception starting milton servlet", ex);
            throw new RuntimeException(ex);
        }
    }


    @Override
    public void destroy() {
        log.debug("destroy");
        if (configurator == null) {
            return;
        }
        configurator.shutdown();
    }

    @Override
    public void service(javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse) throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        try {
            setThreadlocals(req, resp);
            tlServletConfig.set(config.getServletConfig());
            Request request = new ServletRequest(req, servletContext);
            Response response = new ServletResponse(resp);
            httpManager.process(request, response);
        } finally {
            clearThreadlocals();
            tlServletConfig.remove();
            ServletRequest.clearThreadLocals();
            servletResponse.getOutputStream().flush();
            servletResponse.flushBuffer();
        }
    }

    public static void clearThreadlocals() {
        originalRequest.remove();
        originalResponse.remove();
    }

    public static void setThreadlocals(HttpServletRequest req, HttpServletResponse resp) {
        originalRequest.set(req);
        originalResponse.set(resp);
    }

    @Override
    public String getServletInfo() {
        return "MiltonServlet";
    }

    @Override
    public ServletConfig getServletConfig() {
        return config.getServletConfig();
    }
}
