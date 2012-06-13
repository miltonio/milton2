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
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class ApplicationConfig {
    
    final FilterConfig config;
    final ServletConfig servletConfig;
    final ServletContext servletContext;
    final List<String> parameterNames;
    
    public ApplicationConfig() {
        parameterNames = new ArrayList<String>();
        this.config = null;
        this.servletConfig = null;
        this.servletContext = null;
    }
    
    public ApplicationConfig(FilterConfig config) {
        parameterNames = new ArrayList<String>();
        this.config = config;
        this.servletConfig = null;
        servletContext = config.getServletContext();
        if( config == null ) return ;        
        Enumeration en = config.getInitParameterNames();
        while( en.hasMoreElements() ) {
            parameterNames.add( (String)en.nextElement() );
        }        
    }

    public ApplicationConfig(ServletConfig config) {
        parameterNames = new ArrayList<String>();
        this.config = null;
        this.servletConfig = config;
        servletContext = servletConfig.getServletContext();
        if( config == null ) return ;        
        Enumeration en = config.getInitParameterNames();
        while( en.hasMoreElements() ) {
            parameterNames.add( (String)en.nextElement() );
        }        
    }
    
    public String getFilterName() {
        if( servletConfig != null) {
            return servletConfig.getServletName();
        } else {
            return config.getFilterName();
        }
    }

    public String getContextName() {
        return servletContext.getServletContextName();
    }
    
    public String getInitParameter(String string) {        
        if( servletConfig != null) {
            return servletConfig.getInitParameter(string);
        } else {
            return config.getInitParameter(string);
        }        
        
    }

    public Collection<String> getInitParameterNames() {
        return parameterNames;
    }
    
    public File getConfigFile(String path) {
        File f = new File( getWebInfDir(), path);
        return f;
    }

    public File getWebInfDir() {
        String s = servletContext.getRealPath("WEB-INF/" );
        File f = new File(s);
        return f;
    }
    
    public File getRootFolder() {
        String s = servletContext.getRealPath("/");
        File f = new File(s);
        return f;        
    }
    
    public File mapPath( String url ) {
        String pth;
        pth = servletContext.getRealPath(url);
        File file = new File(pth);
        return file;
    }
}
