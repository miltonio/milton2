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

package com.ettrema.http.caldav.demo.servlet;

import com.bradmcevoy.http.AuthenticationService;
import com.bradmcevoy.http.HandlerHelper;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.MiltonServlet;
import com.bradmcevoy.http.ProtocolHandlers;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.http.ServletRequest;
import com.bradmcevoy.http.ServletResponse;
import com.bradmcevoy.http.WellKnownResourceFactory;
import com.bradmcevoy.http.http11.Http11Protocol;
import com.bradmcevoy.http.webdav.DefaultWebDavResponseHandler;
import com.bradmcevoy.http.webdav.WebDavProtocol;
import com.bradmcevoy.http.webdav.WebDavResourceTypeHelper;
import com.bradmcevoy.property.PropertySource;
import com.ettrema.http.acl.ACLProtocol;
import com.ettrema.http.caldav.CalDavProtocol;
import com.ettrema.http.caldav.CalendarResourceTypeHelper;
import com.ettrema.http.caldav.demo.TResourceFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
 * Note that this is functionally equivalent to the spring xml configuration, just
 * uses simple substitution of xml constructs (eg <constructur-arg>) with plain
 * java equivalents.
 *
 * @author brad
 */
public class CaldavMiltonServlet implements Servlet {

    private Logger log = LoggerFactory.getLogger( MiltonServlet.class );



    private ServletConfig config;
    protected HttpManager httpManager;

	@Override
    public void init( ServletConfig config ) throws ServletException {
		TResourceFactory demoResourceFactory = new com.ettrema.http.caldav.demo.TResourceFactory();
		WebDavResourceTypeHelper rth = new com.bradmcevoy.http.webdav.WebDavResourceTypeHelper();
		CalendarResourceTypeHelper crth = new com.ettrema.http.caldav.CalendarResourceTypeHelper(
				new com.ettrema.http.acl.AccessControlledResourceTypeHelper(rth)
		);
		AuthenticationService authService = new com.bradmcevoy.http.AuthenticationService();
		HandlerHelper hh = new com.bradmcevoy.http.HandlerHelper(authService);
		DefaultWebDavResponseHandler defaultResponseHandler = new com.bradmcevoy.http.webdav.DefaultWebDavResponseHandler(authService, crth);
		Http11Protocol http11 = new com.bradmcevoy.http.http11.Http11Protocol(defaultResponseHandler, hh);
		WebDavProtocol webdav = new com.bradmcevoy.http.webdav.WebDavProtocol(hh, crth, defaultResponseHandler, new ArrayList<PropertySource>());
		CalDavProtocol caldav = new com.ettrema.http.caldav.CalDavProtocol(demoResourceFactory, defaultResponseHandler, hh, webdav);
		List<WellKnownResourceFactory.WellKnownHandler> wellKnownHandlers = new ArrayList<WellKnownResourceFactory.WellKnownHandler>();
		wellKnownHandlers.add(caldav);
		WellKnownResourceFactory wellKnownResourceFactory = new WellKnownResourceFactory(demoResourceFactory, wellKnownHandlers);
		ACLProtocol acl = new com.ettrema.http.acl.ACLProtocol(webdav);
		ProtocolHandlers protocols = new com.bradmcevoy.http.ProtocolHandlers(Arrays.asList(http11, webdav, caldav, acl));
		httpManager = new com.bradmcevoy.http.HttpManager(wellKnownResourceFactory, defaultResponseHandler, protocols);
		
    }

	@Override
    public void destroy() {

    }

	@Override
    public void service( javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse ) throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        try {
            Request request = new ServletRequest( req );
            Response response = new ServletResponse( resp );
            httpManager.process( request, response );
        } finally {

            //servletResponse.getOutputStream().flush();
            servletResponse.flushBuffer();
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
