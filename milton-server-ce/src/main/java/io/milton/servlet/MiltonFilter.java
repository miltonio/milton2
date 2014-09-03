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
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class MiltonFilter implements javax.servlet.Filter {

	private static final Logger log = LoggerFactory.getLogger(MiltonFilter.class);
	private FilterConfigWrapper config;
	private ServletContext servletContext;
	protected HttpManager httpManager;
	protected MiltonConfigurator configurator;
	/**
	 * Resources with this as the first part of their path will not be served
	 * from milton. Instead, this filter will allow filter processing to
	 * continue so they will be served by JSP or a servlet
	 */
	private String[] excludeMiltonPaths;

	@Override
	public void init(FilterConfig config) throws ServletException {
		try {
			this.config = new FilterConfigWrapper(config);
			this.servletContext = config.getServletContext();

			String configuratorClassName = config.getInitParameter("milton.configurator");
			if (configuratorClassName != null) {
				configurator = DefaultMiltonConfigurator.instantiate(configuratorClassName);
			} else {
				configurator = new DefaultMiltonConfigurator();
			}
			log.info("Using configurator: " + configurator.getClass());

			String sExcludePaths = config.getInitParameter("milton.exclude.paths");
			log.info("init: exclude paths: " + sExcludePaths);
			if (sExcludePaths != null) {
				excludeMiltonPaths = sExcludePaths.split(",");
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
	public void doFilter(javax.servlet.ServletRequest req, javax.servlet.ServletResponse resp, javax.servlet.FilterChain fc) throws IOException, ServletException {
		if (req instanceof HttpServletRequest) {
			HttpServletRequest hsr = (HttpServletRequest) req;
			String url = hsr.getRequestURI();
			// Allow certain paths to be excluded from milton, these might be other servlets, for example
			if (excludeMiltonPaths != null) {
				for (String s : excludeMiltonPaths) {
					if (url.startsWith(s)) {
						fc.doFilter(req, resp);
						return;
					}
				}
			}
			doMiltonProcessing((HttpServletRequest) req, (HttpServletResponse) resp);
		} else {
			fc.doFilter(req, resp);
		}
	}

	private void doMiltonProcessing(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		// Do this part in its own try/catch block, because if there's a classpath
		// problem it will probably be seen here
		Request request;
		Response response;
		try{
			request = new io.milton.servlet.ServletRequest(req, servletContext);
			response = new io.milton.servlet.ServletResponse(resp);
		} catch(Throwable e) {		
			// OK, I know its not cool to log AND throw. But we really want to log the error
			// so it goes to the log4j logs, but we also want the container to handle
			// the exception because we're outside the milton response handling framework
			// So log and throw it is. But should never happen anyway...
			log.error("Exception creating milton request/response objects", e);
			throw new IOException("Exception creating milton request/response objects", e);
		}
		
		try {
			MiltonServlet.setThreadlocals(req, resp);
			httpManager.process(request, response);
		} finally {
			MiltonServlet.clearThreadlocals();
			resp.getOutputStream().flush();
			resp.flushBuffer();
		}
	}
}
