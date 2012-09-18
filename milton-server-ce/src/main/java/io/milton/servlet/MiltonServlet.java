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
