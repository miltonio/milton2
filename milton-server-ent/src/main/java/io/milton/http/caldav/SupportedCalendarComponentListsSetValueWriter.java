/*
 *  Copyright 2012 McEvoy Software Ltd.
 */
package io.milton.http.caldav;

import io.milton.http.XmlWriter;
import io.milton.http.XmlWriter.Element;
import io.milton.http.values.SupportedCalendarComponentList;
import io.milton.http.values.SupportedCalendarComponentListsSet;
import io.milton.http.values.ValueWriter;
import java.util.Map;

/**
 * 
 *
 * @author brad
 */
public class SupportedCalendarComponentListsSetValueWriter implements ValueWriter {

    private final SupportedCalendarComponentListValueWriter componentSetWriter = new SupportedCalendarComponentListValueWriter();
    
    @Override
    public boolean supports(String nsUri, String localName, Class c) {
        boolean b = SupportedCalendarComponentListsSet.class.isAssignableFrom(c);
        return b;
    }

    @Override
    public void writeValue(XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes) {
        if (val instanceof SupportedCalendarComponentListsSet) {
            Element outerEl = writer.begin(prefix, localName).open();
            SupportedCalendarComponentListsSet list = (SupportedCalendarComponentListsSet) val;
            if (list != null) {
                for (SupportedCalendarComponentList compSet : list) {
                    componentSetWriter.writeValue(writer, nsUri, prefix, "supported-calendar-component-set", compSet, href, nsPrefixes);
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
