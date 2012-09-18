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

package io.milton.resource;

import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.property.PropertySource;
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
