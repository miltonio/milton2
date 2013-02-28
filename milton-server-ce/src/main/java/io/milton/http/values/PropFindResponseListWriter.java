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

import io.milton.http.XmlWriter;
import io.milton.http.XmlWriter.Element;
import io.milton.http.webdav.PropFindResponse;
import io.milton.http.webdav.PropFindXmlGeneratorHelper;
import java.util.Map;

/**
 *
 * @author bradm
 */
public class PropFindResponseListWriter  implements ValueWriter {

	private final PropFindXmlGeneratorHelper propFindXmlGeneratorHelper;

	public PropFindResponseListWriter(PropFindXmlGeneratorHelper propFindXmlGeneratorHelper) {
		this.propFindXmlGeneratorHelper = propFindXmlGeneratorHelper;
	}
	
	
	@Override
	public boolean supports(String nsUri, String localName, Class c) {
		return PropFindResponseList.class.isAssignableFrom(c);
	}

	@Override
	public void writeValue(XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes) {
		Element outerEl = writer.begin(prefix, localName).open();
		PropFindResponseList list = (PropFindResponseList) val;
		if (list != null) {
			for (PropFindResponse s : list) {
				propFindXmlGeneratorHelper.appendResponse(writer, s, nsPrefixes);
			}
		}
		outerEl.close();
	}

	@Override
	public Object parse(String namespaceURI, String localPart, String value) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
