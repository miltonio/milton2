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

package io.milton.http.acl;

import io.milton.http.AccessControlledResource;
import io.milton.http.webdav.ResourceTypeHelper;
import io.milton.resource.Resource;
import java.util.List;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author alex
 */
public class AccessControlledResourceTypeHelper implements ResourceTypeHelper {

    private static final Logger log = LoggerFactory.getLogger( AccessControlledResourceTypeHelper.class );
    private final ResourceTypeHelper wrapped;

    public AccessControlledResourceTypeHelper( ResourceTypeHelper wrapped ) {
        this.wrapped = wrapped;
    }

	@Override
    public List<QName> getResourceTypes( Resource r ) {
        List<QName> list = wrapped.getResourceTypes( r );
        return list;
    }

	@Override
    public List<String> getSupportedLevels( Resource r ) {
        log.trace( "getSupportedLevels" );
        List<String> list = wrapped.getSupportedLevels( r );
        if( r instanceof AccessControlledResource ) {
            list.add( "access-control" );
        }
        return list;
    }
}
