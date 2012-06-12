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

import io.milton.property.BeanPropertySource;
import io.milton.property.MultiNamespaceCustomPropertySource;
import io.milton.property.PropertySource;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class PropertySourceUtil {
    /**
     * Create default extension property sources. These are those additional
     * to the webdav default properties defined on the protocol itself
     *
     * @param resourceTypeHelper
     * @return
     */
    public static List<PropertySource> createDefaultSources(ResourceTypeHelper resourceTypeHelper) {
        List<PropertySource> list = new ArrayList<PropertySource>();
        MultiNamespaceCustomPropertySource mncps = new MultiNamespaceCustomPropertySource();
        list.add( mncps );
        BeanPropertySource beanPropertySource = new BeanPropertySource();
        list.add( beanPropertySource);
        return list;
    }
}
