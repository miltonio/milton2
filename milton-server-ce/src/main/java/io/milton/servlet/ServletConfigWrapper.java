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

	public ServletConfig getServletConfig() {
		return config;
	}

	@Override
	protected Enumeration initParamNames() {
		return config.getInitParameterNames();
	}
	
	
	
}
