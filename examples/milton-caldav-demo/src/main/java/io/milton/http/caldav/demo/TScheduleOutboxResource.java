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

package io.milton.http.caldav.demo;

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
