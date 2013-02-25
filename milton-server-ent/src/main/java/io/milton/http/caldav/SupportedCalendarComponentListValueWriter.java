/*
 *  Copyright 2012 McEvoy Software Ltd.
 */
package io.milton.http.caldav;

import io.milton.http.XmlWriter;
import io.milton.http.XmlWriter.Element;
import io.milton.http.values.HrefList;
import io.milton.http.values.SupportedCalendarComponentList;
import io.milton.http.values.ValueWriter;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.resource.CalendarResource.ComponentType;
import java.util.Map;

/**
 * 
 *
 * @author brad
 */
public class SupportedCalendarComponentListValueWriter implements ValueWriter {

    @Override
    public boolean supports(String nsUri, String localName, Class c) {
        boolean b = SupportedCalendarComponentList.class.isAssignableFrom(c);
        return b;
    }

    @Override
    public void writeValue(XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes) {
        if (val instanceof SupportedCalendarComponentList) {
            Element outerEl = writer.begin(prefix, localName).open();
            SupportedCalendarComponentList list = (SupportedCalendarComponentList) val;
            if (list != null) {
                for (ComponentType comp : list) {
                    Element hrefEl = writer.begin(prefix + ":comp");
                    hrefEl.writeAtt("name", comp.name());
                    hrefEl.close();
                }
            }
            outerEl.close();
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
