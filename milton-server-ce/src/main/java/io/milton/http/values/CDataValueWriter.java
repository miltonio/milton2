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

import io.milton.common.Utils;
import io.milton.http.XmlWriter;
import java.util.Map;

/**
 *
 * @author brad
 */
public class CDataValueWriter implements ValueWriter {

	public static void appendCDataString(final StringBuilder sb, final String data) {
		final String cdStart = "<![CDATA[";
		final String cdEnd = "]]>";

		sb.append(cdStart);
		int offset = 0;
		int idx = -1;
		while ((idx = data.indexOf(cdEnd, offset)) >= 0) {
			if (offset < idx) {
				sb.append(data.substring(offset, idx));
			}
			sb.append("]]").append(cdEnd).append(cdStart).append(">");
			offset = idx + cdEnd.length();
		}
		if (offset < data.length()) {
			sb.append(data.substring(offset));
		}
		sb.append(cdEnd);
	}	
	
	@Override
	public boolean supports(String nsUri, String localName, Class c) {
		return CData.class.equals(c);
	}


	@Override
	public void writeValue(XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes) {
		if (val == null) {
			writer.writeProperty(prefix, localName);
		} else {
			CData cd = (CData) val;
			StringBuilder sb = new StringBuilder();
			CDataValueWriter.appendCDataString(sb, cd.getData());
			String s = sb.toString();
			//String s = nameEncode(cd.getData());
			//s = "<![CDATA[" + s + "]]>";
			writer.writeProperty(prefix, localName, s);
		}
	}

    private String nameEncode( String s ) {
        //return Utils.encode(href, false); // see MIL-31
        return Utils.escapeXml( s );
    }	
	
	@Override
	public Object parse(String namespaceURI, String localPart, String value) {
		return value;
	}

}
