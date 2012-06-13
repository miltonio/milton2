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

package io.milton.servlet;

import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.Response;
import io.milton.http.http11.DefaultHttp11ResponseHandler;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

/**
 * Loads the spring context from classpath at applicationContext.xml
 *
 * This filter then gets the bean named milton.http.manager and uses that for
 * milton processing.
 *
 * Requests with a path which begins with one of the exclude paths will not be
 * processed by milton. Instead, for these requests, the filter chain will be
 * invoked so the request can be serviced by JSP or a servlet, etc
 *
 * This uses an init parameter called milton.exclude.paths, which should be a
 * comma seperated list of paths to ignore. For example:
 * /static,/images,/login.jsp
 *
 * This allows non-milton resources to be accessed, while still mapping all urls
 * to milton
 *
 * @author bradm
 */
public class SpringMiltonFilter implements javax.servlet.Filter {

    private ClassPathXmlApplicationContext context;
    private HttpManager httpManager;
    private FilterConfig filterConfig;
    private ServletContext servletContext;
    /**
     * Resources with this as the first part of their path will not be served
     * from milton. Instead, this filter will allow filter processing to
     * continue so they will be served by JSP or a servlet
     */
    private String[] excludeMiltonPaths;

    @Override
    public void init(FilterConfig fc) throws ServletException {        
        StaticApplicationContext parent = new StaticApplicationContext();
        parent.getBeanFactory().registerSingleton("servletContext", fc.getServletContext());
        parent.refresh();
        context = new ClassPathXmlApplicationContext(new String[]{"applicationContext.xml"}, parent);
        this.httpManager = (HttpManager) context.getBean("milton.http.manager");
        this.filterConfig = fc;
        servletContext = fc.getServletContext();
        String sExcludePaths = fc.getInitParameter("milton.exclude.paths");
        excludeMiltonPaths = sExcludePaths.split(",");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain fc) throws IOException, ServletException {
        if (req instanceof HttpServletRequest) {
            HttpServletRequest hsr = (HttpServletRequest) req;
            String url = hsr.getRequestURI();
            // Allow certain paths to be excluded from milton, these might be other servlets, for example
            for (String s : excludeMiltonPaths) {
                if (url.startsWith(s)) {
                    fc.doFilter(req, resp);
                    return;
                }
            }
            doMiltonProcessing((HttpServletRequest) req, (HttpServletResponse) resp);
        } else {
            fc.doFilter(req, resp);
            return;
        }
    }

    @Override
    public void destroy() {
        context.close();
    }

    private void doMiltonProcessing(HttpServletRequest req, HttpServletResponse resp) throws IOException {        
        try {
            MiltonServlet.setThreadlocals(req, resp);
            Request request = new io.milton.servlet.ServletRequest(req,servletContext);
            Response response = new io.milton.servlet.ServletResponse(resp);
            httpManager.process(request, response);
        } finally {
            MiltonServlet.clearThreadlocals();
            resp.getOutputStream().flush();
            resp.flushBuffer();
        }
    }
}
