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
import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.Response;
import io.milton.mail.MailServer;
import io.milton.mail.MailServerBuilder;
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
		StaticApplicationContext parent = new StaticApplicationContext();
		parent.getBeanFactory().registerSingleton("servletContext", fc.getServletContext());
		parent.refresh();
		context = new ClassPathXmlApplicationContext(new String[]{"applicationContext.xml"}, parent);
		Object milton = context.getBean("milton.http.manager");
		if (milton instanceof HttpManager) {
			this.httpManager = (HttpManager) milton;
		} else if (milton instanceof HttpManagerBuilder) {
			HttpManagerBuilder builder = (HttpManagerBuilder) milton;
			this.httpManager = builder.buildHttpManager();
		}
		this.filterConfig = fc;
		servletContext = fc.getServletContext();
		String sExcludePaths = fc.getInitParameter("milton.exclude.paths");
		log.info("init: exclude paths: " + sExcludePaths);
		excludeMiltonPaths = sExcludePaths.split(",");

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
