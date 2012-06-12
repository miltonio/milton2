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

package io.milton.http;

import io.milton.http.webdav.WebDavResponseHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    private ServletConfig config;
    private ServletContext servletContext;
    protected ServletHttpManager httpManager;

    @Override
    public void init(ServletConfig config) throws ServletException {
        try {
            this.config = config;
            this.servletContext = config.getServletContext();
            // Note that the config variable may be null, in which case default handlers will be used
            // If present and blank, NO handlers will be configed
            List<String> authHandlers = loadAuthHandlersIfAny(config.getInitParameter("authentication.handler.classes"));
            String resourceFactoryFactoryClassName = config.getInitParameter("resource.factory.factory.class");
            if (resourceFactoryFactoryClassName != null && resourceFactoryFactoryClassName.length() > 0) {
                initFromFactoryFactory(resourceFactoryFactoryClassName, authHandlers);
            } else {
                String resourceFactoryClassName = config.getInitParameter("resource.factory.class");
                String responseHandlerClassName = config.getInitParameter("response.handler.class");
                init(resourceFactoryClassName, responseHandlerClassName, authHandlers);
            }
            httpManager.init(new ApplicationConfig(config), httpManager);
        } catch (ServletException ex) {
            log.error("Exception starting milton servlet", ex);
            throw ex;
        } catch (Throwable ex) {
            log.error("Exception starting milton servlet", ex);
            throw new RuntimeException(ex);
        }
    }

    protected void init(String resourceFactoryClassName, String responseHandlerClassName, List<String> authHandlers) throws ServletException {
        log.debug("resourceFactoryClassName: " + resourceFactoryClassName);
        ResourceFactory rf = instantiate(resourceFactoryClassName);
        WebDavResponseHandler responseHandler;
        if (responseHandlerClassName == null) {
            responseHandler = null; // allow default to be created
        } else {
            responseHandler = instantiate(responseHandlerClassName);
        }
        init(rf, responseHandler, authHandlers);
    }

    protected void initFromFactoryFactory(String resourceFactoryFactoryClassName, List<String> authHandlers) throws ServletException {
        log.debug("resourceFactoryFactoryClassName: " + resourceFactoryFactoryClassName);
        ResourceFactoryFactory rff = instantiate(resourceFactoryFactoryClassName);
        rff.init();
        ResourceFactory rf = rff.createResourceFactory();
        WebDavResponseHandler responseHandler = rff.createResponseHandler();
        init(rf, responseHandler, authHandlers);
    }

    protected void init(ResourceFactory rf, WebDavResponseHandler responseHandler, List<String> authHandlers) throws ServletException {
        AuthenticationService authService;
        if (authHandlers == null) {
            authService = new AuthenticationService();
        } else {
            List<AuthenticationHandler> list = new ArrayList<AuthenticationHandler>();
            for (String authHandlerClassName : authHandlers) {
                Object o = instantiate(authHandlerClassName);
                if (o instanceof AuthenticationHandler) {
                    AuthenticationHandler auth = (AuthenticationHandler) o;
                    list.add(auth);
                } else {
                    throw new ServletException("Class: " + authHandlerClassName + " is not a: " + AuthenticationHandler.class.getCanonicalName());
                }
            }
            authService = new AuthenticationService(list);
        }

        // log the auth handler config
        log.debug("Configured authentication handlers: " + authService.getAuthenticationHandlers().size());
        if (authService.getAuthenticationHandlers().size() > 0) {
            for (AuthenticationHandler hnd : authService.getAuthenticationHandlers()) {
                log.debug(" - " + hnd.getClass().getCanonicalName());
            }
        } else {
            log.warn("No authentication handlers are configured! Any requests requiring authorisation will fail.");
        }


        if (responseHandler == null) {
            httpManager = new ServletHttpManager(rf, authService);
        } else {
            httpManager = new ServletHttpManager(rf, responseHandler, authService);
        }
    }

    protected <T> T instantiate(String className) throws ServletException {
        try {
            Class c = Class.forName(className);
            T rf = (T) c.newInstance();
            return rf;
        } catch (Throwable ex) {
            throw new ServletException("Failed to instantiate: " + className, ex);
        }
    }

    @Override
    public void destroy() {
        log.debug("destroy");
        if (httpManager == null) {
            return;
        }
        httpManager.destroy(httpManager);
    }

    @Override
    public void service(javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse) throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        try {
            setThreadlocals(req, resp);
            tlServletConfig.set(config);
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
        return config;
    }

    /**
     * Returns null, or a list of configured authentication handler class names
     *
     * @param initParameter - null, or the (possibly empty) list of comma
     * seperated class names
     * @return - null, or a possibly empty list of class names
     */
    private List<String> loadAuthHandlersIfAny(String initParameter) {
        if (initParameter == null) {
            return null;
        }
        String[] arr = initParameter.split(",");
        List<String> list = new ArrayList<String>();
        for (String s : arr) {
            s = s.trim();
            if (s.length() > 0) {
                list.add(s);
            }
        }
        return list;
    }
}
