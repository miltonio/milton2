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
package com.mycompany;

import io.milton.common.Path;
import io.milton.http.ResourceFactory;
import io.milton.http.values.HrefList;
import io.milton.resource.OAuth2Provider;
import io.milton.resource.OAuth2ProviderBean;
import io.milton.resource.Resource;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * For iCal, start off by opening a calendar at
 *
 * http://localhost:8080/users/userA/ - iCal will discover the calendar inside
 * that user.
 *
 * For Mozilla clients (eg thunderbird) connect directly to the calendar url,
 * eg
 *
 * http://localhost:8080/users/userA/calendars/cal1/
 *
 * @author brad
 */
public class TResourceFactory implements ResourceFactory {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TResourceFactory.class);
    public final static Map<String, OAuth2Provider> mapOfOauthProviders = new HashMap<String, OAuth2Provider>();
    public static final TFolderResource ROOT = new TFolderResource((TFolderResource) null, "http://localhost:8080");
    static TFolderResource users;
    static TFolderResource principals;

    static {
        principals = new TFolderResource(ROOT, "p");
        users = principals; // same as sabresav demo
        //users = new TFolderResource( principals, "users" );        
        addUser(users, "userA", "password", "userA@somewhere.com", "ACME", "555 1111");
        addUser(users, "userB", "password", "userB@somewhere.com", "ACME", "555 1121");
        addUser(users, "userC", "password", "userC@somewhere.com", "ACME", "555 1131");

        List<String> fbScopes = Arrays.asList("email");
        
        OAuth2Provider p = new OAuth2ProviderBean("fb", 
                "https://graph.facebook.com/oauth/authorize", 
                "131804060198305",
                "3acb294b071c9aec86d60ae3daf32a93", 
                "http://localhost:8080/", 
                "https://graph.facebook.com/oauth/access_token", 
                "https://graph.facebook.com/me", 
                fbScopes);
        mapOfOauthProviders.put("fb", p);
        
        List<String> googleScopes = Arrays.asList( "profile");
        p = new OAuth2ProviderBean(
                "google",   // our internal ID
                "https://accounts.google.com/o/oauth2/auth",  // authorisation url
                "22595264249-rs4r73t1qk345tn8u75p9qt3lsf3qv2n.apps.googleusercontent.com", // client ID
                "8GCs-I-LLqxi8UkTQ0qHbYAv", // client secret
                "http://localhost:8080/", // return url
                "https://www.googleapis.com/oauth2/v3/token", // URL to call to get an access token from an access code
                "https://www.googleapis.com/plus/v1/people/me", // URL to call to get profile information
                googleScopes
        );
        mapOfOauthProviders.put("google", p);
    }

    
    public static TCalDavPrincipal getUser(String name) {
        return (TCalDavPrincipal) users.child(name);
    }
    
    public static TCalDavPrincipal addUser(TFolderResource users, String name, String password, String email, String org, String phone) {
        TCalDavPrincipal user = new TCalDavPrincipal(users, name, password, null, null, null);
        user.setGivenName("joe");
        user.setSurName("blogs" + users.children.size());
        user.setMail(email);
        user.setOrganizationName(org);
        user.setTelephonenumber(phone);

        TFolderResource files = new TFolderResource(user, "files");
        
        
        TFolderResource calendars = new TFolderResource(user, "calendars");
        TCalendarResource cal1 = new TCalendarResource(calendars, "cal1");
        TEvent e = new TEvent(cal1, "event1.ics");
        e.setiCalData(createICalData());

        TFolderResource addressBooks = new TFolderResource(user, "abs");
        user.setAddressBookHome(addressBooks);
        TAddressBookResource addressBook1 = new TAddressBookResource(addressBooks, "addressbook");
        //System.out.println("created address book: " + addressBook1.getHref());
        addContact(addressBook1, "ed@blah.com", "ed", "ward", "111 222 333", "contact1.vcf");
        addContact(addressBook1, "sam@blah.com", "sam", "smith", "111 222 444", "contact2.vcf");
        addContact(addressBook1, "john@blah.com", "john", "long", "111 222 555", "contact3.vcf");

        user.setCalendarHome(calendars);
        
        return user;
    }

    private static void addContact(TAddressBookResource ab, String email, String givenName, String surName, String phone, String filename) {
        TContact c1 = new TContact(ab, filename);
        c1.setData(getCardDavData(givenName, surName, phone, email));
    }

    public static TCalDavPrincipal findUser(String name) {
        if (name.contains("@")) {
            name = name.substring(0, name.indexOf("@"));
        }
        System.out.println("find user:" + name);
        for (Resource r : users.children) {
            if (r.getName().equals(name)) {
                return (TCalDavPrincipal) r;
            }
        }
        return null;
    }

    static HrefList getPrincipalCollectionHrefs() {
        HrefList list = new HrefList();
        list.add("/users/");
        return list;
    }

    @Override
    public Resource getResource(String host, String url) {
        log.debug("getResource: url: " + url);
        Path path = Path.path(url);
        Resource r = find(path);
        log.debug("_found: " + r + " for url: " + url + " and path: " + path);
        return r;
    }

    private Resource find(Path path) {
        if (path.isRoot()) {
            return ROOT;
        }
        Resource rParent = find(path.getParent());
        if (rParent == null) {
            return null;
        }
        if (rParent instanceof TFolderResource) {
            TFolderResource folder = (TFolderResource) rParent;
            for (Resource rChild : folder.getChildren()) {
                if (rChild.getName().equals(path.getName())) {
                    return rChild;
                }
            }
            log.warn("Resource: " + path.getName() + " not found in collection: " + path.getParent() + " of type: " + rParent.getClass());
        } else {
            if (rParent != null) {
                log.warn("parent is not a folder: " + path.getParent() + " is a: " + rParent.getClass());
            } else {
                log.warn("parent not found: " + path.getParent());
            }
        }
        return null;
    }
    
    public List<Resource> getUsers() {
        System.out.println("TResourcEFactory: " + users.children);
        return users.children;
    }    

    public static String createICalData() {
        Calendar cal = Calendar.getInstance();

        String start = format(cal);
        cal.add(Calendar.HOUR, 2);
        String finish = format(cal);

        String s = "";
        s += "BEGIN:VCALENDAR\n";
        s += "PRODID:-//MailEnable.com MailEnable Calendar V1.1//EN\n";
        s += "VERSION:2.0\n";
        s += "METHOD:PUBLISH\n";
        s += "BEGIN:VTIMEZONE\n";
        s += "TZID:America/New_York\n";
        s += "X-LIC-LOCATION:America/New_York\n";
        s += "BEGIN:DAYLIGHT\n";
        s += "TZOFFSETFROM:-0500\n";
        s += "TZOFFSETTO:-0400\n";
        s += "TZNAME:EDT\n";
        s += "DTSTART:19700308T020000\n";
        s += "RRULE:FREQ=YEARLY;BYDAY=2SU;BYMONTH=3\n";
        s += "END:DAYLIGHT\n";
        s += "BEGIN:STANDARD\n";
        s += "TZOFFSETFROM:-0400\n";
        s += "TZOFFSETTO:-0500\n";
        s += "TZNAME:EST\n";
        s += "DTSTART:19701101T020000\n";
        s += "RRULE:FREQ=YEARLY;BYDAY=1SU;BYMONTH=11\n";
        s += "END:STANDARD\n";
        s += "END:VTIMEZONE\n";
        s += "BEGIN:VEVENT\n";
        s += "CREATED:20091113T212858Z\n";
        s += "LAST-MODIFIED:20090814T231840Z\n";
        s += "DTSTAMP:20090814T231840Z\n";
        s += "UID:0C4DBFA762A44E359A373562C9DE463A.CAL\n";
        s += "SUMMARY:consona\n";
        s += "PRIORITY:5\n";
        s += "ORGANIZER:mailto:vvvvv@zzzz.com\n";
        s += "DTSTART:" + start + "\n";
        s += "DTEND:" + finish + "\n";
        s += "CLASS:PUBLIC\n";
        s += "TRANSP:OPAQUE\n";
        s += "SEQUENCE:0\n";
        s += "X-MICROSOFT-CDO-ALLDAYEVENT:FALSE\n";
        s += "X-MICROSOFT-CDO-IMPORTANCE:0\n";
        s += "X-MICROSOFT-CDO-BUSYSTATUS:1\n";
        s += "END:VEVENT\n";
        s += "END:VCALENDAR\n";
        return s;
    }

    private static String getCardDavData(String givenName, String surName, String phone, String email) {
        String s = "";
        s += "BEGIN:VCARD\n";
        s += "VERSION:3.0\n";
        s += "N:" + givenName + ";" + surName + ";;;\n";
        s += "FN:" + givenName + " " + surName + "\n";
        s += "TEL;type=WORK;type=pref:" + phone + "\n";
        s += "ADR;type=WORK;type=pref;LABEL=\"42 Plantation St.\nBaytown, LA 30314\nUnited States of America\"\n";
        s += "EMAIL:" + email + "\n";
        s += "UID:95490BEA-5793-4E3B-8788-054C8B394F68-ABSPlugin\n";
        s += "REV:2011-12-15T01:04:25Z\n";
        s += "END:VCARD\n";
        return s;
    }

    private static String format(Calendar cal) {
        // "20090820T180000Z";
        String s = "" + cal.get(Calendar.YEAR);
        s += pad2(cal.get(Calendar.MONTH) + 1);
        s += pad2(cal.get(Calendar.DATE));
        s += "T";
        s += pad2(cal.get(Calendar.HOUR_OF_DAY));
        s += pad2(cal.get(Calendar.MINUTE));
        s += pad2(cal.get(Calendar.SECOND));
        s += "Z";
        return s;
    }

    private static String pad2(int i) {
        if (i < 10) {
            return "0" + i;
        } else {
            return i + "";
        }
    }
}
