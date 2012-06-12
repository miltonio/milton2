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

package io.milton.http.values;

import io.milton.http.XmlWriter;
import io.milton.http.XmlWriter.Element;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceTypeValueWriter implements ValueWriter {

	private static final Logger log = LoggerFactory.getLogger(ResourceTypeValueWriter.class);
	
	private Map<String,String> prefixes = new HashMap<String, String>();

	public ResourceTypeValueWriter() {
	}
	
	

	@Override
	public boolean supports(String nsUri, String localName, Class c) {
		return localName.equals("resourcetype");
	}

	@Override
	public void writeValue(XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes) {
		List<QName> list = (List<QName>) val;
		if (list != null && list.size() > 0) {
			Element rt = writer.begin(prefix, localName);
			for (QName name : list) {
				String childNsUri = name.getNamespaceURI();
				String childPrefix = nsPrefixes.get(childNsUri);
				// might be null if the namespace is on a value qname but not a property (eg caldav resource type)
				// so if null write the full uri
				if (childPrefix == null) {
					String p = lookupUnspecifiedPrefix(childNsUri);
					rt.begin(childNsUri, p, name.getLocalPart()).noContent(false);
				} else {
					// don't write a new line - see http://www.ettrema.com:8080/browse/MIL-83
					rt.begin(childPrefix, name.getLocalPart()).noContent(false);
				}
			}
			rt.close();
		} else {
			writer.writeProperty(prefix, localName);
		}
	}

	@Override
	public Object parse(String namespaceURI, String localPart, String value) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	private String lookupUnspecifiedPrefix(String childNsUri) {
		String p = prefixes.get(childNsUri);
		if( p != null ) {
			return p;
		}
		int i = 1;
		while(prefixes.containsKey("P" + i)) {
			i++;
		}
		p = "P" + i;
		prefixes.put(childNsUri, p);
		return p;
	}
}
