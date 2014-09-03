/*
 * Copyright 2012 McEvoy Software Ltd.
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
package com.mycontacts.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * This class is to simulate the sort of relational/hierarchial data model typically
 * found in business applications.
 * 
 * This sort of data would normally be in a database or accessed over a webservice
 * but for simplicity of the tutorial we are holding it in memory
 *
 * @author brad
 */
public final class ContactsDao {

    private final Set<Contact> contacts = new HashSet<Contact>();
    private final Set<User> users = new HashSet<User>();

    private long contactsVersion = 0; // used for the ctag    
    
    public ContactsDao() {
        addUser("admin", "password");                        
    }

    public synchronized void incrementContactsVersion() {
        contactsVersion++;
    }

    public long getContactsVersion() {
        return contactsVersion;
    }

    
    
    public Contact addContact(String fileName) {
        return addContact(fileName, null, null, null, null);
    }
    
    
    public Contact addContact(String firstName, String surName) {
        return addContact(firstName, surName, null, null);
    }
    
    public Contact addContact(String firstName, String surName, String email, String phone) {
        String fileName = UUID.randomUUID().toString();
        return addContact(fileName, firstName, surName, email, phone);
    }
    
    public Contact addContact(String fileName, String firstName, String surName, String email, String phone) {
        Contact c = new Contact();
        c.setFileName(fileName);
        c.setUid(fileName);
        c.setGivenName(firstName);
        c.setSurName(surName);
        c.setMail(email);
        c.setTelephonenumber(phone);
        contacts.add(c);
        return c;
    }
    
    public User addUser(String username, String password) {
        User u = new User();
        u.setUserName(username);
        u.setPassword(password);
        users.add(u);
        return u;
    }
    
    public final User findUser(String username) {
        for(User u : users ) {
            if( u.getUserName().equals(username)) {
                return u;
            }            
        }
        return null;
    }

    public Set<User> getUsers() {
        return users;
    }

    public Set<Contact> getContacts() {
        return contacts;
    }

    
    
    public Contact findContact(String uid) {
        for( Contact c : contacts) {
            if( c.getUid().equals(uid)) {
                return c;
            }
        }
        return null;
    }
    

    public class User {
        private String userName;
        private String password;
                
        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }                
    }
    
    public class Contact {
        private String fileName;
        private String uid;
        private String firstName;
        private String lastName;
        private String phone;
        private String email;
        private String icalData;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        
        
        public String getUid() {
            return uid;
        }

        public void setUid(String name) {
            this.uid = name;
        }

        public String getGivenName() {
            return firstName;
        }

        public void setGivenName(String firstName) {
            this.firstName = firstName;
        }

        public String getSurName() {
            return lastName;
        }

        public void setSurName(String lastName) {
            this.lastName = lastName;
        }

        public String getMail() {
            return email;
        }

        public void setMail(String email) {
            this.email = email;
        }

        public String getTelephonenumber() {
            return phone;
        }

        public void setTelephonenumber(String phone) {
            this.phone = phone;
        }               

        public String getIcalData() {
            return icalData;
        }

        public void setIcalData(String icalData) {
            this.icalData = icalData;
        }                
    }
}
