/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.bandstand.web;

import com.bandstand.domain.Band;
import com.bandstand.domain.BandMember;
import com.bandstand.domain.Gig;
import com.bandstand.domain.Musician;
import com.bandstand.domain.SessionManager;
import com.bandstand.util.CalUtils;
import io.milton.annotations.AccessControlList;
import io.milton.annotations.Calendars;
import io.milton.annotations.ChildrenOf;
import io.milton.annotations.CreatedDate;
import io.milton.annotations.Delete;
import io.milton.annotations.Get;
import io.milton.annotations.ICalData;
import io.milton.annotations.ModifiedDate;
import io.milton.annotations.PutChild;
import io.milton.annotations.ResourceController;
import io.milton.http.annotated.AnnoResource;
import io.milton.resource.AccessControlledResource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.parameter.TzId;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Gig is a calendar item (ie an event), and it is inside a logical calendar,
 * which is itse
 *
 * @author brad
 */
@ResourceController
public class GigController {

    private static final Logger log = LoggerFactory.getLogger(GigController.class);

    @ChildrenOf
    public MusicianCalendarsHome getCalendarsHomeForMusician(Musician m) {
        return new MusicianCalendarsHome(m);
    }

    @Calendars
    @ChildrenOf
    public List<MusicianCalendar> getCalendarsForMusician(MusicianCalendarsHome calHome) {
        List<MusicianCalendar> cals = new ArrayList<MusicianCalendar>();
        if (calHome.musician.getBandMembers() != null) {
            for (BandMember bm : calHome.musician.getBandMembers()) {
                MusicianCalendar cal = new MusicianCalendar(calHome.musician, bm.getBand());
                cals.add(cal);
            }
        }
        return cals;
    }

    /**
     * This method is just a bogus example showing that you can have ACL methods
     * for different objects in a hierarchy. In this case we make a rule that a band
     * with a name ending with 'ReadOnly' will be read-only for everyone.
     * 
     * @param target
     * @param currentUser
     * @return 
     */
    @AccessControlList
    public List<AccessControlledResource.Priviledge> getMusicianCalendarPrivs(MusicianCalendar target, Musician currentUser) {
        System.out.println("getMusicianCalendarPrivs");
        if (target.musician == currentUser) {
            Band band = target.band;
            if (band.getName().endsWith("ReadOnly")) {
                return AccessControlledResource.READ_CONTENT;
            }
            return AccessControlledResource.READ_WRITE;
        } else {
            return null;
        }
    }

    @AccessControlList
    public List<AccessControlledResource.Priviledge> getMusicianPrivs(Musician target, Musician currentUser) {
        if (target == currentUser) {
            return AccessControlledResource.READ_WRITE;
        } else {
            // This prevents read access to each others calendars
            return null;

            // This gives read access to each others calendars
            //return AccessControlledResource.READ_CONTENT;
        }
    }

    @Delete
    public void delete(Gig gig) {
        Transaction tx = SessionManager.session().beginTransaction();
        try {
            SessionManager.session().delete(gig);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        }
    }

    @Delete
    public void delete(MusicianCalendar cal) {
        Transaction tx = SessionManager.session().beginTransaction();
        try {
            cal.musician.removeFromBand(cal.band, SessionManager.session());

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        }
    }

    @ChildrenOf
    public List<Gig> getGigs(MusicianCalendar cal) {
        return cal.getBand().getGigs();
    }

    @PutChild
    public Gig uploadGig(MusicianCalendar cal, String name, byte[] arr) throws IOException, ParserException {
        Gig gig = cal.band.addGig(name, null);
        gig.setFileName(name);
        updateGig(gig, arr);
        return gig;
    }

    @PutChild
    public Gig updateGig(Gig gig, byte[] ical) throws IOException, ParserException {
        log.info("updateGig: " + gig.getDisplayName());
        Transaction tx = SessionManager.session().beginTransaction();
        try {
            CalendarBuilder builder = new CalendarBuilder();
            ByteArrayInputStream bin = new ByteArrayInputStream(ical);
            System.out.println(bin.toString());
            Calendar calendar = builder.build(bin);
            VEvent ev = (VEvent) calendar.getComponent("VEVENT");
            gig.setStartDate(ev.getStartDate().getDate());
            Date endDate = null;
            if (ev.getEndDate() != null) {
                endDate = ev.getEndDate().getDate();
            }
            gig.setEndDate(endDate);
            String summary = null;
            if (ev.getSummary() != null) {
                summary = ev.getSummary().getValue();
            }
            gig.setDisplayName(summary);

            gig.setModifiedDate(new Date());

            SessionManager.session().save(gig);
            SessionManager.session().flush();
            tx.commit();
            System.out.println("Updated gig: " + gig.getStartDate() + " - " + gig.getEndDate());
        } catch (Exception e) {
            tx.rollback();
        }
        return gig;
    }

    @Get
    @ICalData
    public byte[] getIcalData(Gig gig) {
        net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
        calendar.getProperties().add(new ProdId("-//spliffy.org//iCal4j 1.0//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        //calendar.getProperties().add(CalScale.GREGORIAN);
        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        TimeZone timezone = registry.getTimeZone("Pacific/Auckland");
        VTimeZone tz = timezone.getVTimeZone();
        calendar.getComponents().add(tz);
        net.fortuna.ical4j.model.DateTime start = CalUtils.toCalDateTime(gig.getStartDate(), timezone);
        net.fortuna.ical4j.model.DateTime finish = CalUtils.toCalDateTime(gig.getEndDate(), timezone);
        String summary = gig.getDisplayName();
        VEvent vevent = new VEvent(start, finish, summary);
        //vevent.getProperties().add(new Uid(UUID.randomUUID().toString()));
        vevent.getProperties().add(new Uid(gig.getFileName()));
        vevent.getProperties().add(tz.getTimeZoneId());
        TzId tzParam = new TzId(tz.getProperties().getProperty(Property.TZID).getValue());
        vevent.getProperties().getProperty(Property.DTSTART).getParameters().add(tzParam);

        Attendee attendee = new Attendee(URI.create("mailto:xxx1@bradmcevoy.com"));
        attendee.getParameters().add(Role.REQ_PARTICIPANT);//required participants.
        attendee.getParameters().add(Rsvp.TRUE);//to get the status request from the attendees
        vevent.getProperties().add(attendee);

        calendar.getComponents().add(vevent);

        CalendarOutputter outputter = new CalendarOutputter();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            outputter.output(calendar, bout);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        System.out.println("ical: " + bout.toString());
        return bout.toByteArray();
    }

    @ModifiedDate
    public Date getModifiedDate(Gig gig) {
        return gig.getModifiedDate();
    }

    @CreatedDate
    public Date getCreatedDate(Gig gig) {
        return gig.getCreatedDate();
    }

    public class MusicianCalendarsHome {

        private final Musician musician;

        public MusicianCalendarsHome(Musician musician) {
            this.musician = musician;
        }

        public String getName() {
            return "cals";
        }
    }

    public class MusicianCalendar {

        private final Musician musician;
        private final Band band;

        public MusicianCalendar(Musician musician, Band band) {
            this.musician = musician;
            this.band = band;
        }

        public String getName() {
            return band.getName();
        }

        public Musician getMusician() {
            return musician;
        }

        public Band getBand() {
            return band;
        }
    }
}
