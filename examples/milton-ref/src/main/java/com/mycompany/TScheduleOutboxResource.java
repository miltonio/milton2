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

package com.mycompany;

import io.milton.http.FileItem;
import io.milton.http.caldav.ITip;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.SchedulingOutboxResource;
import io.milton.resource.SchedulingResponseItem;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class TScheduleOutboxResource extends TFolderResource implements SchedulingOutboxResource {

    private static final Logger log = LoggerFactory.getLogger(TScheduleOutboxResource.class);

    private String color = "#2952A3";
    
    public TScheduleOutboxResource(TFolderResource parent, String name) {
        super(parent, name);
    }

    @Override
    public List<SchedulingResponseItem> queryFreeBusy(String iCalText) {
        log.info("queryFreeBusy");
        List<SchedulingResponseItem> respItems = new ArrayList<SchedulingResponseItem>();
        try {
            Reader sr = new StringReader(iCalText);
            LineNumberReader r = new LineNumberReader(sr);
            String organiser = "";
            String line = nextLine(r);
            while (line != null) {
                if (line.startsWith("ORGANIZER:")) {
                    organiser = line.substring(line.lastIndexOf(":"));
                } else if (line.startsWith("ATTENDEE:")) {
                    SchedulingResponseItem item = processAttendeeLine(line, organiser);
                    respItems.add(item);
                }
                line = nextLine(r);
            }
            return respItems;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String processForm(Map<String, String> parameters, Map<String, FileItem> files) throws BadRequestException, NotAuthorizedException, ConflictException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private SchedulingResponseItem processAttendeeLine(String line, String organiser) {
        System.out.println("processAttendeeLine: " + line);
        String attendeeName = line.substring(line.lastIndexOf(":")+1);
        System.out.println("process user: " + attendeeName);
        TCalDavPrincipal attendee = TResourceFactory.findUser(attendeeName);
        if (attendee == null) {
            return new SchedulingResponseItem(attendeeName, ITip.StatusResponse.RS_INVALID_37, null);
        } else {
            String ical = "";
            ical += "BEGIN:VCALENDAR\n";
            ical += "VERSION:2.0\n";
            ical += "PRODID:-//Example Corp.//CalDAV Server//EN\n";
            ical += "METHOD:REPLY\n";
            ical += "BEGIN:VFREEBUSY\n";
            ical += "UID:4FD3AD926350\n";
            ical += "DTSTAMP:20090602T200733Z\n";
            ical += "DTSTART:20090602T000000Z\n";
            ical += "DTEND:20090604T000000Z\n";
            ical += "ORGANIZER;CN=\"" + organiser + "\":mailto:" + organiser + "\n";  // TODO: should be organiser user
            ical += "ATTENDEE;CN=\"" + attendeeName + "\":mailto:" + attendeeName + "\n";
            ical += "FREEBUSY;FBTYPE=BUSY:20090602T110000Z/20090602T120000Z\n";
            ical += "FREEBUSY;FBTYPE=BUSY:20090603T170000Z/20090603T180000Z\n";
            ical += "END:VFREEBUSY\n";
            ical += "END:VCALENDAR\n";

            return new SchedulingResponseItem(attendeeName, ITip.StatusResponse.RS_SUCCESS_20, ical);
        }
    }

    private String nextLine(LineNumberReader r) throws IOException {
        String s = r.readLine();
        if (s == null) {
            return null;
        }

        r.mark(10000);
        String nextLine = r.readLine();
        if (nextLine != null) {
            if (nextLine.length() != nextLine.trim().length()) {
                s += nextLine.trim();
            } else {
                r.reset();
            }
        }
        return s;
    }
    
}
