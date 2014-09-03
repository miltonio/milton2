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

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.ServletContext;

/**
 * Provides a common interface for servlet and filter configuration
 *
 * @author brad
 */
public abstract class Config {
    public abstract ServletContext getServletContext();

    public abstract String getInitParameter(String string);
    
	
	protected abstract Enumeration initParamNames();

    public File getConfigFile(String path) {
        File f = new File( getWebInfDir(), path);
        return f;
    }

    public File getWebInfDir() {
        String s = getServletContext().getRealPath("WEB-INF/" );
        File f = new File(s);
        return f;
    }
    
    public File getRootFolder() {
        String s = getServletContext().getRealPath("/");
        File f = new File(s);
        return f;        
    }
    
    public File mapPath( String url ) {
        String pth;
        pth = getServletContext().getRealPath(url);
        File file = new File(pth);
        return file;
    }	
	
	public List<String> getInitParameterNames() {
		List<String> list = new ArrayList<String>();
		Enumeration en = initParamNames();
		while(en.hasMoreElements()) {
			list.add((String)en.nextElement());
		}
		return list;
	}
	
}
