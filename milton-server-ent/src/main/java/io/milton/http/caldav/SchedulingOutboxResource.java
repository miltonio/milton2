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

import io.milton.common.StringUtils;
import io.milton.http.Auth;
import io.milton.http.FileItem;
import io.milton.http.HttpManager;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.principal.CalDavPrincipal;
import io.milton.resource.PostableResource;
import io.milton.resource.Resource;
import io.milton.resource.SchedulingResponseItem;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * The following WebDAV properties specified in CalDAV "calendar-access"
 * [RFC4791] MAY also be defined on scheduling Outbox collections:
 *
 * CALDAV:supported-calendar-component-set - when present this indicates the
 * allowed calendar component types for scheduling messages submitted to the
 * scheduling Outbox collection with the POST method.
 *
 * CALDAV:supported-calendar-data - when present this indicates the allowed
 * media types for scheduling messages submitted to the scheduling Outbox
 * collection with the POST method.
 *
 * CALDAV:max-resource-size - when present this indicates the maximum size of a
 * resource in octets that the server is willing to accept for scheduling
 * messages submitted to the scheduling Outbox
 *
 *
 *
 * Daboo & Desruisseaux Expires April 28, 2011 [Page 12]
 *
 *
 * Internet-Draft CalDAV Scheduling Extensions October 2010
 *
 *
 * collection with the POST method.
 *
 * CALDAV:min-date-time - when present this indicates the earliest date and time
 * (in UTC) that the server is willing to accept for any DATE or DATE-TIME value
 * in scheduling messages submitted to the scheduling Outbox collection with the
 * POST method.
 *
 * CALDAV:max-date-time - when present this indicates the latest date and time
 * (in UTC) that the server is willing to accept for any DATE or DATE-TIME value
 * in scheduling messages submitted to the scheduling Outbox collection with the
 * POST method.
 *
 * CALDAV:max-instances - when present this indicates the maximum number of
 * recurrence instances in scheduling messages submitted to the scheduling
 * Outbox collection with the POST method.
 *
 * CALDAV:max-attendees-per-instance - when present this indicates the maximum
 * number of ATTENDEE properties in any instance of scheduling messages
 * submitted to the scheduling Outbox collection with the POST method.
 *
 *
 * Example:
 *
 * POST /home/cyrus/calendars/outbox/ HTTP/1.1 Host: cal.example.com
 * Content-Type: text/calendar; charset="utf-8" Content-Length: xxxx
 *
 * BEGIN:VCALENDAR VERSION:2.0 PRODID:-//Example Corp.//CalDAV Client//EN
 * METHOD:REQUEST BEGIN:VFREEBUSY UID:4FD3AD926350 DTSTAMP:20090602T190420Z
 * DTSTART:20090602T000000Z DTEND:20090604T000000Z ORGANIZER;CN="Cyrus
 * Daboo":mailto:cyrus@example.com ATTENDEE;CN="Wilfredo Sanchez
 * Vega":mailto:wilfredo@example.com ATTENDEE;CN="Bernard
 * Desruisseaux":mailto:bernard@example.net ATTENDEE;CN="Mike
 * Douglass":mailto:mike@example.org END:VFREEBUSY END:VCALENDAR
 *
 * >> Response <<
 *
 * HTTP/1.1 200 OK Date: Tue, 02 Jun 2009 20:07:34 GMT Content-Type:
 * application/xml; charset="utf-8" Content-Length: xxxx
 *
 * <?xml version="1.0" encoding="utf-8" ?> <C:schedule-response xmlns:D="DAV:"
 * xmlns:C="urn:ietf:params:xml:ns:caldav">
 * <C:response>
 * <C:recipient>
 * <D:href>mailto:wilfredo@example.com<D:href>
 * </C:recipient>
 * <C:request-status>2.0;Success</C:request-status>
 * <C:calendar-data>
 * BEGIN:VCALENDAR VERSION:2.0 PRODID:-//Example Corp.//CalDAV Server//EN
 * METHOD:REPLY BEGIN:VFREEBUSY UID:4FD3AD926350 DTSTAMP:20090602T200733Z
 * DTSTART:20090602T000000Z DTEND:20090604T000000Z ORGANIZER;CN="Cyrus
 * Daboo":mailto:cyrus@example.com ATTENDEE;CN="Wilfredo Sanchez
 * Vega":mailto:wilfredo@example.com
 * FREEBUSY;FBTYPE=BUSY:20090602T110000Z/20090602T120000Z
 * FREEBUSY;FBTYPE=BUSY:20090603T170000Z/20090603T180000Z END:VFREEBUSY
 * END:VCALENDAR
 * </C:calendar-data>
 * </C:response>
 * <C:response>
 * <C:recipient>
 * <D:href>mailto:bernard@example.net<D:href>
 * </C:recipient>
 * <C:request-status>2.0;Success</C:request-status>
 * <C:calendar-data>
 * BEGIN:VCALENDAR VERSION:2.0 PRODID:-//Example Corp.//CalDAV Server//EN
 * METHOD:REPLY BEGIN:VFREEBUSY UID:4FD3AD926350 DTSTAMP:20090602T200733Z
 * DTSTART:20090602T000000Z DTEND:20090604T000000Z ORGANIZER;CN="Cyrus
 * Daboo":mailto:cyrus@example.com ATTENDEE;CN="Bernard
 * Desruisseaux":mailto:bernard@example.net
 * FREEBUSY;FBTYPE=BUSY:20090602T150000Z/20090602T160000Z
 * FREEBUSY;FBTYPE=BUSY:20090603T090000Z/20090603T100000Z
 * FREEBUSY;FBTYPE=BUSY:20090603T180000Z/20090603T190000Z END:VFREEBUSY
 * END:VCALENDAR
 * </C:calendar-data>
 * </C:response>
 * <C:response>
 * <C:recipient>
 * <D:href>mailto:mike@example.org<D:href>
 * </C:recipient>
 * <C:request-status>3.7;Invalid calendar user</C:request-status>
 * </C:response>
 * </C:schedule-response>
 *
 *
 *
 *
 * @author brad
 */
