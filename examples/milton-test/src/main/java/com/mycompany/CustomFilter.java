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

package com.mycompany;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.http.ServletRequest;
import com.bradmcevoy.http.ServletResponse;



/**
 * This filter demonstrates how you can easily write your own servlet filter
 * to invoke milton
 *
 * Using this approach allows you to mix non-milton resources. This example
 * shows the filter bypassing milton for JSP files, but allowing milton
 * to handle all other requests.
 *
 * You can also use StaticResourceFilter
 *
 * @author brad
 */
public class CustomFilter implements javax.servlet.Filter {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CustomFilter.class);

    private HttpManager httpManager;

    public void init( FilterConfig filterConfig ) throws ServletException {
        TResourceFactory fact = new TResourceFactory();
        httpManager = new HttpManager( fact );
    }

    public void doFilter( javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse, FilterChain chain ) throws IOException, ServletException {
        log.debug( "doFilter");
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        String url = null;
        url = ( (HttpServletRequest) servletRequest ).getRequestURL().toString();
        if( !url.endsWith( ".jsp") ) {
            log.debug( "not a JSP, use milton");
            try {
                Request request = new ServletRequest( req );
                Response response = new ServletResponse( resp );
                httpManager.process( request, response );
            } finally {
                servletResponse.getOutputStream().flush();
                servletResponse.flushBuffer();
            }
        } else {
            log.debug( "is a JSP, do not use milton");
            chain.doFilter( servletRequest, servletResponse );
        }

    }

    public void destroy() {

    }
}
