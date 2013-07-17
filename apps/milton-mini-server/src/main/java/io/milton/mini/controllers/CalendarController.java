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

import io.milton.annotations.Calendars;
import io.milton.annotations.ChildOf;
import io.milton.annotations.ChildrenOf;
import io.milton.annotations.Delete;
import io.milton.annotations.Get;
import io.milton.annotations.ICalData;
import io.milton.annotations.MakeCalendar;
import io.milton.annotations.ModifiedDate;
import io.milton.annotations.Post;
import io.milton.annotations.Principal;
import io.milton.annotations.PutChild;
import io.milton.annotations.ResourceController;
import io.milton.common.ModelAndView;
import io.milton.common.StringUtils;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.mini.DataSessionManager;
import io.milton.mini.services.CalendarService;
import io.milton.vfs.data.DataSession;
import io.milton.vfs.db.AttendeeRequest;
import io.milton.vfs.db.CalEvent;
import io.milton.vfs.db.Calendar;
import io.milton.vfs.db.Profile;
import io.milton.vfs.db.Repository;
import io.milton.vfs.db.utils.SessionManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.hibernate.Transaction;

@ResourceController
public class CalendarController {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CalendarController.class);
    @Inject
    private DataSessionManager dataSessionManager;
    @Inject
    private CalendarService calendarService;

    public CalendarController() {
    }

    @Get
    public String showUserCalendarsHome(CalendarsHome home) {
        return "calendarsHome";
    }

    @ChildrenOf
    public CalendarsHome getCalendarsHome(Profile user) {
        return new CalendarsHome(user);
    }

    @ChildrenOf
    @Calendars
    public List<Calendar> getCalendars(CalendarsHome cals, @Principal Profile profile) throws NotAuthorizedException {
        if( profile == null ) {
            log.warn("getCalendarrs with no user");
            throw new NotAuthorizedException();
        }
        return cals.user.getCalendars();
    }

    @Get
    public String showUserCalendar(Calendar home) {
        System.out.println("show calendar");
        return "calendar";
    }

    @Get(params = {"editMode"})
    public ModelAndView showCalendarEditPage(Calendar calendar) {
        System.out.println("show calendar edit page");
        return new ModelAndView("profile", calendar, "calendarEditPage");
    }

    @ChildOf(pathSuffix = "new")
    public Calendar createNewCalendar(CalendarsHome calendarsHome, String name, @Principal Profile currentUser) throws NotAuthorizedException {
        if (currentUser == null) {
            throw new NotAuthorizedException(null);
        }
        Calendar newCal = calendarsHome.user.newCalendar(name, currentUser);
        return newCal;
    }

    @Post(bindData = true)
    public Calendar saveCalendar(Calendar calendar) {
        log.info("saveCalendar: " + calendar.getName());
        Transaction tx = SessionManager.session().beginTransaction();
        SessionManager.session().save(calendar);
        SessionManager.session().flush();
        tx.commit();
        log.info("saved cal");
        return calendar;
    }
    
    @MakeCalendar
    public Calendar createNewCalendar(CalendarsHome calendarsHome, String newName, Map<QName, String> fieldsToSet, @Principal Profile currentUser) {
        Calendar newCal = calendarsHome.user.newCalendar(newName, currentUser);
        log.info("Create new calendar: " + newName);
        for( QName qname : fieldsToSet.keySet()) {
            log.info(" field: " + qname + " = " + fieldsToSet.get(qname));
        }
        SessionManager.session().save(newCal);
        return newCal;
    }

    /**
     * This should override the normal file handling from Repository (base class
     * of Calendar)
     *
     * @param calendar
     * @param request
     * @return
     */
    @ChildrenOf(override = true)
    public List<CalEvent> getEvents(Calendar calendar, Request request) {
        List<CalEvent> events = calendar.getEvents();
        if (events != null) {
            System.out.println("events: " + events.size());
        } else {
            System.out.println("null events!");
        }
        return events;
    }

    @Get
    @ICalData
    public void getEventIcal(CalEvent event, Calendar calendar, Request request, OutputStream out, Range range) throws IOException {
        DataSession ds = dataSessionManager.get(request, calendar);
        DataSession.FileNode fileNode = (DataSession.FileNode) ds.getRootDataNode().get(event.getName());
        if (fileNode != null) {
            if (range == null) {
                fileNode.writeContent(out);
            } else {
                fileNode.writeContent(out, range.getStart(), range.getFinish());
            }
        } else {
            String s = calendarService.getCalendar(event);
            out.write(s.getBytes(StringUtils.UTF8));
        }
    }

    @Get
    @ICalData
    public void getAttendeeRequestIcal(AttendeeRequest event, Request request, OutputStream out, Range range) throws IOException {
        CalEvent orgEvent = event.getOrganiserEvent();
        Calendar calendar = orgEvent.getCalendar();
        getEventIcal(orgEvent, calendar, request, out, range);
    }

    @PutChild
    public CalEvent createEvent(Calendar calendar, final String newName, InputStream inputStream, Request request, final @Principal Profile principal) throws IOException {
        log.trace("createNew: set content");
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, bout);

        final DataSession ds = dataSessionManager.get(request, calendar, true, principal);


        String icalData = bout.toString(StringUtils.UTF8.name());
//        System.out.println("new ical --- " + newName);
//        System.out.println(icalData);
//        System.out.println("---");
        CalEvent newEvent = calendarService.createEvent(calendar, newName, icalData, new CalendarService.UpdatedEventCallback() {
            public void updated(String updatedIcal) throws IOException {
                DataSession.FileNode newFileNode = (DataSession.FileNode) ds.getRootDataNode().get(newName);
                if (newFileNode == null) { // usually should be null
                    newFileNode = ds.getRootDataNode().addFile(newName);
                }
                newFileNode.setContent(new ByteArrayInputStream(updatedIcal.getBytes(StringUtils.UTF8)));
                ds.save(principal);
            }
        });

        return newEvent;
    }

    @PutChild
    public CalEvent updateEvent(CalEvent event, InputStream inputStream, Request request, Calendar calendar, @Principal Profile principal) throws IOException {
        log.trace("createNew: set content");
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, bout);

        DataSession ds = dataSessionManager.get(request, calendar, true, principal);
        DataSession.FileNode fileNode = (DataSession.FileNode) ds.getRootDataNode().get(event.getName());
        fileNode.setContent(new ByteArrayInputStream(bout.toByteArray()));
        ds.save(principal);

        String icalData = bout.toString(StringUtils.UTF8.name());
        calendarService.update(event, icalData);

        return event;
    }

    @Delete
    public void deleteEvent(CalEvent event, Request request, Calendar calendar, @Principal Profile principal) throws IOException {
        calendarService.delete(event);
        DataSession ds = dataSessionManager.get(request, calendar, true, principal);
        DataSession.FileNode fileNode = (DataSession.FileNode) ds.getRootDataNode().get(event.getName());
        fileNode.delete();
        ds.save(principal);
    }

    @ModifiedDate
    public Date getEventModifiedDate(CalEvent event) {
        return event.getModifiedDate();
    }

    public class CalendarsHome {

        private final Profile user;

        public CalendarsHome(Profile user) {
            this.user = user;
        }

        public String getName() {
            return "cals";
        }
    }
}
