/*
 * Copyright 2012 McEvoy Software Ltd.
 */

package io.milton.http.webdav2;

import io.milton.resource.LockableResource;
import io.milton.http.XmlWriter;
import io.milton.http.XmlWriter.Element;
import io.milton.http.values.ValueWriter;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.http.webdav.WebDavProtocol.SupportedLocks;
import java.util.Map;

public class SupportedLockValueWriter implements ValueWriter {

	private static String D = WebDavProtocol.DAV_PREFIX;
	
	@Override
	public boolean supports(String nsUri, String localName, Class c) {
		return SupportedLocks.class.isAssignableFrom(c);
	}

	@Override
	public void writeValue(XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes) {
		Element supportedLocks = writer.begin(D + ":supportedlock").open();
		SupportedLocks slocks = (SupportedLocks) val;
		if (slocks != null && slocks.getResource() instanceof LockableResource) {
			Element lockentry = writer.begin(D + ":lockentry").open();
			writer.begin(D + ":lockscope").open(false).writeText("<" + D + ":exclusive/>").close();
			writer.begin(D + ":locktype").open(false).writeText("<" + D + ":write/>").close();
			lockentry.close();
		}
		supportedLocks.close();
	}

	@Override
	public Object parse(String namespaceURI, String localPart, String value) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
