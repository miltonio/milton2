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
import io.milton.http.ResourceFactory;
import io.milton.resource.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used for providing simple readonly access to resources which are files in a
 * conventional file system.
 *
 * Can be provided with a single or multiple root directories. If multiple they
 * are searched in turn for a matching resource
 *
 * Will check for a system property static.resource.roots which, if present, is
 * expected to be a comma delimited list of absolute paths to root locations. An
 * exception will be thrown if a path is given which does not exist
 *
 * @author brad
 */
public class StaticResourceFactory implements ResourceFactory {

	private static final Logger log = LoggerFactory.getLogger(StaticResourceFactory.class);
	public static final String FILE_ROOTS_SYS_PROP_NAME = "static.resource.roots";
	private final List<File> roots;
	private String contextPath;
	private Date modDate = new Date();

	public StaticResourceFactory() {
		roots = new ArrayList<File>();
		String sRoots = System.getProperty(FILE_ROOTS_SYS_PROP_NAME);
		if (sRoots != null && sRoots.length() > 0) {
			for (String s : sRoots.split(",")) {
				s = s.trim();
				if (s.length() > 0) {
					File root = new File(s);
					if (root.exists()) {
						if (root.isDirectory()) {
							roots.add(root);
						} else {
							throw new RuntimeException("Extra file root is not a directory: " + root.getAbsolutePath());
						}
					} else {
						throw new RuntimeException("Extra file root does not exist: " + root.getAbsolutePath());
					}
				}
			}

		}
	}

	public StaticResourceFactory(File root) {
		this();
		roots.add(root);
	}

	public StaticResourceFactory(List<File> roots) {
		this();
		this.roots.addAll(roots);
	}

	@Override
	public Resource getResource(String host, String url) {
		Path p = Path.path(url);
		String contentType = ContentTypeUtils.findContentTypes(p.getName());
		String s = stripContext(url);

		for (File root : roots) {
			File file = new File(root, s);
			if (file.exists() && file.isFile()) {
				return new StaticResource(file, url, contentType);
			}
		}
		return null;
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

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public Date getModDate() {
		return modDate;
	}

	public void setModDate(Date modDate) {
		this.modDate = modDate;
	}

	public List<File> getRoots() {
		return roots;
	}
}
