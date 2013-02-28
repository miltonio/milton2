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

package io.milton.http.values;

import io.milton.resource.AccessControlledResource.Priviledge;
import io.milton.http.XmlWriter;
import io.milton.http.XmlWriter.Element;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.principal.PriviledgeList;
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