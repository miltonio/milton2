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

import io.milton.http.Response;
import io.milton.http.Response.Status;
import io.milton.http.XmlWriter;
import io.milton.http.values.ValueAndType;
import io.milton.http.values.ValueWriters;
import io.milton.http.webdav.PropFindResponse.NameAndError;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 *
 * @author bradm
 */
public class PropFindXmlGeneratorHelper {
	private ValueWriters valueWriters;

	public PropFindXmlGeneratorHelper() {
	}
	
	
	public PropFindXmlGeneratorHelper(ValueWriters valueWriters) {
		this.valueWriters = valueWriters;
	}

	/**
	 *
	 * @param propFindResponses
	 * @return - map where key is the uri, and value is the prefix
	 */
	Map<String, String> findNameSpaces(List<PropFindResponse> propFindResponses) {
		int i = 1;
		Map<String, String> map = new HashMap<String, String>();
		// always add webdav namespace
		map.put(WebDavProtocol.NS_DAV.getName(), WebDavProtocol.NS_DAV.getPrefix());
		// Hack for caldav!!! Temporary only!!!
		//xmlns:cal="urn:ietf:params:xml:ns:caldav" xmlns:cs="http://calendarserver.org/ns/"
		map.put("urn:ietf:params:xml:ns:caldav", "cal");
		map.put("http://calendarserver.org/ns/", "cs");
		map.put("urn:ietf:params:xml:ns:carddav", "card");
		for (PropFindResponse r : propFindResponses) {
			for (QName p : r.getKnownProperties().keySet()) {
				String uri = p.getNamespaceURI();
				//                    if( uri.endsWith( ":" ) ) uri = uri.substring( 0, uri.length() - 1 ); // strip trailing :
				if (!map.containsKey(uri)) {
					map.put(uri, "ns" + i++);
				}
			}
		}
		return map;
	}

	String generateNamespaceDeclarations(Map<String, String> mapOfNamespaces) {
		String decs = "";
		for (String uri : mapOfNamespaces.keySet()) {
			String prefix = mapOfNamespaces.get(uri);
			decs += " xmlns:" + prefix + "=\"" + uri + "\"";
		}
		return decs;
	}

	void appendResponses(XmlWriter writer, List<PropFindResponse> propFindResponses, Map<String, String> mapOfNamespaces) {
		//            log.debug( "appendResponses: " + propFindResponses.size() );
		for (PropFindResponse r : propFindResponses) {
			appendResponse(writer, r, mapOfNamespaces);
		}
	}

	public void appendResponse(XmlWriter writer, PropFindResponse r, Map<String, String> mapOfNamespaces) {
		XmlWriter.Element el = writer.begin(WebDavProtocol.NS_DAV.getPrefix(), "response");
		el.open();
		writer.writeProperty(WebDavProtocol.NS_DAV.getPrefix(), "href", r.getHref());
		sendKnownProperties(writer, mapOfNamespaces, r.getKnownProperties(), r.getHref());
		if (r.getErrorProperties() != null) {
			for (Status status : r.getErrorProperties().keySet()) {
				List<NameAndError> props = r.getErrorProperties().get(status);
				sendErrorProperties(status, writer, mapOfNamespaces, props);
			}
		}
		el.close();
	}

	private void sendKnownProperties(XmlWriter writer, Map<String, String> mapOfNamespaces, Map<QName, ValueAndType> properties, String href) {
		sendProperties(Response.Status.SC_OK, writer, mapOfNamespaces, properties, href);
	}

	private void sendProperties(Response.Status status, XmlWriter writer, Map<String, String> mapOfNamespaces, Map<QName, ValueAndType> properties, String href) {
		if (!properties.isEmpty()) {
			XmlWriter.Element elPropStat = writer.begin(WebDavProtocol.NS_DAV.getPrefix(), "propstat").open();
			XmlWriter.Element elProp = writer.begin(WebDavProtocol.NS_DAV.getPrefix(), "prop").open();
			for (QName qname : properties.keySet()) {
				String prefix = mapOfNamespaces.get(qname.getNamespaceURI());
				ValueAndType val = properties.get(qname);
				valueWriters.writeValue(writer, qname, prefix, val, href, mapOfNamespaces);
			}
			elProp.close();
			writer.writeProperty(WebDavProtocol.NS_DAV.getPrefix(), "status", status.toString());
			elPropStat.close();
		}
	}

	private void sendErrorProperties(Response.Status status, XmlWriter writer, Map<String, String> mapOfNamespaces, List<NameAndError> properties) {
		//            log.debug( "sendUnknownProperties: " + properties.size() );
		if (!properties.isEmpty()) {
			XmlWriter.Element elPropStat = writer.begin(WebDavProtocol.NS_DAV.getPrefix(), "propstat").open();
			XmlWriter.Element elProp = writer.begin(WebDavProtocol.NS_DAV.getPrefix(), "prop").open();
			for (NameAndError ne : properties) {
				QName qname = ne.getName();
				String prefix = mapOfNamespaces.get(qname.getNamespaceURI());
				writer.writeProperty(prefix, qname.getLocalPart());
			}
			elProp.close();
			writer.writeProperty(WebDavProtocol.NS_DAV.getPrefix(), "status", status.toString());
			elPropStat.close();
		}
	}

	void write(ByteArrayOutputStream out, OutputStream outputStream) {
		try {
			String xml = out.toString("UTF-8");
			outputStream.write(xml.getBytes()); // note: this can and should write to the outputstream directory. but if it aint broke, dont fix it...
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public ValueWriters getValueWriters() {
		return valueWriters;
	}

	public void setValueWriters(ValueWriters valueWriters) {
		this.valueWriters = valueWriters;
	}
	
	
}
