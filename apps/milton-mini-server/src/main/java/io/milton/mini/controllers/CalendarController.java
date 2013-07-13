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
import io.milton.annotations.Get;
import io.milton.annotations.Post;
import io.milton.annotations.Principal;
import io.milton.annotations.ResourceController;
import io.milton.common.ModelAndView;
import io.milton.vfs.db.Calendar;
import io.milton.vfs.db.Profile;
import io.milton.vfs.db.utils.SessionManager;
import java.util.List;
import org.hibernate.Transaction;

@ResourceController
public class CalendarController {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CalendarController.class);

	
    public CalendarController() {

    }
       

    @ChildrenOf
    public CalendarsHome getUsersHome(Profile profile) {
        return new CalendarsHome(profile);
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
    public List<Calendar> getCalendars(CalendarsHome cals) {
        return cals.user.getCalendars();
    }
    
    @Get
    public String showUserCalendar(Calendar home) {
        System.out.println("show calendar");
        return "calendar";
    }    
    
    @Get(params={"editMode"})
    public ModelAndView showCalendarEditPage(Calendar calendar) {
        System.out.println("show calendar edit page");
        return new ModelAndView("profile", calendar, "calendarEditPage"); 
    }      
    
    @ChildOf(pathSuffix="new")
    public Calendar createNewCalendar(CalendarsHome calendarsHome, String name, @Principal Profile currentUser) {
        Calendar newCal = calendarsHome.user.newCalendar(name, currentUser);
        return newCal;
    }        
    
    @Post(bindData=true)
    public Calendar saveCalendar(Calendar calendar) {
        log.info("saveCalendar: " + calendar.getName());
        Transaction tx = SessionManager.session().beginTransaction();
        SessionManager.session().save(calendar);
        SessionManager.session().flush();
        tx.commit();
        log.info("saved cal");
        return calendar;
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
