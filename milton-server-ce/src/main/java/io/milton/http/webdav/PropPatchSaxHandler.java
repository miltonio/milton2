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
			if (attributesCurrent != null) {
				if (elementPath.peek().endsWith("prop")) {
					inProp = true;
				}
			} else {
				if (elementPath.peek().endsWith("set")) {
					attributesCurrent = attributesSet;
				}
				if (elementPath.peek().endsWith("remove")) {
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
					attributesCurrent.put(qname, s);
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
