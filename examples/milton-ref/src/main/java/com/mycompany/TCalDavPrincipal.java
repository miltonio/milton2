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

import io.milton.common.StreamUtils;
import io.milton.http.caldav.ICalFormatter;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.values.HrefList;
import io.milton.http.values.SupportedCalendarComponentList;
import io.milton.http.values.SupportedCalendarComponentListsSet;
import io.milton.ldap.Condition;
import io.milton.ldap.LdapPrincipal;
import io.milton.mail.Mailbox;
import io.milton.mail.MessageFolder;
import io.milton.principal.CalDavPrincipal;
import io.milton.principal.CardDavPrincipal;
import io.milton.principal.HrefPrincipleId;
import io.milton.principal.Principal.PrincipleId;
import io.milton.annotations.BeanPropertyResource;
import io.milton.resource.CalendarResource;
import io.milton.resource.CollectionResource;
import io.milton.resource.LdapContact;
import io.milton.resource.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author brad
 */
@BeanPropertyResource(value = "ldap")
public class TCalDavPrincipal extends TFolderResource implements CalDavPrincipal, Mailbox, CalendarResource, CardDavPrincipal, LdapPrincipal, LdapContact { 

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractResource.class);
    private HrefPrincipleId principleId;
    private TFolderResource calendarHome;
    private TFolderResource addressBookHome;
    private TFolderResource dropBox;
    private String password;
    private final TMailFolder mailInbox;
    private String color = "#2952A3";// sets the default color for the user's calendar
    // LDAP properties
    private String givenName;
    private String surName;
    private String mail;
    private String organizationName;
    private String telephonenumber;

    public TCalDavPrincipal(TFolderResource parent, String name, String password, TFolderResource calendarHome, TFolderResource dropBox, TFolderResource addressBookHome) {
        super(parent, name);
        this.principleId = new HrefPrincipleId(getHref());
        this.calendarHome = calendarHome;
        this.addressBookHome = addressBookHome;
        this.dropBox = dropBox;
        this.mailInbox = new TMailFolder(this, "Inbox");
        this.password = password;
    }

    @Override
    public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException {
        log.debug("createNew");
        if (contentType.startsWith("text/calendar")) {
            TEvent e = new TEvent(this, newName);
            log.debug("created tevent: " + e.name);
            ICalFormatter formatter = new ICalFormatter();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            StreamUtils.readTo(inputStream, bout);
            bout.close();
            String data = bout.toString();
            e.setiCalData(data);
            return e;
        } else {
            throw new RuntimeException("eek");
            //log.debug( "creating a normal resource");
            //return super.createNew( newName, inputStream, length, contentType );
        }
    }

    @Override
    public List<LdapContact> searchContacts(Condition condition, int maxCount) {
        System.out.println("searchContacts: " + condition);
        List<LdapContact> results = new ArrayList<LdapContact>();
        for (Resource rAddressBook : addressBookHome.children) {
            if (rAddressBook instanceof CollectionResource) {
                try {
                    CollectionResource cr = (CollectionResource) rAddressBook;
                    for (Resource rContact : cr.getChildren()) {
                        if (rContact instanceof LdapContact) {
                            LdapContact ldapContact = (LdapContact) rContact;
                            if (condition.isMatch(ldapContact)) {
                                log.trace("searchContacts: contact matches search criteria: " + ldapContact.getName());
                                results.add(ldapContact);
                            } else {
                                log.trace("searchContacts: contact does not match search criteria: " + ldapContact.getName());
                            }
                        }
                    }
                } catch (NotAuthorizedException ex) {
                    throw new RuntimeException(ex);
                } catch (BadRequestException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        log.trace("searchContacts: " + getName() + ", results ->" + results.size());
        return results;
    }

    @Override
    public Object authenticate(String requestedUserName, String requestedPassword) {
        log.debug("authentication: " + requestedUserName + " - " + requestedPassword + " = " + password);

        if (!this.getName().equals(requestedUserName)) {
            return null;
        }
        if (password == null) {
            if (requestedPassword == null || requestedPassword.length() == 0) {
                return this;
            } else {
                return null;
            }
        } else {
            if (password.equals(requestedPassword)) {
                return "ok";
            } else {
                return null;
            }
        }
    }

    @Override
    public String getPrincipalURL() {
        return getHref();
    }

    public String getPassword() {
        return password;
    }

    public TFolderResource getCalendarHome() {
        return calendarHome;
    }

    public void setCalendarHome(TFolderResource calendarHome) {
        this.calendarHome = calendarHome;
    }

    @Override
    public HrefList getCalendarHomeSet() {
        return HrefList.asList(calendarHome.getHref());
    }

    @Override
    public HrefList getAddressBookHomeSet() {
        return HrefList.asList(addressBookHome.getHref());
    }

    @Override
    public String getAddress() {
        return addressBookHome.getHref();
    }

    @Override
    public HrefList getCalendarUserAddressSet() {
        return HrefList.asList("mailto:" + name + "@localhost", getHref());
    }

    @Override
    public String getDropBoxUrl() {
        if (dropBox != null) {
            return dropBox.getHref();
        } else {
            return null;
        }
    }

    @Override
    public PrincipleId getIdenitifer() {
        return principleId;
    }

    @Override
    protected Object clone(TFolderResource newParent, String newName) {
        return new TCalDavPrincipal(newParent, newName, password, calendarHome, dropBox, addressBookHome);
    }

    /**
     * Valiate the password for this user, required for mail support
     *
     * @param password
     * @return
     */
    @Override
    public boolean authenticate(String password) {
        Object o = authenticate(this.name, password);
        return o != null;
    }

    /**
     * Validate the password hash for this user, required for mail support
     *
     * @param passwordHash
     * @return
     */
    @Override
    public boolean authenticateMD5(byte[] passwordHash) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MessageFolder getInbox() {
        return mailInbox;
    }

    @Override
    public MessageFolder getMailFolder(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isEmailDisabled() {
        return false;
    }

    @Override
    public void storeMail(MimeMessage mm) {
        mailInbox.storeMail(mm);
    }

    @Override
    public String getCalendarDescription() {
        return name;
    }

    @Override
    public String getColor() {
        return color;
    }

    @Override
    public void setColor(String s) {
        this.color = s;
    }

    public TFolderResource getAddressBookHome() {
        return addressBookHome;
    }

    public void setAddressBookHome(TFolderResource addressBookHome) {
        this.addressBookHome = addressBookHome;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getTelephonenumber() {
        return telephonenumber;
    }

    public void setTelephonenumber(String telephonenumber) {
        this.telephonenumber = telephonenumber;
    }

    public String getCommonName() {
        return givenName + " " + surName;
    }

    @Override
    public SupportedCalendarComponentListsSet getSupportedComponentSets() {
        return null;
    }

    @Override
    public SupportedCalendarComponentList getSupportedComponentSet() {
        return null; // probably should not implement CalendarResource ... ?
    }

    

    
}
