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

package io.milton.http.json;

import io.milton.common.Path;
import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.GetableResource;
import io.milton.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class AjaxLoginResourceFactory implements ResourceFactory{

    private static final Logger log = LoggerFactory.getLogger( AjaxLoginResourceFactory.class );

    private String suffix = ".login";
    private final ResourceFactory wrapped;

    public AjaxLoginResourceFactory( ResourceFactory wrapped ) {
        this.wrapped = wrapped;
    }    
    
    public AjaxLoginResourceFactory( String suffix, ResourceFactory wrapped ) {
        this.suffix = suffix;
        this.wrapped = wrapped;
    }

    @Override
    public Resource getResource( String host, String path ) throws NotAuthorizedException, BadRequestException {
        if(path.endsWith( suffix )) {
            int i = path.lastIndexOf( suffix);
            String p2 = path.substring( 0, i);
            Resource r = wrapped.getResource( host, p2);
            if( r != null) {
                if( r instanceof GetableResource) {
                    GetableResource gr = (GetableResource) r;
                    Path pathFull = Path.path( path );
                    log.debug( "found an ajax resource, wrapping a: " + gr.getClass());
                    return new AjaxLoginResource( pathFull.getName(), gr );
                } else {
                    return r;
                }
            }
        }
        return wrapped.getResource( host, path );
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
    
    
}
