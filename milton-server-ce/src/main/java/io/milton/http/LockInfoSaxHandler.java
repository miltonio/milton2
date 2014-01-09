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
package io.milton.http;

import io.milton.http.LockInfo.LockDepth;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import io.milton.http.LockInfo.LockScope;
import io.milton.http.LockInfo.LockType;
import io.milton.principal.DiscretePrincipal;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class LockInfoSaxHandler extends DefaultHandler {

	private static final Logger log = LoggerFactory.getLogger(LockInfoSaxHandler.class);

	public static LockInfo parseLockInfo(Request request) throws IOException, FileNotFoundException, SAXException {
		InputStream in = request.getInputStream();

		XMLReader reader = XMLReaderFactory.createXMLReader();
		LockInfoSaxHandler handler = new LockInfoSaxHandler();
		reader.setContentHandler(handler);
		if (log.isDebugEnabled()) {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			IOUtils.copy(in, bout);
			byte[] bytes = bout.toByteArray();
			in = new ByteArrayInputStream(bytes);
			log.debug("LockInfo: " + bout.toString());
		}
		reader.parse(new InputSource(in));
		LockInfo info = handler.getInfo();
		info.depth = LockDepth.INFINITY; // todo
		if (info.lockedByUser == null) {
			if (request.getAuthorization() != null) {
				if (request.getAuthorization().getUser() != null) {
					info.lockedByUser = request.getAuthorization().getUser();
				} else {
					Object user = request.getAuthorization().getTag();
					if (user instanceof DiscretePrincipal) {
						DiscretePrincipal dp = (DiscretePrincipal) user;
						info.lockedByUser = dp.getPrincipalURL();
					}
				}

			}
		}
		if (info.lockedByUser == null) {
			log.warn("resource is being locked with a null user. This won't really be locked at all...");
		}
		return info;
	}

	private final LockInfo info = new LockInfo();
	private StringBuilder owner;
	private final Stack<String> elementPath = new Stack<String>();

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		elementPath.push(localName);
		if (localName.equals("owner")) {
			owner = new StringBuilder();
		}
		super.startElement(uri, localName, name, attributes);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (owner != null) {
			owner.append(ch, start, length);
		}
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		elementPath.pop();
		if (localName.equals("owner")) {
			getInfo().lockedByUser = owner.toString();
		}
		if (elementPath.size() > 1) {
			if (elementPath.get(1).equals("lockscope")) {
				if (localName.equals("exclusive")) {
					getInfo().scope = LockScope.EXCLUSIVE;
				} else if (localName.equals("shared")) {
					getInfo().scope = LockScope.SHARED;
				} else {
					getInfo().scope = LockScope.NONE;
				}
			} else if (elementPath.get(1).equals("locktype")) {
				if (localName.equals("read")) {
					getInfo().type = LockType.READ;
				} else if (localName.equals("write")) {
					getInfo().type = LockType.WRITE;
				} else {
					getInfo().type = LockType.WRITE;
				}
			}

		}
		super.endElement(uri, localName, name);
	}

	public LockInfo getInfo() {
		return info;
	}
}
