/*
 * Copyright 2012 McEvoy Software Ltd.
 */
package io.milton.http.webdav2;

import io.milton.webdav.utils.LockUtils;
import io.milton.http.LockInfo;
import io.milton.http.LockToken;
import io.milton.http.XmlWriter;
import io.milton.http.XmlWriter.Element;
import io.milton.http.values.ValueWriter;
import io.milton.http.webdav.WebDavProtocol;
import java.util.Map;

public class LockTokenValueWriter implements ValueWriter {


    @Override
    public boolean supports(String nsUri, String localName, Class c) {
        return LockToken.class.isAssignableFrom(c);
    }

    @Override
    public void writeValue(XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes) {
        LockToken token = (LockToken) val;
        String d = WebDavProtocol.DAV_PREFIX;
        Element lockDiscovery = writer.begin(d + ":lockdiscovery").open();
        if (token != null) {
            Element activeLock = writer.begin(d + ":activelock").open();
            LockInfo info = token.info;
            LockUtils.appendType(writer, info.type);
            LockUtils.appendScope(writer, info.scope);
            LockUtils.appendDepth(writer, info.depth);
            LockUtils.appendOwner(writer, info.lockedByUser);
            LockUtils.appendTimeout(writer, token.timeout.getSeconds());
            LockUtils.appendTokenId(writer, token.tokenId);
            LockUtils.appendRoot(writer, href);
            activeLock.close();
        }
        lockDiscovery.close();
    }

    @Override
    public Object parse(String namespaceURI, String localPart, String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
