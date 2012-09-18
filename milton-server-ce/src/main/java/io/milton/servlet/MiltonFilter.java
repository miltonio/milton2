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
