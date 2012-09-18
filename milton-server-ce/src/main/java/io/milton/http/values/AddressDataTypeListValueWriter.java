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
