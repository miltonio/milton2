/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ettrema.http.caldav.demo;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.values.HrefList;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.property.BeanPropertyResource;
import com.ettrema.http.CalendarResource;
import com.ettrema.http.acl.HrefPrincipleId;
import com.ettrema.http.acl.Principal;
import com.ettrema.http.caldav.CalDavPrincipal;
import com.ettrema.http.caldav.ICalFormatter;
import com.ettrema.http.carddav.CardDavPrincipal;
import com.ettrema.ldap.Condition;
import com.ettrema.ldap.LdapContact;
import com.ettrema.ldap.LdapPrincipal;
import com.ettrema.mail.Mailbox;
import com.ettrema.mail.MessageFolder;
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
    private TScheduleInboxResource scheduleInboxResource;
    private TScheduleOutboxResource scheduleOutboxResource;
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

    public TCalDavPrincipal(TFolderResource parent, String name, String password, TFolderResource calendarHome, TScheduleInboxResource scheduleInboxResource, TScheduleOutboxResource scheduleOutboxResource, TFolderResource dropBox, TFolderResource addressBookHome) {
        super(parent, name);
        this.principleId = new HrefPrincipleId(getHref());
        this.calendarHome = calendarHome;
        this.addressBookHome = addressBookHome;
        this.scheduleInboxResource = scheduleInboxResource;
        this.scheduleOutboxResource = scheduleOutboxResource;
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

    public TScheduleInboxResource getScheduleInboxResource() {
        return scheduleInboxResource;
    }

    public void setScheduleInboxResource(TScheduleInboxResource scheduleInboxResource) {
        this.scheduleInboxResource = scheduleInboxResource;
    }

    public TScheduleOutboxResource getScheduleOutboxResource() {
        return scheduleOutboxResource;
    }

    public void setScheduleOutboxResource(TScheduleOutboxResource scheduleOutboxResource) {
        this.scheduleOutboxResource = scheduleOutboxResource;
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
    public String getScheduleInboxUrl() {
        if (scheduleInboxResource != null) {
            return scheduleInboxResource.getHref();
        } else {
            return null;
        }
    }

    @Override
    public String getScheduleOutboxUrl() {
        if (scheduleOutboxResource != null) {
            return scheduleOutboxResource.getHref();
        } else {
            return null;
        }

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
    public Principal.PrincipleId getIdenitifer() {
        return principleId;
    }

    @Override
    protected Object clone(TFolderResource newParent) {
        return new TCalDavPrincipal(newParent, name, password, calendarHome, scheduleInboxResource, scheduleOutboxResource, dropBox, addressBookHome);
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
}
