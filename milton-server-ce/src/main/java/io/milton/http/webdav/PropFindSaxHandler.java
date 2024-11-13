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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class PropFindSaxHandler extends DefaultHandler {

    private static final Logger log = LoggerFactory.getLogger(PropFindSaxHandler.class);

    private final Stack<QName> elementPath = new Stack<>();
    private final Map<QName, String> attributes = new HashMap<>();
    private final StringBuilder sb = new StringBuilder();
    private boolean inProp;
    private boolean allProp;
    private boolean propname;

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        if (!elementPath.isEmpty()) {
            String elname = elementPath.peek().getLocalPart();
            if (elname.equals("prop")) {
                inProp = true;
            }
        }
        if (localName.equals("allprop")) {
            allProp = true;
        }
        if (localName.equals("propname")) {
            propname = true;
        }

        QName qname = new QName(uri, localName);
        elementPath.push(qname);
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
        if (!elementPath.isEmpty() && elementPath.peek().getLocalPart().endsWith("prop")) {
            QName qname = new QName(uri, localName);
            getAttributes().put(qname, sb.toString().trim());
            sb.delete(0, sb.length());
        }

        super.endElement(uri, localName, name);
    }

    public Map<QName, String> getAttributes() {
        return attributes;
    }

    public boolean isAllProp() {
        return allProp;
    }

    public boolean isPropname() {
        return propname;
    }
}
