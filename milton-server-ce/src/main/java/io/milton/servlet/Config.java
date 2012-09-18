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

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.ServletContext;

/**
 * Provides a common interface for servetl and filter configuration
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
