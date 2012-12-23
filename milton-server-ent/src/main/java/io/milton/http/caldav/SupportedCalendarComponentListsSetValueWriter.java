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
package io.milton.http.caldav;

import io.milton.http.XmlWriter;
import io.milton.http.XmlWriter.Element;
import io.milton.http.values.SupportedCalendarComponentList;
import io.milton.http.values.SupportedCalendarComponentListsSet;
import io.milton.http.values.ValueWriter;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.resource.CalendarResource.ComponentType;
import java.util.Map;

/**
 * Supports HrefList objects, and writes them out as a list of <href>...</href>
 * elements
 *
 * Currently readonly, but should support writing
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