public class SchedulingOutboxResource extends BaseSchedulingXBoxResource implements PostableResource {

    private static final Logger log = LoggerFactory.getLogger(SchedulingOutboxResource.class);
    private final SchedulingXmlHelper schedulingHelper = new SchedulingXmlHelper();
    private byte[] xmlResponse;

    public SchedulingOutboxResource(CalDavPrincipal principal, SchedulingResourceFactory schedulingResourceFactory) {
        super(principal, schedulingResourceFactory);
    }

    /**
     * A POST request may deliver a scheduling message to one or more Calendar
     * Users. Thus the response needs to contain separate status information for
     * each recipient. This specification defines a new XML response body to
     * convey multiple recipient status.
     *
     * A response to a POST method that indicates status for one or more
     * recipients MUST be an XML document with a CALDAV:schedule-response XML
     * element as its root element. This MUST contain one or more
     * CALDAV:response elements for each recipient, with each of those
     * containing elements that indicate which recipient they correspond to, the
     * scheduling status for that recipient, any error codes and an optional
     * description. See Section 14.1 for the detail on the child elements.
     *
     * In the case of a freebusy request, the CALDAV:response elements can also
     * contain CALDAV:calendar-data elements which contain freebusy information
     * (e.g., an iCalendar VFREEBUSY component) indicating the busy state of the
     * corresponding recipient, assuming that the freebusy request for that
     * recipient succeeded.
     *
     * @param iCalText
     * @return
     */
    public List<SchedulingResponseItem> queryFreeBusy(String iCalText) {
        return calendarSearchService.queryFreeBusy(principal, iCalText);
    }

    @Override
    public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
        return null;

    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        return Collections.EMPTY_LIST;
    }

    @Override
    public String getName() {
        return schedulingResourceFactory.getOutboxName();
    }

    @Override
    public String processForm(Map<String, String> parameters, Map<String, FileItem> files) throws BadRequestException, NotAuthorizedException, ConflictException {
        log.trace("process");
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            Request request = HttpManager.request();
            IOUtils.copy(request.getInputStream(), bout);
            String iCalText = bout.toString("UTF-8");
            if (log.isTraceEnabled()) {
                log.trace("Freebusy query: " + iCalText);
            }
            List<SchedulingResponseItem> respItems = queryFreeBusy(iCalText);

            String xml = schedulingHelper.generateXml(respItems);
            xmlResponse = xml.getBytes(StringUtils.UTF8);
            if (log.isTraceEnabled()) {
                log.trace("FreeBusy response= " + xml);
            }

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return null;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        out.write(xmlResponse);
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    @Override
    public String getContentType(String accepts) {
        return "application/xml; charset=\"utf-8\"";
    }

    @Override
    public Long getContentLength() {
        return (long) xmlResponse.length;
    }
}
