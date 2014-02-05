/*
 * Copyright (C) 2012 McEvoy Software Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.milton.mini.services;

import io.milton.mini.utils.CalUtils;
import io.milton.vfs.db.AttendeeRequest;
import static io.milton.vfs.db.AttendeeRequest.PARTSTAT_ACCEPTED;
import io.milton.vfs.db.BaseEntity;
import io.milton.vfs.db.CalEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.TzId;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.milton.vfs.db.Calendar;
import io.milton.vfs.db.Profile;
import io.milton.vfs.db.utils.SessionManager;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.parameter.ScheduleStatus;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author brad
 */
public class CalendarService {

    private static final Logger log = LoggerFactory.getLogger(CalendarService.class);
    private String defaultColor = "blue";

    public net.fortuna.ical4j.model.Calendar parse(InputStream icalContent) throws IOException, ParserException {
        CalendarBuilder builder = new CalendarBuilder();
        net.fortuna.ical4j.model.Calendar cal = builder.build(icalContent);
        return cal;
    }

    /**
     * Called when an invitee responds to an invitation
     *
     * @param attendeeReq
     * @param cal
     * @return returns the user's event if a partstat is found for this user's invitation and the partstat is accepted
     */
    public CalEvent processResponse(AttendeeRequest attendeeReq, Calendar userCalendar, net.fortuna.ical4j.model.Calendar cal, Session session) {
        VEvent ev = event(cal);
        for (Object p : ev.getProperties()) {
            if (p instanceof Attendee) {
                Attendee att = (Attendee) p;
                String attEmail = findEmail(att);
                if (attEmail != null) {
                    if (attEmail.equals(attendeeReq.getAttendee().getEmail())) {
                        // Found it, find the part-stat
                        Parameter pPartStat = att.getParameter("PARTSTAT");
                        if (pPartStat != null) {
                            attendeeReq.setParticipationStatus(pPartStat.getValue());
                            session.save(attendeeReq);
                            
                            if( attendeeReq.getParticipationStatus().equals(AttendeeRequest.PARTSTAT_ACCEPTED)) {
                                CalEvent e = attendeeReq.getAttendeeEvent();
                                if( e == null ) {
                                    log.info("add event");
                                    Date now = new Date();
                                    CalEvent orgEvent = attendeeReq.getOrganiserEvent();
                                    e = userCalendar.add(attendeeReq.getName(), now);
                                    e.setAttendeeRequest(attendeeReq);
                                    copyProps(orgEvent, e);
                                    session.save(e);
                                    
                                    attendeeReq.setAttendeeEvent(e);
                                    session.save(attendeeReq);
                                }
                                return e;
                            } else if( attendeeReq.getParticipationStatus().equals(AttendeeRequest.PARTSTAT_TENTATIVE)) {
                            } else if( attendeeReq.getParticipationStatus().equals(AttendeeRequest.PARTSTAT_ACCEPTED)) {
                            } else {
                                CalEvent e = attendeeReq.getAttendeeEvent();
                                if( e != null ) {
                                    log.info("delete event");
                                    e.delete(session);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the updated ical text
     *
     * @param event
     * @param data
     * @param callback - called for any updated events, included attendee events
     * which have been generated on acceptance of invitations
     * @return - updated ical text
     */
    public String update(CalEvent event, String data, UpdatedAttendeeCallback callback) {
        log.info("update: " + event.getName());
        System.out.println(data);
        String ical = null;
        Session session = SessionManager.session();
        try {
            ical = _update(event, data, callback, session);
        } catch (IOException | ParserException ex) {
            throw new RuntimeException(ex);
        }
        session.save(event);
        session.save(event.getCalendar());
        return ical;
    }

    public Calendar createCalendar(BaseEntity owner, String newName) {
        Session session = SessionManager.session();

        Calendar c = new Calendar();
        c.setColor(defaultColor);
        c.setCreatedDate(new Date());
        c.setName(newName);
        c.setBaseEntity(owner);

        session.save(c);

        return c;
    }

    public void delete(CalEvent event) {
        Session session = SessionManager.session();
        if (event.getAttendeeRequest() != null) {
            event.getAttendeeRequest().setAttendeeEvent(null);
            session.save(event.getAttendeeRequest());
        }
        session.flush();

        List<AttendeeRequest> attendeeRequests = AttendeeRequest.findByOrganisorEvent(event, session);
        for (AttendeeRequest ar : attendeeRequests) {
            log.info("Delete AttendeeRequest: " + ar.getName());
            session.delete(ar);
            session.flush();
            CalEvent attendeeRequesst = ar.getAttendeeEvent();
            if (attendeeRequesst != null) {
                delete(attendeeRequesst);
            }
        }
        session.flush();

        log.info("delete event id: " + event.getId());
        session.delete(event);
        session.flush();
    }

    public void move(CalEvent event, Calendar destCalendar, String name) {
        Session session = SessionManager.session();

        if (!name.equals(event.getName())) {
            event.setName(name);
        }

        Calendar sourceCal = event.getCalendar();
        if (destCalendar != sourceCal) {
            sourceCal.getEvents().remove(event);
            event.setCalendar(destCalendar);
            if (destCalendar.getEvents() == null) {
                destCalendar.setEvents(new ArrayList<CalEvent>());
            }
            destCalendar.getEvents().add(event);
            session.save(sourceCal);
            session.save(destCalendar);
        }
    }

    public void copy(CalEvent event, Calendar destCalendar, String name) {
        Session session = SessionManager.session();

        if (destCalendar.getEvents() == null) {
            destCalendar.setEvents(new ArrayList<CalEvent>());
        }
        CalEvent newEvent = new CalEvent();
        newEvent.setCalendar(destCalendar);
        destCalendar.getEvents().add(newEvent);

        newEvent.setCreatedDate(new Date());
        newEvent.setModifiedDate(new Date());        
        newEvent.setName(name);
        
        copyProps(event, newEvent);
        session.save(newEvent);
    }
    
    public void copyProps(CalEvent event, CalEvent newEvent) {
        newEvent.setDescription(event.getDescription());
        newEvent.setEndDate(event.getEndDate());
        newEvent.setStartDate(event.getStartDate());
        newEvent.setSummary(event.getSummary());
        newEvent.setTimezone(event.getTimezone());        
    }

    public void delete(Calendar calendar) {
        Session session = SessionManager.session();

        session.delete(calendar);
    }

    public CalEvent createEvent(Calendar calendar, String newName, String icalData, UpdatedEventCallback callback) throws IOException {
        log.info("createEvent: newName=" + newName + " -- " + icalData);
        Session session = SessionManager.session();

        Date now = new Date();
        CalEvent e = calendar.add(newName, now);

        AttendeeRequest ar = AttendeeRequest.findByName(newName, session);

        if (icalData != null) {
            ByteArrayInputStream fin = new ByteArrayInputStream(icalData.getBytes("UTF-8"));
            CalendarBuilder builder = new CalendarBuilder();
            net.fortuna.ical4j.model.Calendar cal4jCalendar;
            try {
                cal4jCalendar = builder.build(fin);
            } catch (IOException | ParserException ex) {
                throw new RuntimeException(ex);
            }
            boolean isAccept = ar != null;
            _setCalendar(cal4jCalendar, e, isAccept, session);
            session.save(e);
            if (callback != null) {
                String newIcal = formatIcal(cal4jCalendar);
                callback.updated(newIcal, e);
            }
        }
        // Check to see if we are accepting an attendeerequest, ie where name is the same
        if (ar != null) {
            log.info("found attendee request, so link");
            e.setAttendeeRequest(ar);
            ar.setAttendeeEvent(e);
            ar.setAcknowledged(true);
            ar.setParticipationStatus(PARTSTAT_ACCEPTED);
            session.save(ar);
        }

        return e;
    }

    /**
     * When an organisor event has changed, update appropriate fields only on
     * attendee events
     *
     * @param attendeeEvent
     * @param event
     * @param calendar
     * @param session
     */
    private void updateAttendeeEvent(AttendeeRequest ar, boolean needsReInvite, CalEvent attendeeEvent, CalEvent event, net.fortuna.ical4j.model.Calendar calendar, UpdatedAttendeeCallback callback, Session session) throws IOException {
        log.info("updateAttendeeEvent: " + event.getSummary());

        attendeeEvent.setDescription(event.getDescription());
        attendeeEvent.setModifiedDate(new Date());
        attendeeEvent.setSummary(event.getSummary());

        if (needsReInvite) {
            // delete the event and set partstat to NEEDS-ACTION
            callback.deleted(event);
            ar.setAttendeeEvent(null);
            ar.setParticipationStatus(AttendeeRequest.PARTSTAT_NEEDS_ACTION);
            ar.setAcknowledged(false);
            session.save(ar);
            attendeeEvent.delete(session);
        } else {
            session.save(attendeeEvent);
            callback.updated(attendeeEvent);
        }
    }

    /**
     * Add RSVP=True to attendees and return new ical data
     *
     * @param event
     * @param e
     * @param session
     */
    public void setRsvps(VEvent event, CalEvent e, Session session) {
        log.info("setRsvps: " + event.getName());
        for (Object o : event.getProperties()) {
            if (o instanceof Organizer) {
                Organizer org = (Organizer) o;
                String mail = org.getCalAddress().toString();
                System.out.println("------------ org mail = " + mail);
                mail = mail.replace("mailto:", "");
                Profile p = Profile.findByEmail(mail, session);
                if (p != null) {
                    log.info("set org profile: " + p.getFormattedName());
                    e.setOrganisor(p);
                } else {
                    e.setOrganisor(null);
                }
                session.save(e);
                // TODO
            } else if (o instanceof Attendee) {
                Attendee attendee = (Attendee) o;
                Iterator it = attendee.getParameters().iterator();
                boolean rsvpFound = false;
                while (it.hasNext()) {
                    Parameter p = (Parameter) it.next();
                    if (p.getName().equals(Rsvp.RSVP)) {
                        rsvpFound = true;
                        break;
                    }
                }
                if (!rsvpFound) {
                    attendee.getParameters().add(Rsvp.TRUE);
                }
                String attendeeEmail = attendee.getValue();
                attendeeEmail = attendeeEmail.replace("mailto:", "");
                log.info("Check/Create attendance record for: " + attendeeEmail);
                Profile p = Profile.findByEmail(attendeeEmail, SessionManager.session());
                if (p != null) {
                    log.info("Check create attendance for: " + p.getName());
                    Date now = new Date();
                    Parameter sa = attendee.getParameter("SCHEDULE-AGENT");
                    String scheduleAgent = null;
                    if (sa != null) {
                        scheduleAgent = sa.getValue();
                    }
                    AttendeeRequest.checkCreate(p, e, now, scheduleAgent, session);
                } else {
                    log.warn("Did not find user: " + attendeeEmail);
                }

            }
        }

    }

    public net.fortuna.ical4j.model.Calendar getCalendar(CalEvent calEvent) {
        net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
        calendar.getProperties().add(new ProdId("-//milton.io//iCal4j 1.0//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        //calendar.getProperties().add(CalScale.GREGORIAN);
        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        String sTimezone = calEvent.getTimezone();
        TimeZone timezone = null;
        if (sTimezone != null && sTimezone.length() > 0) {
            timezone = registry.getTimeZone(sTimezone); // Eg Pacific/Auckland
        }
        if (timezone == null) {
            timezone = registry.getTimeZone("Pacific/Auckland");
            log.warn("Couldnt find timezone: " + sTimezone + ", using default: " + timezone);
        }
        VTimeZone tz = timezone.getVTimeZone();
        calendar.getComponents().add(tz);
        net.fortuna.ical4j.model.DateTime start = CalUtils.toCalDateTime(calEvent.getStartDate(), timezone);
        net.fortuna.ical4j.model.DateTime finish = CalUtils.toCalDateTime(calEvent.getEndDate(), timezone);
        String summary = calEvent.getSummary();
        VEvent vevent = new VEvent(start, finish, summary);
        //vevent.getProperties().add(new Uid(UUID.randomUUID().toString()));
        vevent.getProperties().add(new Uid(calEvent.getId().toString()));
        vevent.getProperties().add(tz.getTimeZoneId());
        TzId tzParam = new TzId(tz.getProperties().getProperty(Property.TZID).getValue());
        vevent.getProperties().getProperty(Property.DTSTART).getParameters().add(tzParam);

        Session session = SessionManager.session();
        List<AttendeeRequest> attendees = AttendeeRequest.findByOrganisorEvent(calEvent, session);
        if (!attendees.isEmpty()) {
            if (calEvent.getOrganisor() != null) {
                Organizer organizer = new Organizer(URI.create("mailto:" + calEvent.getOrganisor().getEmail()));
                vevent.getProperties().add(organizer);
            } else {
                log.warn("There is no organisaor");
            }
        }
        
                       
        for (AttendeeRequest ar : attendees) {
            String email;
            StringBuilder sbCommonName = new StringBuilder();
            String stat;
            if (ar.getAttendee() != null) {
                Profile p = ar.getAttendee();
                sbCommonName.append(p.getFormattedName());
                stat = "1.2"; // delivered ok
                email = p.getEmail();
            } else {
                if (ar.getFirstName() != null) {
                    sbCommonName.append(ar.getFirstName());
                }
                if (ar.getSurName() != null) {
                    sbCommonName.append(" ").append(ar.getFirstName());
                }
                stat = "5.3"; // we dont deliver to non-system users at the moment
                email = ar.getMail();
            }
            String cn = sbCommonName.toString().trim();
            Attendee a = new Attendee(URI.create("mailto:" + email));
            a.getParameters().add(Role.REQ_PARTICIPANT);

            a.getParameters().add(new Cn(cn));

            Parameter ss = new ScheduleStatus(stat);
            a.getParameters().add(ss);

            if (ar.getParticipationStatus() == null) {
                ar.setParticipationStatus(AttendeeRequest.PARTSTAT_NEEDS_ACTION);
            }
            PartStat partStat = new PartStat(ar.getParticipationStatus());
            a.getParameters().add(partStat);

            vevent.getProperties().add(a);
        }

        calendar.getComponents().add(vevent);
        return calendar;
    }

    public String getCalendarICal(CalEvent calEvent) {
        net.fortuna.ical4j.model.Calendar calendar = getCalendar(calEvent);
        return formatIcal(calendar);
    }

    public String getInviteICal(AttendeeRequest attendeeRequest) {
        net.fortuna.ical4j.model.Calendar calendar = getCalendar(attendeeRequest.getOrganiserEvent());
        // Need to add method=request
        VEvent ev = event(calendar);
        ev.getProperties().add(Method.REQUEST);
        return formatIcal(calendar);
    }

    /**
     * Given a CalEvent which contains updated information, apply that to the
     * parsed ical text in the calendar object
     *
     * @param calEvent
     * @param calendar
     * @return
     */
    public String updateIcalText(CalEvent calEvent, net.fortuna.ical4j.model.Calendar calendar) {
        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        VEvent vevent = event(calendar);

        String sTimezone = calEvent.getTimezone();
        log.info("source timezone id: " + sTimezone);
        TimeZone timezone = null;
        if (sTimezone != null && sTimezone.length() > 0) {
            timezone = registry.getTimeZone(sTimezone); // Eg Pacific/Auckland
        }
//        //VTimeZone tz = timezone.getVTimeZone();
//        Iterator it = calendar.getComponents().iterator();
//        while (it.hasNext()) {
//            Object c = it.next();
//            if (c instanceof VTimeZone) {
//                it.remove();
//            }
//        }
//        it = vevent.getProperties().iterator();
//        while (it.hasNext()) {
//            Object c = it.next();
//            if (c instanceof TzId) {
//                it.remove();
//            }
//        }
//        if (timezone != null) {
//            VTimeZone tz = timezone.getVTimeZone();
//            calendar.getComponents().add(tz);
//            vevent.getProperties().add(tz.getTimeZoneId());
//        } else {
//            log.warn("No timezone!");
//        }

        net.fortuna.ical4j.model.DateTime start = CalUtils.toCalDateTime(calEvent.getStartDate(), timezone);
        net.fortuna.ical4j.model.DateTime finish = CalUtils.toCalDateTime(calEvent.getEndDate(), timezone);

        vevent.getStartDate().setDate(start);
        vevent.getStartDate().setTimeZone(timezone);

        vevent.getEndDate().setDate(finish);
        vevent.getEndDate().setTimeZone(timezone);

        String summary = calEvent.getSummary();
        if (summary == null || summary.length() == 0) {
            throw new RuntimeException("no summary");
        }

        vevent.getSummary().setValue(summary);
        if (vevent.getDescription() != null) {
            vevent.getDescription().setValue(calEvent.getDescription());
        } else {
            Description d = new Description(calEvent.getDescription());
            vevent.getProperties().add(d);
        }

        return formatIcal(calendar);
    }

    /**
     * Given an updated calendar object, apply updates to CalEvent
     *
     * @param calendar
     * @param calEvent
     * @param session
     */
    private void _setCalendar(net.fortuna.ical4j.model.Calendar calendar, CalEvent calEvent, boolean isAccept, Session session) {
        VEvent ev = event(calendar);
        calEvent.setStartDate(ev.getStartDate().getDate());
        Date endDate = null;
        if (ev.getEndDate() != null) {
            endDate = ev.getEndDate().getDate();
        }
        calEvent.setEndDate(endDate);
        String summary = null;
        if (ev.getSummary() != null) {
            summary = ev.getSummary().getValue();
        }
        calEvent.setSummary(summary);
        String tzId = getTimeZoneId(calendar);
        calEvent.setTimezone(tzId);
        calEvent.setModifiedDate(new Date());

        String loc = null;
        if (ev.getLocation() != null) {
            ev.getLocation().getValue();
        }
        calEvent.setLocation(loc);
        session.save(calEvent);
        if (!isAccept) {
            log.info("not an invitation, so check/create invites");
            setRsvps(ev, calEvent, session);
        }
    }

    private VEvent event(net.fortuna.ical4j.model.Calendar cal) {
        return (VEvent) cal.getComponent("VEVENT");
    }

    public String getTimeZoneId(net.fortuna.ical4j.model.Calendar calendar) {
        Iterator it = calendar.getComponents().iterator();
        while (it.hasNext()) {
            Object c = it.next();
            if (c instanceof VTimeZone) {
                VTimeZone tz = (VTimeZone) c;
                net.fortuna.ical4j.model.property.TzId tzId = tz.getTimeZoneId();
                if (tzId != null) {
                    return tzId.getValue();
                }
            }
        }
        return null;
    }

    public String getDefaultColor() {
        return defaultColor;
    }

    public void setDefaultColor(String defaultColor) {
        this.defaultColor = defaultColor;
    }

    /**
     * Updates teh CalEvent from the ical data. Also updates any child attendee
     * requests
     *
     * @param event
     * @param data
     * @param callback
     * @param session
     * @return
     * @throws IOException
     * @throws ParserException
     */
    private String _update(CalEvent event, String data, UpdatedAttendeeCallback callback, Session session) throws IOException, ParserException {
        System.out.println("Update Event--");
        System.out.println(data);
        System.out.println("----");
        CalendarBuilder builder = new CalendarBuilder();
        net.fortuna.ical4j.model.Calendar calendar = builder.build(new ByteArrayInputStream(data.getBytes("UTF-8")));
        AttendeeRequest invite = AttendeeRequest.findByName(event.getName(), session);
        boolean isAccept = invite != null;
        boolean needsReInvite = hasSchedulingInfoChanged(event, calendar);
        log.info("needsReInvite? " + needsReInvite);
        _setCalendar(calendar, event, isAccept, session);

        List<AttendeeRequest> attendeeRequests = AttendeeRequest.findByOrganisorEvent(event, session);
        if (!attendeeRequests.isEmpty()) {
            log.info("update attendee requests");
            for (AttendeeRequest ar : attendeeRequests) {
                CalEvent attendeeEvent = ar.getAttendeeEvent();
                if (attendeeEvent != null) {
                    updateAttendeeEvent(ar, needsReInvite, attendeeEvent, event, calendar, callback, session);
                }
            }
        }

        return formatIcal(calendar);
    }

    public String formatIcal(net.fortuna.ical4j.model.Calendar calendar) {
        CalendarOutputter outputter = new CalendarOutputter();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            outputter.output(calendar, bout);
        } catch (IOException | ValidationException ex) {
            throw new RuntimeException(ex);
        }
        return bout.toString();
    }

    public String getCalendarInvitationsCTag(Profile user) {
        try {
            // combine and hash names and mod dates for AR's
            List<AttendeeRequest> list = getAttendeeRequests(user, true);
            MessageDigest cout = MessageDigest.getInstance("SHA");
            Charset charset = Charset.forName("UTF-8");
            for (AttendeeRequest ar : list) {
                String s = ar.getName() + "-" + ar.getOrganiserEvent().getModifiedDate() + "-" + ar.getParticipationStatus();
                cout.update(s.getBytes(charset));
            }
            byte[] arr = cout.digest();
            String hash = DigestUtils.shaHex(arr);
            return hash;
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<AttendeeRequest> getAttendeeRequests(Profile user) {
        return getAttendeeRequests(user, true);
    }

    public List<AttendeeRequest> getAttendeeRequests(Profile user, boolean includeAckd) {
        log.info("getAttendeeRequests: " + user.getName());
        List<AttendeeRequest> list = new ArrayList<>();
        if (user.getAttendeeRequests() != null) {
            for (AttendeeRequest ar : user.getAttendeeRequests()) {
                if (includeAckd || !ar.isAcknowledged() && ar.getAttendeeEvent() == null) {
                    list.add(ar);
                }
            }
        }
        log.info("getAttendeeRequests: found requests: " + list.size());
        return list;
    }

    /**
     * Has any of the scheduling related fields in the event changed relative to
     * its calendar representation
     *
     * @param event
     * @param calendar
     * @return
     */
    private boolean hasSchedulingInfoChanged(CalEvent event, net.fortuna.ical4j.model.Calendar calendar) {
        VEvent ev = event(calendar);
        Date calStart = ev.getStartDate().getDate();
        Date calEnd = ev.getEndDate().getDate();
        String calTzId = getTimeZoneId(calendar);
        String calLoc = null;
        if (ev.getLocation() != null) {
            ev.getLocation().getValue();
        }

        return anyChanged(calStart, event.getStartDate(), calEnd, event.getEndDate(), calTzId, event.getTimezone(), calLoc, event.getLocation());

    }

    /**
     * Given a series of pairs, return true if any of the pairs have not-equal
     * values
     *
     * @param pairs
     * @return
     */
    private boolean anyChanged(Object... pairs) {
        for (int i = 0; i < pairs.length; i += 2) {
            Object v1 = pairs[i];
            Object v2 = pairs[i + 1];
            if (v1 == null) {
                if (v2 != null) {
                    return true;
                }
            } else {
                if (!v1.equals(v2)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String findEmail(Attendee att) {
        String mail = att.getCalAddress().toString();
        mail = mail.replace("mailto:", "");
        return mail;
    }

    public interface UpdatedEventCallback {

        public void updated(String ical, CalEvent updated) throws IOException;
    }

    public interface UpdatedAttendeeCallback {

        void updated(CalEvent updated) throws IOException;

        void deleted(CalEvent deleted) throws IOException;
    }
}
