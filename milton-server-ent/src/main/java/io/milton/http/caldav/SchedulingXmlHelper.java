/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 */

package io.milton.http.caldav;

import io.milton.http.XmlWriter;
import io.milton.http.XmlWriter.Element;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.resource.SchedulingResponseItem;
import io.milton.http.caldav.ITip.StatusResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example response:
 * <?xml version="1.0" encoding="utf-8" ?>
<C:schedule-response xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
<C:response>
<C:recipient>
<D:href>mailto:wilfredo@example.com<D:href>
</C:recipient>
<C:request-status>2.0;Success</C:request-status>
<C:calendar-data>BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//Example Corp.//CalDAV Server//EN
METHOD:REPLY
BEGIN:VFREEBUSY
UID:4FD3AD926350
DTSTAMP:20090602T200733Z
DTSTART:20090602T000000Z
DTEND:20090604T000000Z
ORGANIZER;CN="Cyrus Daboo":mailto:cyrus@example.com
ATTENDEE;CN="Wilfredo Sanchez Vega":mailto:wilfredo@example.com
FREEBUSY;FBTYPE=BUSY:20090602T110000Z/20090602T120000Z
FREEBUSY;FBTYPE=BUSY:20090603T170000Z/20090603T180000Z
END:VFREEBUSY
END:VCALENDAR
</C:calendar-data>
</C:response>
<C:response>
<C:recipient>
<D:href>mailto:bernard@example.net<D:href>
</C:recipient>
<C:request-status>2.0;Success</C:request-status>
<C:calendar-data>BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//Example Corp.//CalDAV Server//EN
METHOD:REPLY
BEGIN:VFREEBUSY
UID:4FD3AD926350
DTSTAMP:20090602T200733Z
DTSTART:20090602T000000Z
DTEND:20090604T000000Z
ORGANIZER;CN="Cyrus Daboo":mailto:cyrus@example.com
ATTENDEE;CN="Bernard Desruisseaux":mailto:bernard@example.net
FREEBUSY;FBTYPE=BUSY:20090602T150000Z/20090602T160000Z
FREEBUSY;FBTYPE=BUSY:20090603T090000Z/20090603T100000Z
FREEBUSY;FBTYPE=BUSY:20090603T180000Z/20090603T190000Z
END:VFREEBUSY
END:VCALENDAR
</C:calendar-data>
</C:response>
<C:response>
<C:recipient>
<D:href>mailto:mike@example.org<D:href>
</C:recipient>
<C:request-status>3.7;Invalid calendar user</C:request-status>
</C:response>
</C:schedule-response>

 *
 * @author brad
 */
public class SchedulingXmlHelper {

    private static final Logger log = LoggerFactory.getLogger(SchedulingXmlHelper.class);
    private static final String CALDAV_PREFIX = "C";
    private final Helper helper = new Helper();

    public String generateXml(List<SchedulingResponseItem> respItems) throws UnsupportedEncodingException {
        log.trace("respondWithSchedulingResults: " + respItems.size());
        ByteArrayOutputStream generatedXml = new ByteArrayOutputStream();
        XmlWriter writer = new XmlWriter(generatedXml);
        writer.writeXMLHeader();
        writer.open(WebDavProtocol.NS_DAV.getPrefix(), "schedule-response " + helper.generateNamespaceDeclarations());
        writer.newLine();
        for (SchedulingResponseItem resp : respItems) {
            Element elResp = writer.begin("C", "response");
            Element elRecip = elResp.begin("C", "recipient");
            elRecip.begin("D", "href").writeText(resp.getRecipient(), false).close();
            elRecip.close();
            StatusResponse stat = resp.getStatus();
            elRecip.begin(CALDAV_PREFIX, "request-status").writeText(stat.code + ";" + stat.description, false).close();
            if (resp.getiCalText() != null) {
                elRecip.begin(CALDAV_PREFIX, "calendar-data").writeText(resp.getiCalText(), false).close();
            }
            elResp.close();
        }

        writer.close(WebDavProtocol.NS_DAV.getPrefix(), "multistatus");
        writer.flush();
//        log.debug( generatedXml.toString() );
        return generatedXml.toString("UTF-8");

    }

    private class Helper {

        String generateNamespaceDeclarations() {
            return "xmlns:D=\"DAV:\" xmlns:" + CALDAV_PREFIX + "=\"urn:ietf:params:xml:ns:caldav\"";
        }

        void write(ByteArrayOutputStream out, OutputStream outputStream) {
            try {
                String xml = out.toString("UTF-8");
                outputStream.write(xml.getBytes("UTF-8")); // note: this can and should write to the outputstream directory. but if it aint broke, dont fix it...
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
