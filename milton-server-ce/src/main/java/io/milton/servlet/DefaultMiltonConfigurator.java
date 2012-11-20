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

import io.milton.config.HttpManagerBuilder;
import io.milton.http.AuthenticationHandler;
import io.milton.http.Filter;
import io.milton.http.HttpManager;
import io.milton.http.ResourceFactory;
import io.milton.http.webdav.WebDavResponseHandler;
import java.util.*;
import javax.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default means of configuring milton's HttpManager.
 *
 * Provide init-params to the filter or servlet to configure it:
 * resource.factory.class - the class of your resource factory
 * response.handler.class - specify if you want a different response handler
 * authenticationHandlers - a list of class names for the authentication
 * handlers filter_X - define an ordered list of milton filters, where the name
 * is in the form filter_1, filter_2, etc and the value is the name of the
 * filter class
 *
 * @author brad
 */
public class DefaultMiltonConfigurator implements MiltonConfigurator {

	private static final Logger log = LoggerFactory.getLogger(DefaultMiltonConfigurator.class);
	protected HttpManagerBuilder builder;
	protected List<Initable> initables;
	protected HttpManager httpManager;

	public DefaultMiltonConfigurator() {
		try {
			// Attempt to use Enterprise edition build if available
			Class builderClass = Class.forName("io.milton.ent.config.HttpManagerBuilderEnt");
			builder = (HttpManagerBuilder) builderClass.newInstance();
			log.info("Using enterprise builder: " + builder.getClass());
		} catch (InstantiationException ex) {
			log.info("Couldnt instantiate enterprise builder, DAV level 2 and beyond features will not be available");
			builder = new HttpManagerBuilder();
		} catch (IllegalAccessException ex) {
			log.info("Couldnt instantiate enterprise builder, DAV level 2 and beyond features will not be available");
			builder = new HttpManagerBuilder();
		} catch (ClassNotFoundException ex) {
			log.info("Couldnt instantiate enterprise builder, DAV level 2 and beyond features will not be available");
			builder = new HttpManagerBuilder();
		}
	}

	@Override
	public HttpManager configure(Config config) throws ServletException {

		log.info("Listing all config parameters:");
		for (String s : config.getInitParameterNames()) {
			log.info(" " + s + " = " + config.getInitParameter(s));
		}

		String authHandlers = config.getInitParameter("authenticationHandlers");
		if (authHandlers != null) {
			initAuthHandlers(authHandlers);
		}
		String resourceFactoryClassName = config.getInitParameter("resource.factory.class");
		if (resourceFactoryClassName != null) {
			ResourceFactory rf = instantiate(resourceFactoryClassName);
			builder.setMainResourceFactory(rf);
		} else {
			log.warn("No custom ResourceFactory class name provided in resource.factory.class");
		}
		String responseHandlerClassName = config.getInitParameter("response.handler.class");
		if (responseHandlerClassName != null) {
			WebDavResponseHandler davResponseHandler = instantiate(responseHandlerClassName);
			builder.setWebdavResponseHandler(davResponseHandler);
		}
		List<Filter> filters = null;
		List<String> params = config.getInitParameterNames();
		for (String paramName : params) {
			if (paramName.startsWith("filter_")) {
				String filterClass = config.getInitParameter(paramName);
				Filter f = instantiate(filterClass);
				if (filters == null) {
					filters = new ArrayList<Filter>();
				}
				filters.add(f);
			}
		}
		if (filters != null) {
			builder.setFilters(filters);
		}
		build();
		initables = new ArrayList<Initable>();

		checkAddInitable(initables, builder.getAuthenticationHandlers());
		checkAddInitable(initables, builder.getMainResourceFactory());
		checkAddInitable(initables, builder.getWebdavResponseHandler());
		checkAddInitable(initables, builder.getFilters());

		for (Initable i : initables) {
			i.init(config, httpManager);
		}
		return httpManager;
	}

	/**
	 * Actually builds the httpManager. Can be overridden by subclasses
	 */
	protected void build() {
		httpManager = builder.buildHttpManager();
	}

	@Override
	public void shutdown() {
		if (httpManager != null) {
			httpManager.shutdown();
		}
		if (initables != null) {
			for (Initable i : initables) {
				i.destroy(httpManager);
			}
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
		builder.setAuthenticationHandlers(list);
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

	private void checkAddInitable(List<Initable> initables, Object o) {
		if (o instanceof Initable) {
			initables.add((Initable) o);
		} else if (o instanceof List) {
			for (Object o2 : (List) o) {
				checkAddInitable(initables, o2);
			}
		}
	}
}
