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

import io.milton.http.AccessControlledResource.Priviledge;
import io.milton.http.XmlWriter;
import io.milton.http.XmlWriter.Element;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.http.acl.PriviledgeList;
import java.util.EnumMap;
import java.util.Map;

/**
 * Supports PriviledgeList objects, and writes them out as a list of
 * <privilege>...</privilege> elements.
 *
 * Currently readonly but should support writing.
 *
 * @author avuillard
 */
public class PriviledgeListValueWriter implements ValueWriter {

	private static Map<Priviledge, String> priviledgeToStringMap = PriviledgeListValueWriter.initPriviledgeToStringMap();

	private static Map<Priviledge, String> initPriviledgeToStringMap() {
		Map<Priviledge, String> map = new EnumMap<Priviledge, String>(Priviledge.class);
		map.put(Priviledge.READ, "read");
		map.put(Priviledge.WRITE, "write");
		map.put(Priviledge.READ_ACL, "read-acl");
		map.put(Priviledge.WRITE_ACL, "write-acl");
		map.put(Priviledge.UNLOCK, "unlock");
		map.put(Priviledge.READ_CURRENT_USER_PRIVILEDGE, "read-current-user-privilege-set");
		map.put(Priviledge.WRITE_PROPERTIES, "write-properties");
		map.put(Priviledge.WRITE_CONTENT, "write-content");
		map.put(Priviledge.BIND, "bind");
		map.put(Priviledge.UNBIND, "unbind");
		map.put(Priviledge.ALL, "all");
		return map;
	}

	@Override
	public boolean supports(String nsUri, String localName, Class c) {
		return PriviledgeList.class.isAssignableFrom(c);
	}

	@Override
	public void writeValue(XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes) {
		if (val instanceof PriviledgeList) {
			PriviledgeList list = (PriviledgeList) val;
			Element outerEl = writer.begin(prefix, localName).open();
			if (list != null) {
				for (Priviledge p : list) {
					String privilegeString = PriviledgeListValueWriter.priviledgeToStringMap.get(p);
					if (privilegeString == null) {
						continue;
					}

					Element privilegeEl = writer.begin(WebDavProtocol.DAV_PREFIX + ":privilege").open(false);
					Element privilegeValueEl = privilegeEl.begin(WebDavProtocol.DAV_PREFIX, privilegeString);
					privilegeValueEl.noContent();
					privilegeEl.close();
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