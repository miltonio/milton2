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

import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.property.PropertySource.PropertyMetaData;
import io.milton.resource.Resource;
import java.util.List;
import javax.xml.namespace.QName;

/**
 * A resource interface similar to CustomPropertyResource, except that it doesnt
 * use accessor objects, and it supports multiple namespaces.
 *
 * Properties are requested with qualified names, QNames, which include both
 * a namespace and a local name.
 * 
 * To implement this you should decide on a namespace for your custom properties
 * and then look for that component of the QName when implementing
 *
 * @author brad
 */
public interface MultiNamespaceCustomPropertyResource extends Resource {
    Object getProperty( QName name );

	/**
	 * Update the property with the given typed value.
	 * 
	 * @param name - the qualified name of the property
	 * @param value - the new typed value
	 * @throws com.bradmcevoy.property.PropertySource.PropertySetException - if the input is invalid
	 * @throws NotAuthorizedException - if the current user is not allowed to set this value
	 */
    void setProperty( QName name, Object value ) throws PropertySource.PropertySetException, NotAuthorizedException;

	/**
	 * Get the metadata for the requested property, or return null if this
	 * implementation does not provide that property
	 * 
	 * It is also legitimate to return PropertyMetaData.UNKNOWN for unsupported properties
	 */
    PropertyMetaData getPropertyMetaData( QName name );

    List<QName> getAllPropertyNames();
}
