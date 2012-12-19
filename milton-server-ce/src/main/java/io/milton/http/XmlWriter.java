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

import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * Lightweight XML generation. Gives the programmer fine grained control of the
 * generated xml, including whitespace.
 * <P/>
 * The XML is not guaranteed to be parseable.
 *
 * @author brad
 */
public class XmlWriter {

	private Logger log = LoggerFactory.getLogger(XmlWriter.class);

	public static Charset utf8() {
		return Charset.forName("UTF-8");
	}

	public enum Type {

		OPENING,
		CLOSING,
		NO_CONTENT
	};
	private boolean allowNewlines = false;
	protected final OutputStream out;
	protected final Charset charset;

	public XmlWriter(OutputStream out, Charset charset) {
		this.out = out;
		this.charset = charset;
	}

	public XmlWriter(OutputStream out) {
		this.out = out;
		this.charset = XmlWriter.utf8();
	}

	/**
	 * Append the given raw String to the ouput. No encoding is applied
	 *
	 * @param value
	 */
	private void append(String value) {
		try {
			byte[] arr = value.getBytes(charset);
			out.write(arr);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Append the given character to the output. No encoding is applied
	 *
	 * @param c
	 */
	private void append(char c) {
		try {
			out.write(c);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Convenience method to write a single element containing a piece of text
	 *
	 *
	 * @param namespace - optional, namespace prefix
	 * @param namespaceInfo - optional, namespace url
	 * @param name - the local name of the element to create
	 * @param value - the raw text to insert into the element
	 */
	public void writeProperty(String namespace, String namespaceInfo, String name, String value) {
		writeElement(namespace, namespaceInfo, name, Type.OPENING);
		append(value);
		writeElement(namespace, namespaceInfo, name, Type.CLOSING);

	}

	public void writeProperty(String namespace, String name, String value) {
		if (value == null) {
			writeProperty(namespace, name);
		} else {
			writeElement(namespace, name, Type.OPENING);
			append(value);
			writeElement(namespace, name, Type.CLOSING);
		}
	}

	public void writeProperty(String namespace, String name) {
		writeElement(namespace, name, Type.NO_CONTENT);
	}

	public void writeProperty(String name) {
		writeElement(null, name, Type.NO_CONTENT);
	}

	public void writeElement(String namespace, String name, Type type) {
		writeElement(namespace, null, name, type);
	}

	/**
	 * Write an opening tag
	 *
	 * @param namespace
	 * @param name
	 */
	public void open(String namespace, String name) {
		writeElement(namespace, name, Type.OPENING);
	}

	/**
	 * Write a closing tag, Eg </name>
	 *
	 * @param namespace
	 * @param name
	 */
	public void close(String namespace, String name) {
		writeElement(namespace, name, Type.CLOSING);
	}

	/**
	 * Write an opening tag
	 *
	 * @param name
	 */
	public void open(String name) {
		writeElement(null, name, Type.OPENING);
	}

	/**
	 * Write a closing tag for the given name
	 *
	 * @param name
	 */
	public void close(String name) {
		writeElement(null, name, Type.CLOSING);
	}

	/**
	 * Represents an element which is currently being written
	 *
	 */
	public class Element {

		private final Element parent;
		private final String nsPrefix;
		private final String name;
		private boolean openEnded;

		/**
		 * Create the element and write the first part of the opening tag
		 *
		 * Eg <name
		 *
		 *
		 *
		 *
		 *

		 *
		 * @param name
		 */
		Element(Element parent, String name) {
			this(parent, null, name);
		}

		/**
		 * Create the element and write the first part of the opening tag
		 *
		 * Eg <name
		 *
		 *
		 *
		 *
		 *

		 *
		 * @param nsPrefix
		 * @param name
		 */
		Element(Element parent, String nsPrefix, String name) {
			this.parent = parent;
			this.name = name;
			this.nsPrefix = nsPrefix;
			append("<");
			if (nsPrefix != null) {
				append(nsPrefix);
				append(":");
			}
			append(name);
		}

		Element(Element parent, String uri, String nsPrefix, String name) {
			this.parent = parent;
			this.name = name;
			this.nsPrefix = nsPrefix;
			append("<");
			if (nsPrefix != null) {
				append(nsPrefix);
				append(":");
			}
			append(name);
			append(" ");
			append("xmlns:" + nsPrefix + "=\"");
			append(uri);
			append("\"");
		}

		/**
		 * Write a name/value attribute pair
		 *
		 * @param name
		 * @param value
		 * @return
		 */
		public Element writeAtt(String name, String value) {
			append(" ");
			append(name);
			append("=");
			append((char) 34);
			append(value == null ? "" : value);
			append((char) 34);
			return this;
		}

		/**
		 * Write the text into the element. Will finish the opening tag if
		 * required
		 *
		 * @param text
		 * @return
		 */
		public Element writeText(String text) {
			return writeText(text, true);
		}

		public Element writeText(String text, boolean newline) {
			if (!openEnded) {
				open(newline);
			}
			append(text);
			return this;
		}

		/**
		 * Completes the opening tag which is started in the constructor. And
		 * writes a new line
		 *
		 * Eg >
		 *
		 * @return
		 */
		public Element open() {
			return open(true);
		}

		public Element open(boolean newline) {
			openEnded = true;
			append(">");
			if (newline) {
				newLine();
			}
			return this;
		}

		/**
		 * Closes the tag by determining its current state. Can close with a
		 * no-content tag </name> if no content has been written, or with write
		 * a close tag
		 *
		 * @return - the parent element
		 */
		public Element close() {
			return close(false);
		}

		public Element close(boolean newline) {
			if (openEnded) {
				if (nsPrefix != null) {
					append("</" + nsPrefix + ":" + name + ">");
					newLine();
				} else {
					append("</" + name + ">");
					newLine();
				}
				if (newline) {
					newLine();
				}
				return parent;
			} else {
				if (newline) {
					newLine();
				}
				return noContent();
			}
		}

		/**
		 * Write a self closing tag, eg />
		 *
		 * @return - the parent element
		 */
		public Element noContent() {
			append("/>");
			newLine();
			return parent;
		}

		public Element noContent(boolean newLine) {
			append("/>");
			if (newLine) {
				newLine();
			}
			return parent;
		}

		/**
		 * Start a new element, completing the open tag if required
		 *
		 * @param name
		 * @return
		 */
		public Element begin(String name) {
			return begin(null, name);
		}

		public Element begin(String prefix, String name) {
			return begin(prefix, name, true);
		}

		public Element begin(String prefix, String name, boolean newLine) {
			if (!openEnded) {
				open(newLine);
			}

			Element el = new Element(this, prefix, name);
			return el;
		}

		public Element begin(String uri, String prefix, String name) {
			if (!openEnded) {
				open();
			}

			Element el = new Element(this, uri, prefix, name);
			return el;
		}

		/**
		 * Write a property element like - <name>value</name>
		 *
		 * @param name
		 * @param value
		 * @return
		 */
		public Element prop(String name, String value) {
			begin(name).writeText(value, false).close(true);
			return this;
		}

		public Element prop(String name, Integer value) {
			if (value != null) {
				prop(name, value.toString());
			} else {
				begin(name).noContent();
			}
			return this;
		}
	}

	public Element begin(String name) {
		Element el = new Element(null, name);
		return el;
	}

	public Element begin(String nsPrefix, String name) {
		Element el = new Element(null, nsPrefix, name);
		return el;
	}

	public void writeElement(String nsPrefix, String nsUrl, String name, Type type) {
		if ((nsPrefix != null) && (nsPrefix.length() > 0)) {
			switch (type) {
				case OPENING:
					if (nsUrl != null) {
						append("<" + nsPrefix + ":" + name + " xmlns:" + nsPrefix + "=\"" + nsUrl + "\">");
					} else {
						append("<" + nsPrefix + ":" + name + ">");
					}
					break;
				case CLOSING:
					append("</" + nsPrefix + ":" + name + ">");
					newLine();
					break;
				case NO_CONTENT:
				default:
					if (nsUrl != null) {
						append("<" + nsPrefix + ":" + name + " xmlns:" + nsPrefix + "=\"" + nsUrl + "\"/>");
					} else {
						append("<" + nsPrefix + ":" + name + "/>");
					}
					break;
			}
		} else {
			switch (type) {
				case OPENING:
					append("<" + name + ">");
					break;
				case CLOSING:
					append("</" + name + ">\n");
					break;
				case NO_CONTENT:
				default:
					append("<" + name + "/>");
					break;
			}
		}
	}

	/**
	 * Append plain text.
	 *
	 * @param text Text to append
	 */
	public void writeText(String text) {
		append(text);
	}

	/**
	 * Write a CDATA segment.
	 *
	 * @param data Data to append
	 */
	public void writeData(String data) {
		append("<![CDATA[" + data + "]]>");
	}

	public void writeXMLHeader() {
		append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
	}

	/**
	 * Send data and reinitializes buffer.
	 */
	public void flush() {
		try {
			out.flush();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void newLine() {
		if (allowNewlines) {
			append("\n");
		}
	}

	public boolean isAllowNewlines() {
		return allowNewlines;
	}

	public void setAllowNewlines(boolean allowNewlines) {
		this.allowNewlines = allowNewlines;
	}
}
