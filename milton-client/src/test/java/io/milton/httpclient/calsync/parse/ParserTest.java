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
package io.milton.httpclient.calsync.parse;

import io.milton.httpclient.calsync.parse.CalDavBeanPropertyMapper;
import io.milton.httpclient.parse.PropertyAccessor;
import java.text.ParseException;
import junit.framework.TestCase;
import net.fortuna.ical4j.model.*;

/**
 *
 * @author brad
 */
public class ParserTest extends TestCase {

    CalDavBeanPropertyMapper parser;

    @Override
    protected void setUp() throws Exception {
        parser = new CalDavBeanPropertyMapper(new PropertyAccessor());
    }

    public void testParse() throws Exception {
        String ical = "BEGIN:VCALENDAR\n"
                + "PRODID:-//milton.io//iCal4j 1.0//EN\n"
                + "VERSION:2.0\n"
                + "BEGIN:VTIMEZONE\n"
                + "TZID:Europe/London\n"
                + "TZURL:http://tzurl.org/zoneinfo/Europe/London\n"
                + "X-LIC-LOCATION:Europe/London\n"
                + "BEGIN:DAYLIGHT\n"
                + "TZOFFSETFROM:+0000\n"
                + "TZOFFSETTO:+0100\n"
                + "TZNAME:BST\n"
                + "DTSTART:19810329T010000\n"
                + "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\n"
                + "END:DAYLIGHT\n"
                + "BEGIN:STANDARD\n"
                + "TZOFFSETFROM:+0100\n"
                + "TZOFFSETTO:+0000\n"
                + "TZNAME:GMT\n"
                + "DTSTART:19961027T020000\n"
                + "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\n"
                + "END:STANDARD\n"
                + "BEGIN:STANDARD\n"
                + "TZOFFSETFROM:-000115\n"
                + "TZOFFSETTO:+0000\n"
                + "TZNAME:GMT\n"
                + "DTSTART:18471201T000000\n"
                + "END:STANDARD\n"
                + "BEGIN:DAYLIGHT\n"
                + "TZOFFSETFROM:+0000\n"
                + "TZOFFSETTO:+0100\n"
                + "TZNAME:BST\n"
                + "DTSTART:19160521T023000\n"
                + "END:DAYLIGHT\n"
                + "BEGIN:STANDARD\n"
                + "TZOFFSETFROM:+0100\n"
                + "TZOFFSETTO:+0000\n"
                + "TZNAME:GMT\n"
                + "DTSTART:19161001T033000\n"
                + "END:STANDARD\n"
                + "BEGIN:DAYLIGHT\n"
                + "TZOFFSETFROM:+0100\n"
                + "TZOFFSETTO:+0200\n"
                + "TZNAME:BDST\n"
                + "DTSTART:19410504T030000\n"
                + "END:DAYLIGHT\n"
                + "BEGIN:DAYLIGHT\n"
                + "TZOFFSETFROM:+0200\n"
                + "TZOFFSETTO:+0100\n"
                + "TZNAME:BST\n"
                + "DTSTART:19410810T040000\n"
                + "END:DAYLIGHT\n"
                + "BEGIN:STANDARD\n"
                + "TZOFFSETFROM:+0100\n"
                + "TZOFFSETTO:+0100\n"
                + "TZNAME:BST\n"
                + "DTSTART:19681027T000000\n"
                + "RDATE:\n"
                + "END:STANDARD\n"
                + "BEGIN:STANDARD\n"
                + "TZOFFSETFROM:+0000\n"
                + "TZOFFSETTO:+0000\n"
                + "TZNAME:GMT\n"
                + "DTSTART:19960101T000000\n"
                + "RDATE:\n"
                + "END:STANDARD\n"
                + "END:VTIMEZONE\n"
                + "BEGIN:VEVENT\n"
                + "DTSTAMP:20120606T213352Z\n"
                + "DTSTART;TZID=Europe/London:20120606T235959\n"
                + "DTEND:20120606T010000\n"
                + "SUMMARY:aSummary\n"
                + "DESCRIPTION:aDescription\n"
                + "LOCATION:aLocation\n"
                + "ORGANIZER:anOrganizer\n"
                + "UID:XXX\n"
                + "TZID:Europe/London\n"
                + "END:VEVENT\n"
                + "END:VCALENDAR";

        MyCalendarEventBean bean = new MyCalendarEventBean();


        parser.toBean(bean, ical);

        assertEquals("XXX", bean.getUid());
        assertEquals("aSummary", bean.getSummary());
        assertEquals("aDescription", bean.getDescription());
        assertEquals("aLocation", bean.getLocation());
        assertEquals("anOrganizer", bean.getOrganizer());
        assertNotNull(bean.getStartDate());
        assertNotNull(bean.getEndDate());
        assertEquals("20120606T235959", bean.getStartDate().toString());
        
//        assertEquals(st, bean.getStartDate());
//        assertEquals(end, bean.getEndDate());
    }

    public void testFormat() {
        MyCalendarEventBean bean = new MyCalendarEventBean();
        bean.setUid("xxx");
        bean.setDescription("aDescription");
        bean.setSummary("aSummary");
        bean.setLocation("The moon");
        bean.setOrganizer("anOrganizer");
        Date now= new Date();
        bean.setStartDate(now );
        bean.setEndDate(now);
        bean.setTimezone("GMT");
        
        String text = parser.toVCard(bean);
        System.out.println("---");
        System.out.println(text);
        System.out.println("---");
        assertTrue(text.contains("xxx"));
        assertTrue(text.contains("aDescription"));
        assertTrue(text.contains("aSummary"));
        assertTrue(text.contains("GMT"));
        assertTrue(text.contains("The moon"));
        assertTrue(text.contains("anOrganizer"));
    }

}
