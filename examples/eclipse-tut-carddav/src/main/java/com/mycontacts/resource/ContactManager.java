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
package com.mycontacts.resource;

import com.mycontacts.model.ContactsDao;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.sourceforge.cardme.engine.VCardEngine;
import net.sourceforge.cardme.io.VCardWriter;
import net.sourceforge.cardme.vcard.VCard;
import net.sourceforge.cardme.vcard.features.EmailFeature;
import net.sourceforge.cardme.vcard.features.TelephoneFeature;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class ContactManager {

    private static final Logger log = LoggerFactory.getLogger(ContactManager.class);

    private final ContactsDao contactsDao;

    public ContactManager(ContactsDao contactsDao) {
        this.contactsDao = contactsDao;
    }
        

    public ContactsDao.Contact createContact(String newName, String icalData) throws UnsupportedEncodingException {
        ContactsDao.Contact e = contactsDao.addContact(newName);
        update(e, icalData);        
        return e;
    }

    
    
    public void update(ContactsDao.Contact contact, String data) {
        log.info("update: " + data);
        VCard vcard = parse(data);
        if (vcard.getUID() != null && vcard.getUID().hasUID()) {
            contact.setUid(vcard.getUID().getUID());
        } else {
            log.warn("NO UID FOUND, making up our own");
            contact.setUid(UUID.randomUUID().toString());
        }
        if (vcard.getName() != null) {
            contact.setGivenName(vcard.getName().getGivenName());
            contact.setSurName(vcard.getName().getFamilyName());
            log.info("parsed name: " + contact.getGivenName() + " " + contact.getSurName());
        } else {
            log.warn("No name component found!");
        }
        contact.setMail(""); // reset in case none given
        Iterator<EmailFeature> it = vcard.getEmails();
        while (it.hasNext()) {
            contact.setMail(it.next().getEmail());
        }
        log.info("email: " + contact.getMail());
        if (vcard.getOrganizations() != null) {
//            contact.setOrganizationName("");
//            Iterator<String> itOrg = vcard.getOrganizations().getOrganizations();
//            while (itOrg.hasNext()) {
//                contact.setOrganizationName(itOrg.next());
//            }
        }
        String ph = getPhone(vcard);
        contact.setTelephonenumber(ph);
        log.info("phone: " + contact.getTelephonenumber());
        contact.setIcalData(data);
        contactsDao.incrementContactsVersion();
    }
    
    public VCard parse(String data) {
        VCardEngine cardEngine = new VCardEngine();
        try {
            return cardEngine.parse(data);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    private String getPhone(VCard vcard) {
        Iterator<TelephoneFeature> itPhone = vcard.getTelephoneNumbers();
        while (itPhone.hasNext()) {
            return itPhone.next().getTelephone();
        }
        return null;
    }

 

    public List<VCard> parseMultiple(InputStream inputStream) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, bout);
        String vcardText = bout.toString("UTF-8");
        VCardEngine cardEngine = new VCardEngine();
        List<VCard> vcards = cardEngine.parseMultiple(vcardText);
        return vcards;
    }

    public ContactsDao getContactsDao() {
        return contactsDao;
    }
    
    
}
