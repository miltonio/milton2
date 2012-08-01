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
 * This can either be initialised with the old
 * servlet/ApplicationConfig/Initable approach, or with directly setting
 * constructor args for the web context and file root.
 *
 * If a URL resolves to a file (not a directory) this ResourceFactory will
 * return a new StaticResource which will serve the file content
 *
 * @author brad
 */
public class StaticResourceFactory implements ResourceFactory, Initable {

	private static final Logger log = LoggerFactory.getLogger(StaticResourceFactory.class);
	/**
	 * either this or root will be set
	 */
	private Config config;
	/**
	 * either this or config will be set
	 */
	private File root;
	private String contextPath;
	private String basePath = "WEB-INF/static";
	private Date modDate = new Date();

	public StaticResourceFactory() {
	}

	public StaticResourceFactory(Config config) {
		this.config = config;
	}

	public StaticResourceFactory(String context, File root) {
		this.root = root;
		this.contextPath = context;
		log.info("root: " + root.getAbsolutePath() + " - context:" + context);
	}

	public StaticResourceFactory(File root) {
		this.root = root;
		this.contextPath = "";
		log.info("root: " + root.getAbsolutePath());
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
		if (root != null) {
			String s = stripContext(url);
			file = new File(root, s);
		} else {
			if (config == null) {
				throw new RuntimeException("ResourceFactory was not configured. ApplicationConfig is null");
			}
			if (config.getServletContext() == null) {
				throw new NullPointerException("config.servletContext is null");
			}
			String path = basePath + url;
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
			}
		}

		if (file != null && file.exists() && !file.isDirectory()) {
			return new StaticResource(file, url, contentType);
		} else {
			return null;
		}

	}

	private String stripContext(String url) {
		if (this.contextPath != null && contextPath.length() > 0) {
			url = url.replaceFirst('/' + contextPath, "");
			log.debug("stripped context: " + url);
			return url;
		} else {
			return url;
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
}
