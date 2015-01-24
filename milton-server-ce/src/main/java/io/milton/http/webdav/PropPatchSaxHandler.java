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

public class PropPatchSaxHandler extends DefaultHandler
{
	private final static Logger log = LoggerFactory.getLogger(PropPatchSaxHandler.class);
	private final static QName SET = new QName(WebDavProtocol.DAV_URI, "set");
	private final static QName REMOVE = new QName(WebDavProtocol.DAV_URI, "remove");
	private final static QName PROP = new QName(WebDavProtocol.DAV_URI, "prop");
	private final Stack<StateHandler> handlers = new Stack<StateHandler>();
	private final Map<QName, String> attributesSet = new LinkedHashMap<QName, String>();
	private final Map<QName, String> attributesRemove = new LinkedHashMap<QName, String>();

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException
	{
		QName qname = new QName(uri, localName);
		StateHandler handler;
		if (!handlers.isEmpty())
			handler = handlers.peek().startChild(qname, attributes);
		else
			handler = new Root(attributesSet, attributesRemove);
		handlers.push(handler);
		super.startElement(uri, localName, name, attributes);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		handlers.peek().characters(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException
	{
		QName qname = new QName(uri, localName);
		StateHandler handler = handlers.pop();
		handler.endSelf(qname);
		super.endElement(uri, localName, name);
	}

	public Map<QName, String> getAttributesToSet()
	{
		return attributesSet;
	}

	public Map<QName, String> getAttributesToRemove()
	{
		return attributesRemove;
	}

	/**
	 * Abstract class to handle SAX events for the various states
	 * that we expect to encounter while parsing a PROPATCH XML
	 * document.
	 */
	private static abstract class StateHandler
	{
		public abstract StateHandler startChild(QName name, Attributes attributes)
				throws SAXException;

		public void endSelf(QName name)
				throws SAXException
		{
		}

		public void characters(char[] ch, int start, int length)
				throws SAXException
		{
		}

		public static StateHandler Ignore = new StateHandler()
		{
			@Override
			public StateHandler startChild(QName name, Attributes attributes) throws SAXException
			{
				return this;
			}
		};
	}

	/**
	 * State handler for the root element of a PROPPATCH request. This
	 * expects to get either a "set" or "remove" element and ignores
	 * any other content.
	 */
	private static class Root extends StateHandler
	{
		private final Map<QName, String> set;
		private final Map<QName, String> remove;

		private Root(Map<QName, String> set, Map<QName, String> remove)
		{
			this.set = set;
			this.remove = remove;
		}

		@Override
		public StateHandler startChild(QName name, Attributes attributes) throws SAXException
		{
			if (name.equals(SET))
				return new Op(set);
			if (name.equals(REMOVE))
				return new Op(remove);
			return StateHandler.Ignore;
		}
	}

	/**
	 * State handler for a "set" or "remove" child or a PROPPATCH request.
	 * This expects to get a "prop" as a child element and ignores all
	 * others.
	 */
	private static class Op extends StateHandler
	{
		private final Map<QName, String> values;

		private Op(Map<QName, String> values)
		{
			this.values = values;
		}

		@Override
		public StateHandler startChild(QName name, Attributes attributes) throws SAXException
		{
			if (name.equals(PROP))
				return new Prop(values);
			return StateHandler.Ignore;
		}
	}

	/**
	 * State handler for the "prop" element beneath a "set" or "remove" in
	 * a PROPPATCH request. It treats children as individual attributes
	 * to be recorded in it's value map.
	 */
	private static class Prop extends StateHandler
	{
		private final Map<QName, String> values;

		private Prop(Map<QName, String> values)
		{
			this.values = values;
		}

		@Override
		public StateHandler startChild(QName name, Attributes attributes) throws SAXException
		{
			return new Attribute(values);
		}
	}

	/**
	 * Attribute to be set or removed in a PROPPATCH request. It's content
	 *  and any child elements will be stringified and added to either the
	 *  set or remove attribute maps.
	 */
	private static class Attribute extends StateHandler
	{
		private final Map<QName, String> values;
		private final Content content = new Content();

		private Attribute(Map<QName, String> values)
		{
			this.values = values;
		}

		@Override
		public StateHandler startChild(QName name, Attributes attributes) throws SAXException
		{
			content.startChild(name, attributes);
			return content;
		}

		@Override
		public void endSelf(QName name) throws SAXException
		{
			values.put(name, content.getValue().trim());
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException
		{
			content.characters(ch, start, length);
		}
	}

	/**
	 * Attribute content. All character data and sub-elements are
	 *  appended to a string buffer.
	 */
	private static class Content extends StateHandler
	{
		private final StringBuilder value = new StringBuilder();

		@Override
		public StateHandler startChild(QName name, Attributes attributes) throws SAXException
		{
			value.append("<").append(name.getLocalPart());
			if(attributes != null)
			{
				for(int i = 0; i < attributes.getLength(); i++)
				{
					value.append(" ");
					value.append(attributes.getLocalName(i));
					value.append("=\"");
					value.append(attributes.getValue(i));
					value.append("\"");
				}
			}
			value.append(">");
			return this;
		}

		@Override
		public void endSelf(QName name) throws SAXException
		{
			if(value.charAt(value.length() - 1) == '>')
			{
				value.insert(value.length() - 1, '/');
			}
			else
			{
				value.append("</").append(name.getLocalPart()).append(">");
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException
		{
			value.append(new String(ch, start, length));
		}

		public String getValue()
		{
			return value.toString();
		}
	}
}
