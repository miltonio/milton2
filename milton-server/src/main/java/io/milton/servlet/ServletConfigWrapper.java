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

import io.milton.servlet.Config;
import java.util.Enumeration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;



/**
 *
 * @author brad
 */


public class ServletConfigWrapper extends Config {

	private final ServletConfig config;

	public ServletConfigWrapper(ServletConfig config) {
		this.config = config;
	}
		
	
	@Override
	public ServletContext getServletContext() {
		return config.getServletContext();
	}

	@Override
	public String getInitParameter(String string) {
		return config.getInitParameter(string);
	}

	@Override
	public Enumeration getInitParameterNames() {
		return config.getInitParameterNames();
	}

	public ServletConfig getServletConfig() {
		return config;
	}
	
	
	
}
