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

import io.milton.http.HttpManager;
import io.milton.http.MultipleResourceFactory;
import io.milton.http.ResourceFactory;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class InitableMultipleResourceFactory extends MultipleResourceFactory {

    private Logger log = LoggerFactory.getLogger(InitableMultipleResourceFactory.class);

    public InitableMultipleResourceFactory() {
        super();
    }

    public InitableMultipleResourceFactory( List<ResourceFactory> factories ) {
        super( factories );
    }

    public void init(ApplicationConfig config, HttpManager manager) {
        String sFactories = config.getInitParameter("resource.factory.multiple");
        init(sFactories, config, manager);
    }


    protected void init(String sFactories,ApplicationConfig config, HttpManager manager) {
        log.debug("init: " + sFactories );
        String[] arr = sFactories.split(",");
        for(String s : arr ) {
            createFactory(s,config,manager);
        }
    }

    private void createFactory(String s,ApplicationConfig config, HttpManager manager) {
        log.debug("createFactory: " + s);
        Class c;
        try {
            c = Class.forName(s);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(s,ex);
        }
        Object o;
        try {
            o = c.newInstance();
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(s,ex);
        } catch (InstantiationException ex) {
            throw new RuntimeException(s,ex);
        }
        ResourceFactory rf = (ResourceFactory) o;
        if( rf instanceof Initable ) {
            Initable i = (Initable)rf;
            i.init(config,manager);
        }
        factories.add(rf);
    }
    

    public void destroy(HttpManager manager) {
        if( factories == null ) {
            log.warn("factories is null");
            return ;
        }
        for( ResourceFactory f : factories ) {
            if( f instanceof Initable ) {
                ((Initable)f).destroy(manager);
            }
        }
    }
}
