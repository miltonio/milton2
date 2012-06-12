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

import io.milton.http.webdav.WebDavResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Just extends the normal HttpManager to support initialising filters from
 * servlet init params.
 * 
 * Note the init method used here is a bit pants, and you should probably not use it.
 * 
 * @author bradm
 */
public class ServletHttpManager extends HttpManager implements Initable {
    
    private static final Logger log = LoggerFactory.getLogger(ServletHttpManager.class);
    
    public ServletHttpManager(ResourceFactory resourceFactory, WebDavResponseHandler responseHandler, AuthenticationService authenticationService) {
        super(resourceFactory, responseHandler, authenticationService );
    }

    public ServletHttpManager(ResourceFactory resourceFactory, AuthenticationService authenticationService) {
        super(resourceFactory, authenticationService );
    }


    public ServletHttpManager(ResourceFactory resourceFactory) {
        super(resourceFactory);
    }
    
	@Override
    public void init(ApplicationConfig config,HttpManager manager) {
        log.debug("init");
        if( resourceFactory != null ) { 
            if( resourceFactory instanceof Initable ) {
                Initable i = (Initable)resourceFactory;
                i.init(config,manager);
            }
            for( String paramName : config.getInitParameterNames() ) {
                if( paramName.startsWith("filter_") ) {
                    String filterClass = config.getInitParameter(paramName);
                    log.debug("init filter: " + filterClass);
                    String[] arr = paramName.split("_");
                    String ordinal = arr[arr.length-1];
                    int pos = Integer.parseInt(ordinal);
                    initFilter(config, filterClass, pos);
                }
            }
        }
    }

    private void initFilter(final ApplicationConfig config, final String filterClass, final int pos) {
        try {
            Class c = Class.forName(filterClass);
            Filter filter = (Filter) c.newInstance();
            if( filter instanceof Initable ) {
                ((Initable)filter).init(config,this);
            }
            this.addFilter(pos,filter);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(filterClass,ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(filterClass,ex);
        } catch (InstantiationException ex) {
            throw new RuntimeException(filterClass,ex);
        }
    }


    @Override
    public void destroy(HttpManager manager) {
        log.debug("destroy");
        if( resourceFactory != null ) {
            if( resourceFactory instanceof Initable ) {
                Initable i = (Initable)resourceFactory;
                i.destroy(manager);
            }
        }
        shutdown();
    }
}
