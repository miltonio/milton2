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

import io.milton.resource.LockableResource;
import io.milton.http.XmlWriter;
import io.milton.http.XmlWriter.Element;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.http.webdav.WebDavProtocol.SupportedLocks;
import java.util.Map;

public class SupportedLockValueWriter implements ValueWriter {

	private static String D = WebDavProtocol.DAV_PREFIX;
	
	@Override
	public boolean supports(String nsUri, String localName, Class c) {
		return SupportedLocks.class.isAssignableFrom(c);
	}

	@Override
	public void writeValue(XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes) {
		Element supportedLocks = writer.begin(D + ":supportedlock").open();
		SupportedLocks slocks = (SupportedLocks) val;
		if (slocks != null && slocks.getResource() instanceof LockableResource) {
			Element lockentry = writer.begin(D + ":lockentry").open();
			writer.begin(D + ":lockscope").open(false).writeText("<" + D + ":exclusive/>").close();
			writer.begin(D + ":locktype").open(false).writeText("<" + D + ":write/>").close();
			lockentry.close();
		}
		supportedLocks.close();
	}

	@Override
	public Object parse(String namespaceURI, String localPart, String value) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
