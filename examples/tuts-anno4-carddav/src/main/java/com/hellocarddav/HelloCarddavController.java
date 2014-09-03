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
package com.hellocarddav;

import io.milton.annotations.Authenticate;
import io.milton.annotations.Calendars;
import io.milton.annotations.ChildrenOf;
import io.milton.annotations.ContactData;
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
public class HelloCarddavController {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HelloCarddavController.class);
    private final List<User> users = new ArrayList<User>();

    public HelloCarddavController() {
        createUser("jack");
        createUser("jill");
    }

    @Root
    public HelloCarddavController getRoot() {
        return this;
    }

    @ChildrenOf
    public UsersHome getUsersHome(HelloCarddavController root) {
        return new UsersHome();
    }

    @ChildrenOf
    @Users
    public List<User> getUsers(UsersHome usersHome) {
        return users;
    }

    @ChildrenOf
    public ContactsHome getCalendarsHome(User user) {
        return new ContactsHome(user);
    }

    @ChildrenOf
    @Calendars
    public AddressBook getCalendarsHome(ContactsHome cals) {
        return new AddressBook(cals.user);
    }

    @ChildrenOf
    public List<Contact> getCalendar(AddressBook cal) {
        return cal.user.getContacts();
    }

    @Get
    @ContactData
    public byte[] getMeetingData(Contact m) {
        return m.getVcardData();
    }

    @PutChild
    public Contact createContact(AddressBook cal, byte[] vcard, String newName) {
        log.info("CreateContact: " + newName);
        Contact m = new Contact();
        m.setVcardData(vcard);
        m.setName(newName);
        m.setId(System.currentTimeMillis()); // just a unique ID for use with locking and etags
        m.setModifiedDate(new Date());
        cal.user.getContacts().add(m);
        return m;
    }

    @PutChild
    public Contact updateContact(Contact m, byte[] vcard) {
        log.info("updateContact: " + m.getName());
        m.setVcardData(vcard);
        m.setModifiedDate(new Date());
        return m;
    }

    @Authenticate
    public String getPassword(User user) {
        return user.getPassword();
    }

    @UniqueId
    public long getUniqueId(Contact m) {
        return m.getId();
    }

    @ModifiedDate
    public Date getModifiedDate(Contact m) {
        return m.getModifiedDate();
    }

    public class UsersHome {
        public String getName() {
            return "users";
        }
    }

    public class ContactsHome {

        private final User user;

        public ContactsHome(User user) {
            this.user = user;
        }

        public String getName() {
            return "addressBooks";
        }
    }

    public class AddressBook {

        private final User user;

        public AddressBook(User user) {
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
        u.setContacts(new ArrayList<Contact>());
        users.add(u);
        log.info("Created user: " + name + " with password: " + u.getPassword());
        return u;
    }
}
