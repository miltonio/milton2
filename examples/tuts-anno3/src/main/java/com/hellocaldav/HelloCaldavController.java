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
package com.hellocaldav;

import io.milton.annotations.Authenticate;
import io.milton.annotations.Calendars;
import io.milton.annotations.ChildrenOf;
import io.milton.annotations.Get;
import io.milton.annotations.ICalData;
import io.milton.annotations.ModifiedDate;
import io.milton.annotations.PutChild;
import io.milton.annotations.ResourceController;
import io.milton.annotations.Root;
import io.milton.annotations.UniqueId;
import io.milton.annotations.Users;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ResourceController
public class HelloCaldavController {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HelloCaldavController.class);
    private final List<User> users = new ArrayList<User>();

    public HelloCaldavController() {
        createUser("jack");
        createUser("jill");
    }

    @Root
    public HelloCaldavController getRoot() {
        return this;
    }

    @ChildrenOf
    public UsersHome getUsersHome(HelloCaldavController root) {
        return new UsersHome();
    }

    @ChildrenOf
    @Users
    public List<User> getUsers(UsersHome usersHome) {
        return users;
    }

    @ChildrenOf
    public CalendarsHome getCalendarsHome(User user) {
        return new CalendarsHome(user);
    }

    @ChildrenOf
    @Calendars
    public Calendar getCalendarsHome(CalendarsHome cals) {
        return new Calendar(cals.user);
    }

    @ChildrenOf
    public List<Meeting> getCalendar(Calendar cal) {
        return cal.user.getMeetings();
    }

    @Get
    @ICalData
    public byte[] getMeetingData(Meeting m) {
        return m.getIcalData();
    }

    @PutChild
    public Meeting createMeeting(Calendar cal, byte[] ical, String newName) {
        Meeting m = new Meeting();
        m.setIcalData(ical);
        m.setName(newName);
        m.setId(System.currentTimeMillis()); // just a unique ID for use with locking and etags
        m.setModifiedDate(new Date());
        cal.user.getMeetings().add(m);
        return m;
    }

    @PutChild
    public Meeting updateMeeting(Meeting m, byte[] ical) {
        m.setIcalData(ical);
        m.setModifiedDate(new Date());
        return m;
    }

    @Authenticate
    public String getPassword(User user) {
        return user.getPassword();
    }

    @UniqueId
    public long getUniqueId(Meeting m) {
        return m.getId();
    }

    @ModifiedDate
    public Date getModifiedDate(Meeting m) {
        return m.getModifiedDate();
    }

    public class UsersHome {
        public String getName() {
            return "users";
        }
    }

    public class CalendarsHome {

        private final User user;

        public CalendarsHome(User user) {
            this.user = user;
        }

        public String getName() {
            return "cals";
        }
    }

    public class Calendar {

        private final User user;

        public Calendar(User user) {
            this.user = user;
        }

        public String getName() {
            return "default";
        }
    }

    public final User createUser(String name) {
        User u = new User();
        u.setName(name);
        u.setPassword("password");
        u.setMeetings(new ArrayList<Meeting>());
        users.add(u);
        return u;
    }
}
