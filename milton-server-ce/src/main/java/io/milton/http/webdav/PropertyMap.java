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

package io.milton.http.webdav;

import io.milton.resource.PropFindableResource;
import io.milton.resource.Resource;
import io.milton.property.PropertySource.PropertyAccessibility;
import io.milton.property.PropertySource.PropertyMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

/**
 * Helper class for PropertySources. Consists of a map of StandardProperty
 * instances, keyed by property name
 *
 * @author brad
 */
public class PropertyMap {

	private final Map<String, StandardProperty> writersMap = new HashMap<String, StandardProperty>();
	private final String nameSpace;

	public PropertyMap(String nameSpace) {
		this.nameSpace = nameSpace;
	}

	public boolean hasProperty(QName name) {
		if (!name.getNamespaceURI().equals(nameSpace)) {
			return false;
		}
		StandardProperty pa = writersMap.get(name.getLocalPart());
		if (pa == null) {
			return false;
		} else {
			return true;
		}
	}

	public Object getProperty(QName name, Resource r) {
		if (!name.getNamespaceURI().equals(nameSpace)) {
			return null;
		}
		StandardProperty pa = writersMap.get(name.getLocalPart());
		if (pa == null) {
			return null;
		}
		if (r instanceof PropFindableResource) {
			return pa.getValue((PropFindableResource) r);
		} else {
			return null;
		}
	}
	
	public void setProperty(QName name, Resource r, Object val) {
		if (!name.getNamespaceURI().equals(nameSpace)) {
			return;
		}
		StandardProperty pa = writersMap.get(name.getLocalPart());
		if (pa == null) {
			return;
		} else if( !(pa instanceof WritableStandardProperty)) {
			return ;
		}
		WritableStandardProperty wsp = (WritableStandardProperty) pa;
		if (r instanceof PropFindableResource) {
			wsp.setValue((PropFindableResource) r, val);
		} else {
			return;
		}
	}	

	public PropertyMetaData getPropertyMetaData(QName name, Resource r) {
		if (!name.getNamespaceURI().equals(nameSpace)) {
			return PropertyMetaData.UNKNOWN;
		}
		StandardProperty pa = writersMap.get(name.getLocalPart());
		if (pa == null) {
			return PropertyMetaData.UNKNOWN;
		} else {
			if (r instanceof PropFindableResource) {
				if (pa instanceof WritableStandardProperty) {
					return new PropertyMetaData(PropertyAccessibility.WRITABLE, pa.getValueClass());
				} else {
					return new PropertyMetaData(PropertyAccessibility.READ_ONLY, pa.getValueClass());
				}
			} else {
				return PropertyMetaData.UNKNOWN;
			}
		}
	}

	public List<QName> getAllPropertyNames(Resource r) {
		List<QName> list = new ArrayList<QName>();
		for (String nm : this.writersMap.keySet()) {
			QName qname = new QName(WebDavProtocol.NS_DAV.getName(), nm);
			list.add(qname);
		}
		return list;
	}

	public void add(StandardProperty pw) {
		writersMap.put(pw.fieldName(), pw);
	}

	public interface StandardProperty<T> {

		String fieldName();

		T getValue(PropFindableResource res);

		Class getValueClass();
	}

	public interface WritableStandardProperty<T> extends StandardProperty<T> {

		void setValue(PropFindableResource res, T value);
	}
}
