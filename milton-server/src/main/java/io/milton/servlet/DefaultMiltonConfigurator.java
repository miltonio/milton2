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

import io.milton.config.HttpManagerBuilder;
import io.milton.http.AuthenticationHandler;
import io.milton.http.Filter;
import io.milton.http.HttpManager;
import io.milton.http.ResourceFactory;
import io.milton.http.webdav.WebDavResponseHandler;
import java.util.*;
import javax.servlet.ServletException;

/**
 *
 * @author brad
 */
public class DefaultMiltonConfigurator implements MiltonConfigurator {

    private HttpManagerBuilder configurer = new HttpManagerBuilder();

    private List<Initable> initables;
    
    private HttpManager httpManager;
    
    @Override
    public HttpManager configure(Config config) throws ServletException {

        String authHandlers = config.getInitParameter("authenticationHandlers");
        if (authHandlers != null) {
            initAuthHandlers(authHandlers);
        }
        String resourceFactoryClassName = config.getInitParameter("resource.factory.class");
        if (resourceFactoryClassName != null) {
            ResourceFactory rf = instantiate(resourceFactoryClassName);
            configurer.setMainResourceFactory(rf);
        }
        String responseHandlerClassName = config.getInitParameter("response.handler.class");
        if (responseHandlerClassName != null) {
            WebDavResponseHandler davResponseHandler = instantiate(responseHandlerClassName);
            configurer.setWebdavResponseHandler(davResponseHandler);
        }
        List<Filter> filters = null;
        List<String> params = allParams(config);
        for (String paramName : params ) {
            if (paramName.startsWith("filter_")) {
                String filterClass = config.getInitParameter(paramName);
                Filter f = instantiate(filterClass);                
                if( filters == null ) {
                    filters = new ArrayList<Filter>();
                }
                filters.add(f);
            }
        }
        if( filters != null ) {
            configurer.setFilters(filters);
        }
        httpManager = configurer.buildHttpManager();
        initables = new ArrayList<Initable>();
        
        checkAddInitable(initables, configurer.getAuthenticationHandlers());
        checkAddInitable(initables, configurer.getMainResourceFactory());
        checkAddInitable(initables, configurer.getWebdavResponseHandler());
        checkAddInitable(initables, configurer.getFilters() );
        
        for( Initable i : initables ) {
            i.init(config, httpManager);
        }
        return httpManager;
    }

    @Override
    public void shutdown() {
        httpManager.shutdown();
        for( Initable i : initables ) {
            i.destroy(httpManager);
        }        
    }
    
    

    private void initAuthHandlers(String classNames) throws ServletException {
        List<String> authHandlers = loadAuthHandlersIfAny(classNames);
        if (authHandlers == null) {
            return;
        }
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
        configurer.setAuthenticationHandlers(list);
    }

    public static <T> T instantiate(String className) throws ServletException {
        try {
            Class c = Class.forName(className);
            T rf = (T) c.newInstance();
            return rf;
        } catch (Throwable ex) {
            throw new ServletException("Failed to instantiate: " + className, ex);
        }
    }

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

    private List<String> allParams(Config config) {
        Enumeration e = config.getInitParameterNames();
        List<String> list = new ArrayList<String>();
        while(e.hasMoreElements()) {
            list.add((String)e.nextElement());
        }
        return list;
    }

    private void checkAddInitable(List<Initable> initables, Object o) {
        if( o instanceof Initable) {
            initables.add((Initable)o);
        } else if( o instanceof List ) {
            for( Object o2 : (List)o) {
                checkAddInitable(initables, o2);
            }
        }
    }
}
