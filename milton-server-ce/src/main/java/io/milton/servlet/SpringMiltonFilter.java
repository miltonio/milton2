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
import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.ResourceFactory;
import io.milton.http.Response;
import io.milton.http.annotated.AnnoResource;
import io.milton.http.annotated.AnnotationResourceFactory;
import io.milton.http.template.JspViewResolver;
import io.milton.http.template.ViewResolver;
import io.milton.http.webdav.DisplayNameFormatter;
import io.milton.mail.MailServer;
import io.milton.mail.MailServerBuilder;
import io.milton.resource.PropFindableResource;
import java.io.File;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Loads the spring context from classpath at applicationContext.xml
 *
 * This filter then gets the bean named milton.http.manager and uses that for
 * milton processing.
 *
 * The milton.http.manager bean can either be a HttpManager or it can be a
 * HttpManagerBuilder, in which case a HttpManager is constructed from it
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

	private static final Logger log = LoggerFactory.getLogger(SpringMiltonFilter.class);
	private ClassPathXmlApplicationContext context;
	private HttpManager httpManager;
	private MailServer mailServer;
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
		log.info("init");
		WebApplicationContext rootContext = WebApplicationContextUtils.getWebApplicationContext(fc.getServletContext());

		StaticApplicationContext parent;
		if (rootContext != null) {
			log.info("Found a root spring context, and using it");
			parent = new StaticApplicationContext(rootContext);
		} else {
			log.info("No root spring context");
			parent = new StaticApplicationContext();
		}
		FilterConfigWrapper configWrapper = new FilterConfigWrapper(fc);
		parent.getBeanFactory().registerSingleton("config", configWrapper);
		parent.getBeanFactory().registerSingleton("servletContext", fc.getServletContext());
		File webRoot = new File(fc.getServletContext().getRealPath("/"));
		parent.getBeanFactory().registerSingleton("webRoot", webRoot);
		log.info("Registered root webapp path in: webroot=" + webRoot.getAbsolutePath());
		parent.refresh();
		
		this.filterConfig = fc;
		servletContext = fc.getServletContext();
		String sExcludePaths = fc.getInitParameter("milton.exclude.paths");
		log.info("init: exclude paths: " + sExcludePaths);
		excludeMiltonPaths = sExcludePaths.split(",");

		String sFiles = fc.getInitParameter("contextConfigLocation");
		String[] contextFiles;
		if (sFiles != null && sFiles.trim().length() > 0) {
			contextFiles = sFiles.split(" ");
		} else {
			contextFiles = new String[]{"applicationContext.xml"};
		}

		context = new ClassPathXmlApplicationContext(contextFiles, parent);
		
		Object milton = context.getBean("milton.http.manager");
		if (milton instanceof HttpManager) {
			this.httpManager = (HttpManager) milton;
		} else if (milton instanceof HttpManagerBuilder) {
			HttpManagerBuilder builder = (HttpManagerBuilder) milton;
			ResourceFactory rf = builder.getMainResourceFactory();
			if (rf instanceof AnnotationResourceFactory) {
				AnnotationResourceFactory arf = (AnnotationResourceFactory) rf;
				if (arf.getViewResolver() == null) {
					ViewResolver viewResolver = new JspViewResolver(servletContext);
					arf.setViewResolver(viewResolver);
				}
			}
			this.httpManager = builder.buildHttpManager();
		}

		// init mail server

		if (context.containsBean("milton.mail.server")) {
			log.info("init mailserver...");
			Object oMailServer = context.getBean("milton.mail.server");
			if (oMailServer instanceof MailServer) {
				mailServer = (MailServer) oMailServer;
			} else if (oMailServer instanceof MailServerBuilder) {
				MailServerBuilder builder = (MailServerBuilder) oMailServer;
				mailServer = builder.build();
			} else {
				throw new RuntimeException("Unsupported type: " + oMailServer.getClass() + " expected " + MailServer.class + " or " + MailServerBuilder.class);
			}
			log.info("starting mailserver");
			mailServer.start();
		}
		log.info("Finished init");
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain fc) throws IOException, ServletException {		
		if (req instanceof HttpServletRequest) {			
			HttpServletRequest hsr = (HttpServletRequest) req;
			String url = hsr.getRequestURI();
			// Allow certain paths to be excluded from milton, these might be other servlets, for example
			for (String s : excludeMiltonPaths) {
				if (url.startsWith(s)) {
					log.trace("doFilter: is excluded path");
					fc.doFilter(req, resp);
					return;
				}
			}
			log.trace("doFilter: begin milton processing");
			doMiltonProcessing((HttpServletRequest) req, (HttpServletResponse) resp);
		} else {
			log.trace("doFilter: request is not a supported type, continue with filter chain");
			fc.doFilter(req, resp);
			return;
		}
	}

	@Override
	public void destroy() {
		context.close();
		if (httpManager != null) {
			httpManager.shutdown();
		}
		if (mailServer != null) {
			mailServer.stop();
		}
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
