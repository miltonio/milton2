/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mycompany;

import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.Response;
import io.milton.servlet.MiltonFilter;
import io.milton.servlet.MiltonServlet;
import java.io.IOException;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an example of rolling your own milton filter. You can do this as an
 * alternative to using MiltonConfigurator and MiltonFilter
 * 
 * Here's an example of usign it with jetty:
 * //    public static void runWithJetty() {
//        Server server = new Server();
//        HttpManagerBuilderEnt builder = new HttpManagerBuilderEnt();
//        builder.setEnableExpectContinue(false);
//        HttpManager = builder.buildHttpManager();
//        JettyServletUtil jetty = new JettyServletUtil(server, serviceConfig, configuration);
//        int port = serviceConfig.getPort() + kWebdavPortOffset;
//        int sslPort = serviceConfig.hasSslPort() ? port + 1 : 0;
//        ServletContextHandler context = jetty.configureServer("webdavapp",port,sslPort);
//        MyVeryOwnMiltonFilter miltonFilter = new MyVeryOwnMiltonFilter(context, server);
//        FilterHolder filterHolder = new FilterHolder(myMiltonFilter);
//        filterHolder.setInitParameter("resource.factory.class", "com.homer.webdav.WebdavResourceFactory");
//        context.addFilter(filterHolder, "/*", FilterMapping.REQUEST);
//        context.addServlet(TestServlet.class, "/*");
//        server.start();
//    }
 *
 * @author brad
 */
public class MyVeryOwnMiltonFilter implements javax.servlet.Filter {

    private static final Logger log = LoggerFactory.getLogger(MiltonFilter.class);


    
    private ServletContext servletContext;
    protected HttpManager httpManager;

    public MyVeryOwnMiltonFilter(ServletContext servletContext, HttpManager httpManager) {
        this.servletContext = servletContext;
        this.httpManager = httpManager;
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(javax.servlet.ServletRequest req, javax.servlet.ServletResponse resp, javax.servlet.FilterChain fc) throws IOException, ServletException {
        doMiltonProcessing((HttpServletRequest) req, (HttpServletResponse) resp);
    }

    private void doMiltonProcessing(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            MiltonServlet.setThreadlocals(req, resp);
            Request request = new io.milton.servlet.ServletRequest(req, servletContext);
            Response response = new io.milton.servlet.ServletResponse(resp);
            httpManager.process(request, response);
        } finally {
            MiltonServlet.clearThreadlocals();
            resp.getOutputStream().flush();
            resp.flushBuffer();
        }
    }
}
