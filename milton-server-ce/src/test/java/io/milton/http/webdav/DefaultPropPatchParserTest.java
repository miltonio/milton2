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

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import javax.xml.namespace.QName;

/**
 *
 * @author brad
 */
public class DefaultPropPatchParserTest extends TestCase {
	public DefaultPropPatchParserTest(String testName) {
		super(testName);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

    public void testMsOfficePropPatch() throws Exception
    {
        DefaultPropPatchParser parser = new DefaultPropPatchParser();
        InputStream is = getClass().getResourceAsStream("/com/bradmcevoy/http/webdav/proppatch_request_msoffice.xml");
        QName lastAccess = new QName("urn:schemas-microsoft-com:", "Win32LastAccessTime");
        QName lastModified = new QName("urn:schemas-microsoft-com:", "Win32LastModifiedTime");
        QName fileAttributes = new QName("urn:schemas-microsoft-com:", "Win32FileAttributes");

        PropPatchParseResult result = parser.getRequestedFields(is);

        Map<QName, String> fieldsToSet = result.getFieldsToSet();
        assertEquals(3, fieldsToSet.size());
        assertEquals("Wed, 10 Dec 2008 21:55:22 GMT", fieldsToSet.get(lastAccess));
        assertEquals("Wed, 10 Dec 2008 21:55:22 GMT", fieldsToSet.get(lastModified));
        assertEquals("00000020", fieldsToSet.get(fileAttributes));

        Set<QName> fieldsToRemove = result.getFieldsToRemove();
        assertEquals(0, fieldsToRemove.size());
    }

    public void testVistaPropPatch() throws Exception
    {
        DefaultPropPatchParser parser = new DefaultPropPatchParser();
        InputStream is = getClass().getResourceAsStream("/com/bradmcevoy/http/webdav/proppatch_request_vista.http");
        QName creationTime = new QName("urn:schemas-microsoft-com:", "Win32CreationTime");
        QName lastAccess = new QName("urn:schemas-microsoft-com:", "Win32LastAccessTime");
        QName lastModified = new QName("urn:schemas-microsoft-com:", "Win32LastModifiedTime");
        QName fileAttributes = new QName("urn:schemas-microsoft-com:", "Win32FileAttributes");

        PropPatchParseResult result = parser.getRequestedFields(is);

        Map<QName, String> fieldsToSet = result.getFieldsToSet();
        assertEquals(4, fieldsToSet.size());
        assertEquals("Thu, 02 Jul 2009 11:28:59 GMT", fieldsToSet.get(creationTime));
        assertEquals("Thu, 02 Jul 2009 11:28:59 GMT", fieldsToSet.get(lastAccess));
        assertEquals("Thu, 02 Jul 2009 11:28:59 GMT", fieldsToSet.get(lastModified));
        assertEquals("00000020", fieldsToSet.get(fileAttributes));

        Set<QName> fieldsToRemove = result.getFieldsToRemove();
        assertEquals(0, fieldsToRemove.size());
    }

    public void testSpecPropPatch() throws Exception
    {
        DefaultPropPatchParser parser = new DefaultPropPatchParser();
        InputStream is = getClass().getResourceAsStream("/com/bradmcevoy/http/webdav/proppatch_request_spec.xml");
        QName author1 = new QName("http://www.w3.com/standards/z39.50/", "Author1");
        QName author2 = new QName("http://www.w3.com/standards/z39.50/", "Author2");
        QName copyrightOwner = new QName("http://www.w3.com/standards/z39.50/", "Copyright-Owner");

        PropPatchParseResult result = parser.getRequestedFields(is);

        Map<QName, String> fieldsToSet = result.getFieldsToSet();
        assertEquals(2, fieldsToSet.size());
        assertEquals("Jim Whitehead", fieldsToSet.get(author1));
        assertEquals("Roy Fielding", fieldsToSet.get(author2));

        Set<QName> fieldsToRemove = result.getFieldsToRemove();
        assertEquals(1, fieldsToRemove.size());
        assertTrue(fieldsToRemove.contains(copyrightOwner));
    }

    public void testMkCalendar() throws Exception
    {
        DefaultPropPatchParser parser = new DefaultPropPatchParser();
        InputStream is = getClass().getResourceAsStream("/com/bradmcevoy/http/webdav/mkcalendar_apple.xml");
        QName calendarColor = new QName("http://apple.com/ns/ical/", "calendar-color");
        QName calendarOrder = new QName("http://apple.com/ns/ical/", "calendar-order");
        QName calendarTimezone = new QName("urn:ietf:params:xml:ns:caldav", "calendar-timezone");
        QName displayName = new QName("DAV:", "displayname");
        QName transparency = new QName("urn:ietf:params:xml:ns:caldav", "schedule-calendar-transp");
        QName supportedComponentSet = new QName("urn:ietf:params:xml:ns:caldav", "supported-calendar-component-set");
        String timezone = "BEGIN:VCALENDAR\r\n" +
                "VERSION:2.0\r\n" +
                "PRODID:-//Apple Inc.//Mac OS X 10.10//EN\r\n" +
                "CALSCALE:GREGORIAN\r\n" +
                "BEGIN:VTIMEZONE\r\n" +
                "TZID:America/Denver\r\n" +
                "BEGIN:DAYLIGHT\r\n" +
                "TZOFFSETFROM:-0700\r\n" +
                "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=2SU\r\n" +
                "DTSTART:20070311T020000\r\n" +
                "TZNAME:MDT\r\n" +
                "TZOFFSETTO:-0600\r\n" +
                "END:DAYLIGHT\r\n" +
                "BEGIN:STANDARD\r\n" +
                "TZOFFSETFROM:-0600\r\n" +
                "RRULE:FREQ=YEARLY;BYMONTH=11;BYDAY=1SU\r\n" +
                "DTSTART:20071104T020000\r\n" +
                "TZNAME:MST\r\n" +
                "TZOFFSETTO:-0700\r\n" +
                "END:STANDARD\r\n" +
                "END:VTIMEZONE\r\n" +
                "END:VCALENDAR";

        PropPatchParseResult result = parser.getRequestedFields(is);

        Map<QName, String> fieldsToSet = result.getFieldsToSet();
        assertEquals(6, fieldsToSet.size());
        assertEquals("#F64F00FF", fieldsToSet.get(calendarColor));
        assertEquals("1001", fieldsToSet.get(calendarOrder));
        assertEquals(timezone, fieldsToSet.get(calendarTimezone));
        assertEquals("Untitled", fieldsToSet.get(displayName));
        assertEquals("<opaque/>", fieldsToSet.get(transparency));
        assertEquals("<comp name=\"VEVENT\"/>", fieldsToSet.get(supportedComponentSet));

        Set<QName> fieldsToRemove = result.getFieldsToRemove();
        assertEquals(0, fieldsToRemove.size());
    }
}
