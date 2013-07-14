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
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.milton.vfs.db.Calendar;
import io.milton.vfs.db.utils.SessionManager;

/**
 *
 * @author brad
 */
public class CalendarService {

    private static final Logger log = LoggerFactory.getLogger(CalendarService.class);
    private String defaultColor = "blue";

    public void update(CalEvent event, String data) {
        Session session = SessionManager.session();
        try {
            _update(event, data);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch( ParserException ex) {
            throw new RuntimeException(ex);
        }
        session.save(event);
        session.save(event.getCalendar());
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
        Transaction tx = session.beginTransaction();

        session.delete(event);

        tx.commit();
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
        newEvent.setDescription(event.getDescription());
        newEvent.setEndDate(event.getEndDate());
        newEvent.setModifiedDate(new Date());
        newEvent.setName(name);
        newEvent.setStartDate(event.getStartDate());
        newEvent.setSummary(event.getSummary());
        newEvent.setTimezone(event.getTimezone());
        session.save(newEvent);
    }

    public void delete(Calendar calendar) {
        Session session = SessionManager.session();

        session.delete(calendar);
    }

    public CalEvent createEvent(Calendar calendar, String newName, String icalData) throws UnsupportedEncodingException {
        System.out.println("createEvent: newName=" + newName + " -- " + icalData);
        Session session = SessionManager.session();
        CalEvent e = new CalEvent();
        e.setName(newName);
        e.setCalendar(calendar);
        e.setCreatedDate(new Date());
        e.setModifiedDate(new Date());

        ByteArrayInputStream fin = new ByteArrayInputStream(icalData.getBytes("UTF-8"));
        CalendarBuilder builder = new CalendarBuilder();
        net.fortuna.ical4j.model.Calendar cal4jCalendar;
        try {
            cal4jCalendar = builder.build(fin);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch( ParserException ex ) {
            throw new RuntimeException(ex);
        }
        _setCalendar(cal4jCalendar, e);
        session.save(e);

        return e;
    }

    public String getCalendar(CalEvent calEvent) {

        net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
        calendar.getProperties().add(new ProdId("-//spliffy.org//iCal4j 1.0//EN"));
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

        calendar.getComponents().add(vevent);


        CalendarOutputter outputter = new CalendarOutputter();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            outputter.output(calendar, bout);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch( ValidationException ex) {
            throw new RuntimeException(ex);
        }
        return bout.toString();

    }



    private void _setCalendar(net.fortuna.ical4j.model.Calendar calendar, CalEvent calEvent) {
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
    }

    private VEvent event(net.fortuna.ical4j.model.Calendar cal) {
        return (VEvent) cal.getComponent("VEVENT");
    }


    public String getDefaultColor() {
        return defaultColor;
    }

    public void setDefaultColor(String defaultColor) {
        this.defaultColor = defaultColor;
    }

    private void _update(CalEvent event, String data) throws IOException, ParserException {
        CalendarBuilder builder = new CalendarBuilder();
        net.fortuna.ical4j.model.Calendar calendar = builder.build(new ByteArrayInputStream(data.getBytes("UTF-8")));
        _setCalendar(calendar, event);
    }
}
