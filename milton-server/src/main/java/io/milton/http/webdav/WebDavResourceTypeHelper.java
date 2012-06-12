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

package io.milton.http.webdav;

import io.milton.resource.CollectionResource;
import io.milton.resource.LockableResource;
import io.milton.resource.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class WebDavResourceTypeHelper implements ResourceTypeHelper {

    private static final Logger log = LoggerFactory.getLogger( WebDavResourceTypeHelper.class );

    public List<QName> getResourceTypes( Resource r ) {
        if( r instanceof CollectionResource ) {
            ArrayList<QName> list = new ArrayList<QName>();
            QName qn = new QName( WebDavProtocol.NS_DAV.getName(), "collection" );
            list.add( qn );
            return list;
        } else {
            return null;
        }
    }

    //Need to create a ArrayList as Arrays.asList returns a fixed length list which
    //cannot be extended.
    public List<String> getSupportedLevels( Resource r ) {
        if( r instanceof LockableResource ) {
            return new ArrayList<String> (Arrays.asList( "1", "2" ));
        } else {
            return new ArrayList<String> (Arrays.asList( "1" ));
        }
    }
}
