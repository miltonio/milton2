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
		path = path.trim();
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
				//throw new RuntimeException(ex);
				log.warn("malformed url when attempting to locate servlet resource", path);
				return null;
			}
			if (resource != null) {
				return new UrlResource(p.getName(), resource, contentType, modDate);
			}
			return null;
		} else {
			if (file.isFile()) {
				return new StaticResource(file);
			} else {
				return null;
			}
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
		if (contextName == null || contextName.equals("") || config.getServletContext().getServletContextName().equals("/")) {
			return url;
		}
		String contextPath = "/" + contextName;
		url = url.replaceFirst('/' + contextPath, "");
		return url;
	}
}
