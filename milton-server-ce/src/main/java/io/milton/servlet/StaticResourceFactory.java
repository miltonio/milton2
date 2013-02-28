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
		String s = stripContext(url);

		for (File root : roots) {
			File file = new File(root, s);
			if (file.exists() && file.isFile()) {
				return new StaticResource(file);
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
