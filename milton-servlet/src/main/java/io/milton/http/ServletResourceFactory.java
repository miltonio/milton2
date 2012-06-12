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

package io.milton.http;

import io.milton.resource.Resource;
import java.io.File;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provides access to servlet resources (ie files defined within the folder
 * which contains WEB-INF) in a milton friendly resource factory
 *
 * @author bradm
 */
public class ServletResourceFactory implements ResourceFactory {

	private final ServletContext servletContext;

	@Autowired
	public ServletResourceFactory(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	public Resource getResource(String host, String path) {
		String contextPath = MiltonServlet.request().getContextPath();
//		System.out.println("url: " + path);
//		System.out.println("context: " + contextPath);
		String localPath = path.substring(contextPath.length());
//		System.out.println("localpath: " + localPath);
		String realPath = servletContext.getRealPath(localPath);
//		System.out.println("realpath: " + realPath);
		if (realPath != null) {
			File file = new File(realPath);
			if (file.exists() && !file.isDirectory()) {
				return new ServletResource(file, localPath, MiltonServlet.request(), MiltonServlet.response());
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
}
