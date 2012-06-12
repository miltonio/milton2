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
import io.milton.http.webdav.WebDavProtocol;
import java.util.Map;

/**
 * Supports HrefList objects, and writes them out as a list of <href>...</href> elements
 *
 * Currently readonly, but should support writing
 *
 * @author brad
 */
public class HrefListValueWriter implements ValueWriter {

	@Override
	public boolean supports(String nsUri, String localName, Class c) {
		boolean b = HrefList.class.isAssignableFrom(c);
		return b;
	}

	@Override
	public void writeValue(XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes) {		
		if (val instanceof HrefList) {
			Element outerEl = writer.begin(prefix, localName).open();
			HrefList list = (HrefList) val;
			if (list != null) {
				for (String s : list) {
					Element hrefEl = writer.begin(WebDavProtocol.DAV_PREFIX + ":href").open(false);
					hrefEl.writeText(s);
					hrefEl.close();
				}
			}
			outerEl.close();
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
