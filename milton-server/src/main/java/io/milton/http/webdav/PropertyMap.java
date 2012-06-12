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
