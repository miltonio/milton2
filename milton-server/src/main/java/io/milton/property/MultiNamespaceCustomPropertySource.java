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

package io.milton.property;

import io.milton.resource.MultiNamespaceCustomPropertyResource;
import io.milton.resource.Resource;
import io.milton.http.exceptions.NotAuthorizedException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

/**
 *
 * @author brad
 */
public class MultiNamespaceCustomPropertySource implements PropertySource{

    public Object getProperty( QName name, Resource r ) {
        MultiNamespaceCustomPropertyResource cpr = (MultiNamespaceCustomPropertyResource) r;
        return cpr.getProperty( name );
    }

    public void setProperty( QName name, Object value, Resource r ) throws PropertySetException, NotAuthorizedException {
        MultiNamespaceCustomPropertyResource cpr = (MultiNamespaceCustomPropertyResource) r;
        cpr.setProperty( name, value );
    }

    public PropertyMetaData getPropertyMetaData( QName name, Resource r ) {
        if( r instanceof MultiNamespaceCustomPropertyResource ) {
            MultiNamespaceCustomPropertyResource cpr = (MultiNamespaceCustomPropertyResource) r;
            return cpr.getPropertyMetaData( name );
        } else {
            return null;
        }
    }

    /**
     * Just calls setProperty(.. null ..);
     *
     * @param name
     * @param r
     */
    public void clearProperty( QName name, Resource r ) throws PropertySetException, NotAuthorizedException {
        setProperty( name, null, r);
    }

    public List<QName> getAllPropertyNames( Resource r ) {
        if( r instanceof MultiNamespaceCustomPropertyResource ) {
            MultiNamespaceCustomPropertyResource cpr = (MultiNamespaceCustomPropertyResource) r;
            return cpr.getAllPropertyNames();
        } else {
            return new ArrayList<QName>();
        }

    }
}
