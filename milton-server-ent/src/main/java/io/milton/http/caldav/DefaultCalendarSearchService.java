/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.milton.http.caldav;

import io.milton.http.HttpManager;
import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.mail.MailboxAddress;
import io.milton.principal.CalDavPrincipal;
import io.milton.resource.CalendarResource;
import io.milton.resource.CollectionResource;
import io.milton.resource.ICalResource;
import io.milton.resource.Resource;
import io.milton.resource.SchedulingResponseItem;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class DefaultCalendarSearchService implements CalendarSearchService {

    private static final Logger log = LoggerFactory.getLogger(DefaultCalendarSearchService.class);
    private final ICalFormatter formatter;
    private final ResourceFactory resourceFactory;
    private String schedulingColName = "cals";
    private String inboxName = "inbox";
    private String outBoxName = "outbox";
    private String usersBasePath = "/users/";

    public DefaultCalendarSearchService(ICalFormatter formatter, ResourceFactory resourceFactory) {
        if (resourceFactory == null) {
            throw new NullPointerException("ResourceFactory is null");
        }
        this.formatter = formatter;
        this.resourceFactory = resourceFactory;
    }

    @Override
    public List<ICalResource> findCalendarResources(CalendarResource calendar, Date start, Date end) throws NotAuthorizedException, BadRequestException {
        return findCalendarResources(calendar, start, end, null);
    }

    @Override
    public List<ICalResource> findCalendarResources(CalendarResource calendar, Date start, Date end, AbstractMap.SimpleImmutableEntry<String, String> propFilter) throws NotAuthorizedException, BadRequestException {
        // build a list of all calendar resources
        List<ICalResource> list = new ArrayList<>();
        for (Resource r : Optional.ofNullable(calendar.getChildren()).orElse(Collections.emptyList())) {
            if (r instanceof ICalResource) {
                ICalResource cr = (ICalResource) r;
                list.add(cr);
            }
        }

        // So now we have (or might have) start and end dates, so filter list
        Iterator<ICalResource> it = list.iterator();
        while (it.hasNext()) {
            ICalResource r = it.next();
            if (outsideDates(r, start, end)) {
                log.info("Not in range: " + r.getName());
                it.remove();
            }

			StringReader sin = new StringReader(r.getICalData());
			CalendarBuilder builder = new CalendarBuilder();
			Calendar cal = null;

			try {
				cal = builder.build(sin);
			} catch(IOException e) {
				log.error("Exception building calendar from ics", e);
			} catch(ParserException e) {
				log.error("Unable to parse ics", e);
			}

			if (propFilter != null) {
				if (!cal.getComponent("VEVENT").getProperty(propFilter.getKey()).getValue().equals(propFilter.getValue())) {
					log.info("Event do not match properties filter");
					it.remove();
				}
			}
        }
        return list;

    }

    private boolean outsideDates(ICalResource r, Date start, Date end) {
        log.info("outsideDates: " + r.getName());
        EventResource event;
        if (r instanceof EventResource) {
            event = (EventResource) r;
        } else {
            event = new EventResourceImpl();
            try {
                formatter.parseEvent(event, r.getICalData());
            } catch (IOException | ParserException ex) {
                throw new RuntimeException(ex);
            }
        }

        if (start != null) {
            if (event.getStart().before(start)) {
                log.info(" before start: " + event.getStart() + " < " + start);
                return true;
            }
        }

        if (end != null) {
            if (event.getEnd().after(end)) {
                log.info(" after end: " + event.getEnd() + " < " + end);
                return true;
            }
        }

        return false;
    }

    @Override
    public List<SchedulingResponseItem> queryFreeBusy(CalDavPrincipal principal, String iCalText) {
        ICalFormatter.FreeBusyRequest r = formatter.parseFreeBusyRequest(iCalText);
        log.info("queryFreeBusy: attendees=" + r.getAttendeeLines().size() + " - " + r.getAttendeeMailtos().size());
        List<SchedulingResponseItem> list = new ArrayList<>();
        // For each attendee locate events within the given date range and add them as busy responses
        try {
            for (String attendeeMailto : r.getAttendeeMailtos()) {
                MailboxAddress add = MailboxAddress.parse(attendeeMailto);
                CalDavPrincipal attendee = findUserFromMailto(add);
                if (attendee == null) {
                    log.warn("Attendee not found: " + attendeeMailto);
                    SchedulingResponseItem item = new SchedulingResponseItem(attendeeMailto, ITip.StatusResponse.RS_INVALID_37, null);
                    list.add(item);
                } else {
                    log.info("Found attendee: " + attendee.getName());
                    // Now locate events and build an ical response
                    String ical = buildFreeBusyAttendeeResponse(attendee, r, add.domain, attendeeMailto);
                    SchedulingResponseItem item = new SchedulingResponseItem(attendeeMailto, ITip.StatusResponse.RS_SUCCESS_20, ical);
                    list.add(item);
                }
            }
        } catch (NotAuthorizedException | BadRequestException ex) {
            throw new RuntimeException(ex);
        }
        return list;
    }

    /**
     * Attempt to iterate over the entire users collection, and for each event
     * in each user's calendar check if the given user is an attendee, and if
     * return it.
     *
     * Rather inefficient
     *
     * @param user
     * @return
     * @throws NotAuthorizedException
     * @throws BadRequestException
     */
    @Override
    public List<ICalResource> findAttendeeResources(CalDavPrincipal user) throws NotAuthorizedException, BadRequestException {
        List<ICalResource> list = new ArrayList<>();
        String host = HttpManager.request().getHostHeader();
        Resource rUsersHome = resourceFactory.getResource(host, usersBasePath);
        if( rUsersHome instanceof CollectionResource ) {
            CollectionResource usersHome = (CollectionResource) rUsersHome;
            for( Resource rUser : Optional.ofNullable(usersHome.getChildren()).orElse(Collections.emptyList())) {
                if( rUser instanceof CalDavPrincipal) {
                    CalDavPrincipal p = (CalDavPrincipal) rUser;
                    for( String href : p.getCalendarHomeSet() ) {
                        Resource rCalHome = resourceFactory.getResource(host, href);
                        if( rCalHome instanceof CollectionResource ) {
                            CollectionResource calHome = (CollectionResource) rCalHome;
                            for( Resource rCal : Optional.ofNullable(calHome.getChildren()).orElse(Collections.emptyList())) {
                                if( rCal instanceof CalendarResource) {
                                    CalendarResource cal = (CalendarResource) rCal;
                                    for( Resource rEvent : Optional.ofNullable(cal.getChildren()).orElse(Collections.emptyList())) {
                                        if( rEvent instanceof ICalResource) {
                                            ICalResource event = (ICalResource) rEvent;
                                            if( isAttendeeOf(user, event) ) {
                                                list.add(event);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return list;
    }

    @Override
    public String findAttendeeResourcesCTag(CalDavPrincipal attendee) throws NotAuthorizedException, BadRequestException {
        Date latest = null;
        for (ICalResource r : findAttendeeResources(attendee)) {
            Date d = r.getModifiedDate();
            if (latest == null || d.after(latest)) {
                latest = d;
            }
        }
        if (latest != null) {
            return "mod-" + latest.getTime();
        } else {
            return "na";
        }

    }

    @Override
    public String getSchedulingColName() {
        return schedulingColName;
    }

    public void setSchedulingColName(String schedulingColName) {
        this.schedulingColName = schedulingColName;
    }



    @Override
    public String getSchedulingInboxColName() {
        return inboxName;
    }

    public void setSchedulingInboxColName(String inboxName) {
        this.inboxName = inboxName;
    }



    @Override
    public String getSchedulingOutboxColName() {
        return outBoxName;
    }

    public void setSchedulingOutboxColName(String outBoxName) {
        this.outBoxName = outBoxName;
    }



    /**
     * Use the domain portion of the email as the host, and the initial portion
     * as the userid. This wont work in systems which require use userid's with
     *
     * @ symbols
     *
     * @param attendeeMailto
     * @return
     */
    private CalDavPrincipal findUserFromMailto(MailboxAddress add) throws NotAuthorizedException, BadRequestException {
        String userPath = usersBasePath + add.user;
        Resource r = resourceFactory.getResource(add.domain, userPath);
        if (r == null) {
            log.warn("Failed to find: " + userPath + " in host: " + add.domain);
            return null;
        } else {
            if (r instanceof CalDavPrincipal) {
                return (CalDavPrincipal) r;
            } else {
                log.warn("findUserFromMailto: found a resource but it is not a CalDavPrincipal. Is a: " + r.getClass().getCanonicalName());
                return null;
            }
        }
    }

    public String getUsersBasePath() {
        return usersBasePath;
    }

    public void setUsersBasePath(String usersBasePath) {
        this.usersBasePath = usersBasePath;
    }

    private String buildFreeBusyAttendeeResponse(CalDavPrincipal attendee, ICalFormatter.FreeBusyRequest request, String domain, String attendeeMailto) throws NotAuthorizedException, BadRequestException {
        Map<String, String> source = request.getLines();
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\n");
        sb.append("VERSION:2.0 PRODID:-//milton.io//CalDAV Server//EN\n");
        sb.append("METHOD:REPLY\n");
        sb.append("BEGIN:VFREEBUSY\n");
        // Copy these lines back verbatim
        sb.append(source.get("UID")).append("\n");
        sb.append(source.get("DTSTAMP")).append("\n");
        sb.append(source.get("DTSTART")).append("\n");
        sb.append(source.get("DTEND")).append("\n");
        sb.append(source.get("ORGANIZER")).append("\n");
        // Output the original attendee line
        sb.append(request.getAttendeeLines().get(attendeeMailto)).append("\n");

        Date start = request.getStart();
        Date finish = request.getFinish();
        for (String href : attendee.getCalendarHomeSet()) {
            if (log.isTraceEnabled()) {
                log.trace("Look for calendar home: " + href);
            }
            Resource rCalHome = resourceFactory.getResource(domain, href);
            if (rCalHome instanceof CollectionResource) {
                CollectionResource calHome = (CollectionResource) rCalHome;
                log.trace("Look for calendars in home");
                for (Resource rColCal : Optional.ofNullable(calHome.getChildren()).orElse(Collections.emptyList())) {
                    if (rColCal instanceof CalendarResource) {
                        CalendarResource cal = (CalendarResource) rColCal;
                        List<ICalResource> eventsInRange = findCalendarResources(cal, start, finish, null);
                        if (log.isTraceEnabled()) {
                            log.trace("Process calendar: " + cal.getName() + " events in range=" + eventsInRange.size());
                            log.trace("  range= " + start + " - " + finish);
                        }
                        for (ICalResource event : eventsInRange) {
                            log.trace("Process event: " + event.getName());
                            EventResourceImpl er = new EventResourceImpl();
                            try {
                                formatter.parseEvent(er, event.getICalData());
                            } catch (IOException | ParserException ex) {
                                throw new RuntimeException(ex);
                            }

                            // write the freebusy statement, Eg:
                            // FREEBUSY;FBTYPE=BUSY:20090602T110000Z/20090602T120000Z
                            sb.append("FREEBUSY;FBTYPE=BUSY:");
                            sb.append(formatter.formatDate(er.getStart()));
                            sb.append("/");
                            sb.append(formatter.formatDate(er.getEnd()));
                            sb.append("\n");
                        }
                    }
                }
            } else {
                if (rCalHome == null) {
                    log.warn("Didnt find calendar home: " + href + " in domain: " + domain);
                } else {
                    log.warn("Found a resource at the calendar home address, but it is not a CollectionResource. Is a: " + rCalHome.getClass());
                }
            }
        }
        sb.append("END:VFREEBUSY\n");
        sb.append("END:VCALENDAR\n");
        return sb.toString();
    }

    /**
     * Check if the given user is an attendee of the given event. Just does
     * a simple check on the userId portion of the mailto address against
     * the name of the principal
     *
     * @param user
     * @param event
     * @return
     */
    private boolean isAttendeeOf(CalDavPrincipal user, ICalResource event) {
        for( String mailto : formatter.parseAttendees(event.getICalData()) ) {
            MailboxAddress add = MailboxAddress.parse(mailto);
            if( add.user.equals(user.getName())) {
                return true;
            }
        }
        return false;
    }
}
