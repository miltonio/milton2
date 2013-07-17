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
package io.milton.mini.controllers;

import io.milton.annotations.CalendarInvitations;
import io.milton.annotations.Delete;
import io.milton.annotations.FreeBusyQuery;
import io.milton.annotations.ModifiedDate;
import io.milton.annotations.ResourceController;
import io.milton.annotations.UniqueId;
import io.milton.http.caldav.EventResourceImpl;
import io.milton.http.caldav.ICalFormatter;
import io.milton.http.caldav.ITip;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.mail.MailboxAddress;
import io.milton.resource.SchedulingResponseItem;
import io.milton.vfs.db.AttendeeRequest;
import io.milton.vfs.db.CalEvent;
import io.milton.vfs.db.Calendar;
import io.milton.vfs.db.Profile;
import io.milton.vfs.db.utils.SessionManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;

@ResourceController
public class SchedulingController {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SchedulingController.class);

    @Inject
    private ICalFormatter formatter;

    public SchedulingController() {
    }

    @FreeBusyQuery
    public List<SchedulingResponseItem> freeBusyQuery(Profile profile, String icalQuery) {
        ICalFormatter.FreeBusyRequest r = formatter.parseFreeBusyRequest(icalQuery);
        log.info("queryFreeBusy: attendees=" + r.getAttendeeLines().size() + " - " + r.getAttendeeMailtos().size());
        List<SchedulingResponseItem> list = new ArrayList<SchedulingResponseItem>();
        try {
            for (String attendeeMailto : r.getAttendeeMailtos()) {
                MailboxAddress add = MailboxAddress.parse(attendeeMailto);
                Profile attendee = findUserFromMailto(add);
                if (attendee == null) {
                    log.warn("Attendee not found: " + attendeeMailto);
                    SchedulingResponseItem item = new SchedulingResponseItem("mailto:" + attendeeMailto, ITip.StatusResponse.RS_INVALID_37, null);
                    list.add(item);
                } else {
                    log.info("Found attendee: " + attendee.getName());
                    // Now locate events and build an ical response
                    String ical = buildFreeBusyAttendeeResponse(attendee, r, add.domain, attendeeMailto);
                    SchedulingResponseItem item = new SchedulingResponseItem("mailto:" + attendeeMailto, ITip.StatusResponse.RS_SUCCESS_20, ical);
                    list.add(item);
                }
            }
        } catch (NotAuthorizedException ex) {
            throw new RuntimeException(ex);
        } catch (BadRequestException ex) {
            throw new RuntimeException(ex);
        }
        return list;
    }

    @CalendarInvitations
    public List<AttendeeRequest> getAttendeeRequests(Profile user) {     
        List<AttendeeRequest> list = new ArrayList<AttendeeRequest>();
        if( user.getAttendeeRequests() != null ) {
            for( AttendeeRequest ar : user.getAttendeeRequests() ) {
                if( !ar.isAcknowledged() ) {
                    list.add(ar);
                }
            }
        }
        return list;
    }
    
    @Delete
    public void deleteAttendeeRequest(AttendeeRequest ar) {
        ar.setAcknowledged(true);
        SessionManager.session().save(ar);
    }
    

    @ModifiedDate
    public Date getAttendeeRequestModDate(AttendeeRequest ar) {
        return ar.getOrganiserEvent().getModifiedDate();
    }
    
    @UniqueId
    public String getAttendeeRequestUniqueId(AttendeeRequest ar) {
        return ar.getId().toString();
    }
    
    /**
     *
     *
     * @param add
     * @return
     * @throws NotAuthorizedException
     * @throws BadRequestException
     */
    private Profile findUserFromMailto(MailboxAddress add) throws NotAuthorizedException, BadRequestException {
        return Profile.findByEmail(add.toPlainAddress(), SessionManager.session());
    }

    private String buildFreeBusyAttendeeResponse(Profile attendee, ICalFormatter.FreeBusyRequest request, String domain, String attendeeMailto) throws NotAuthorizedException, BadRequestException {
        Date start = request.getStart();
        Date finish = request.getFinish();
        List<EventResourceImpl> list = new ArrayList<EventResourceImpl>();
        if (attendee.getCalendars() != null) {
            for (Calendar cal : attendee.getCalendars()) {
                if( cal.getEvents() != null ) {
                    for( CalEvent event : cal.getEvents() ) {
                        if( !outsideDates(event, start, finish)) {
                            EventResourceImpl er = new EventResourceImpl();
                            er.setEnd(event.getEndDate());
                            er.setStart(event.getStartDate());
                            er.setSummary(er.getSummary());
                            er.setUniqueId(er.getUniqueId());
                            list.add(er);
                        }
                    }
                }
            }
        }
        return formatter.buildFreeBusyAttendeeResponse(list, request, domain, attendeeMailto);

    }
    
    private boolean outsideDates(CalEvent event, Date start, Date end) {
        if (start != null) {
            if (event.getStartDate().before(start)) {
                log.info(" before start: " + event.getStartDate() + " < " + start);
                return true;
            }
        }

        if (end != null) {
            if (event.getEndDate().after(end)) {
                log.info(" after end: " + event.getEndDate()+ " < " + end);
                return true;
            }
        }

        return false;
    }    
}
