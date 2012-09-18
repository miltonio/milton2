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
import java.util.Map;

/**
 * Supports AddressDataTypeList objects, and writes them out as a list of  
 * <C:address-data-type content-type="text/vcard" version="3.0"/> 
 * elements
 * 
 * @author nabil.shams
 */
public class AddressDataTypeListValueWriter implements ValueWriter {

	@Override
	public boolean supports(String nsUri, String localName, Class c) {
		boolean b = AddressDataTypeList.class.isAssignableFrom(c);
		return b;
	}

	@Override
	public void writeValue(XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes) {		
		if (val instanceof AddressDataTypeList) {
			Element parent = writer.begin(prefix, localName).open();
			AddressDataTypeList list = (AddressDataTypeList) val;
			if (list != null) {
				for (Pair<String, String> pair : list) {
					Element child = writer.begin(prefix + ":address-data-type").open(false);
					child.writeAtt("content-type", pair.getObject1());
					child.writeAtt("version", pair.getObject2());
					child.close();
				}
			}
			parent.close();
		} else {
			if (val != null) {
				throw new RuntimeException("Value is not correct type. Is a: " + val.getClass());
			}
		}
	}

	@Override
	public Object parse(String namespaceURI, String localPart, String value) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
