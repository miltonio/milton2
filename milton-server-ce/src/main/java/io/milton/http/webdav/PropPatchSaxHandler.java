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
package io.milton.http.webdav;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PropPatchSaxHandler extends DefaultHandler {

	private final static Logger log = LoggerFactory.getLogger(PropPatchSaxHandler.class);
	private Stack<String> elementPath = new Stack<String>();
	private Map<QName, String> attributesCurrent; // will switch between the following
	private Map<QName, String> attributesSet = new LinkedHashMap<QName, String>();
	private Map<QName, String> attributesRemove = new LinkedHashMap<QName, String>();
	private StringBuilder sb = new StringBuilder();
	private boolean inProp;

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		if (inProp) {
			sb.append("<" + localName + ">");
		}
		if (elementPath.size() > 0) {
			String elName = elementPath.peek();
			if (attributesCurrent != null) {
				if (elName.endsWith("prop")) {
					inProp = true;
				}
			} else {
				if (elName.endsWith("set")) {
					attributesCurrent = attributesSet;
				} else if (elName.endsWith("remove")) {
					attributesCurrent = attributesRemove;
				}
			}

		}
		elementPath.push(localName);
		super.startElement(uri, localName, name, attributes);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (inProp) {
			sb.append(ch, start, length);
		}
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		elementPath.pop();
		if (elementPath.size() > 0) {
			if (elementPath.peek().endsWith("prop")) {
				if (sb != null) {
					String s = sb.toString().trim();
					QName qname = new QName(uri, localName);
					if (attributesCurrent != null) {
						// will usually have this because of the set or remove element
						attributesCurrent.put(qname, s);
					} else {
						// but for mkcalendar there's no set or remove element so default to set attributes
						attributesSet.put(qname, s);
					}
				}
				sb = new StringBuilder();
			} else {
				if (inProp) {
					sb.append("</" + localName + ">");
				}

				if (elementPath.peek().endsWith("set")) {
					attributesCurrent = null;
				} else if (elementPath.peek().endsWith("remove")) {
					attributesCurrent = null;
				}
			}

		}

		super.endElement(uri, localName, name);
	}

	public Map<QName, String> getAttributesToSet() {
		return attributesSet;
	}

	public Map<QName, String> getAttributesToRemove() {
		return attributesRemove;
	}
}
