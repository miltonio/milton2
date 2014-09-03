/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.milton.http.values;

import io.milton.http.XmlWriter;
import io.milton.http.webdav.SupportedLocks;
import java.util.Map;

/**
 *
 * @author brad
 */
public class SupportedLocksValueWriter implements ValueWriter {

	@Override
	public boolean supports(String nsUri, String localName, Class c) {
		return SupportedLocks.class.isAssignableFrom(c);
	}

	@Override
	public void writeValue(XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes) {
		if (val instanceof SupportedLocks) {
			SupportedLocks list = (SupportedLocks) val; // Ignored
			XmlWriter.Element outerEl = writer.begin(prefix, localName).open();
			XmlWriter.Element lockEntry = outerEl.begin(prefix, "lockentry");
			lockEntry.begin(prefix, "lockscope").begin(prefix, "exclusive").close().close();
			lockEntry.begin(prefix, "locktype").begin(prefix, "write").close().close();
			//lockEntry.writeText("<D:lockscope><D:exclusive/></D:lockscope>", true);
			//lockEntry.writeText("<D:locktype><D:write/></D:locktype>", true);			
			lockEntry.close();
			outerEl.close();
		} else {
			if (val != null) {
				throw new RuntimeException("Value is not correct type. Is a: " + val.getClass());
			}
		}
	}

	@Override
	public Object parse(String namespaceURI, String localPart, String value) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}
