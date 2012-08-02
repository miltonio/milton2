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

import io.milton.common.ContentTypeUtils;
import io.milton.common.Path;
import io.milton.http.HttpManager;
import io.milton.http.ResourceFactory;
import io.milton.resource.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to resources exposed by the servlet context.
 * 
 * Attempts to locate a physical file, via getRealPath. This will usually work,
 * but may not in cases where the webapp is running from a war file, or if
 * overlays are used.
 * 
 * If not found it attempts to locate a URL with servletContext.getResource
 *
 * @author brad
 */
public class WebResourceFactory implements ResourceFactory, Initable {

	private static final Logger log = LoggerFactory.getLogger(WebResourceFactory.class);
	
	private Config config;
	private String basePath = "WEB-INF/static";
	private Date modDate = new Date();

	public WebResourceFactory() {
	}

	public WebResourceFactory(Config config) {
		this.config = config;
	}

	@Override
	public void init(Config config, HttpManager manager) {
		this.config = config;
	}

	@Override
	public Resource getResource(String host, String url) {
		System.out.println("getResource: " + url);

		Path p = Path.path(url);
		String contentType;
		if (config != null) {
			contentType = MiltonUtils.getContentType(config.getServletContext(), p.getName());
		} else {
			contentType = ContentTypeUtils.findContentTypes(p.getName());
		}

		File file;
		String path = stripContext(url);
		path = basePath + path;
		String realPath = config.getServletContext().getRealPath(path);
		if (realPath != null) {
			file = new File(path);
		} else {
			file = null;
		}
		if (file == null || !file.exists()) {
			URL resource;
			try {
				resource = config.getServletContext().getResource(path);
			} catch (MalformedURLException ex) {
				throw new RuntimeException(ex);
			}
			if (resource != null) {
				return new UrlResource(p.getName(), resource, contentType, modDate);
			}
			return null;
		} else {
			return new StaticResource(file, url, contentType);
		}
	}

	@Override
	public void destroy(HttpManager manager) {
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
	
	private String stripContext(String url) {
		String contextName = config.getServletContext().getServletContextName();
		if( contextName == null || contextName.equals("") || config.getServletContext().getServletContextName().equals("/")) {
			return url;
		}
		String contextPath = "/" + contextName;
		url = url.replaceFirst('/' + contextPath, "");
		return url;
	}	
}
