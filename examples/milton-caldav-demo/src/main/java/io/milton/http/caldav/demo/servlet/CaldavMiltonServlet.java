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
package io.milton.http.caldav.demo.servlet;

import io.milton.http.*;
import io.milton.http.acl.ACLProtocol;
import io.milton.http.caldav.CalDavProtocol;
import io.milton.http.caldav.CalendarResourceTypeHelper;
import io.milton.http.caldav.demo.TResourceFactory;
import io.milton.http.http11.Http11Protocol;
import io.milton.http.webdav.DefaultWebDavResponseHandler;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.http.webdav.WebDavResourceTypeHelper;
import java.io.IOException;
import java.util.Arrays;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an example servlet to show how to use milton's caldav without spring
 *
 * Currently this isnt used in this project.
 *
 * Note that this is functionally equivalent to the spring xml configuration,
 * just uses simple substitution of xml constructs (eg <constructur-arg>) with
 * plain java equivalents.
 *
 * @author brad
 */
public class CaldavMiltonServlet implements Servlet {

    private Logger log = LoggerFactory.getLogger(MiltonServlet.class);
    private ServletConfig config;
    protected HttpManager httpManager;

    @Override
    public void init(ServletConfig config) throws ServletException {
        TResourceFactory demoResourceFactory = new io.milton.http.caldav.demo.TResourceFactory();
        WebDavResourceTypeHelper rth = new io.milton.http.webdav.WebDavResourceTypeHelper();
        CalendarResourceTypeHelper crth = new io.milton.http.caldav.CalendarResourceTypeHelper(
                new io.milton.http.acl.AccessControlledResourceTypeHelper(rth));
        AuthenticationService authService = new io.milton.http.AuthenticationService();
        HandlerHelper hh = new io.milton.http.HandlerHelper(authService);
        DefaultWebDavResponseHandler defaultResponseHandler = new io.milton.http.webdav.DefaultWebDavResponseHandler(authService, crth);
        Http11Protocol http11 = new io.milton.http.http11.Http11Protocol(defaultResponseHandler, hh);
        WebDavProtocol webdav = new io.milton.http.webdav.WebDavProtocol(hh, crth, defaultResponseHandler, null);
        CalDavProtocol caldav = new io.milton.http.caldav.CalDavProtocol(demoResourceFactory, defaultResponseHandler, hh, webdav);
        ACLProtocol acl = new io.milton.http.acl.ACLProtocol(webdav);
        ProtocolHandlers protocols = new io.milton.http.ProtocolHandlers(Arrays.asList(http11, webdav, caldav, acl));
        httpManager = new io.milton.http.HttpManager(demoResourceFactory, defaultResponseHandler, protocols);

    }

    @Override
    public void destroy() {
    }

    @Override
    public void service(javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse) throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        try {
            Request request = new ServletRequest(req, config.getServletContext());
            Response response = new ServletResponse(resp);
            httpManager.process(request, response);
        } finally {
            try {
                //servletResponse.getOutputStream().flush();
                servletResponse.flushBuffer();
            } catch (Exception e) {
                log.warn("exception flushing, probably no real problem", e);
            }
        }
    }

    @Override
    public String getServletInfo() {
        return "CaldavMiltonServlet";
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }
}
