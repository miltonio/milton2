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
