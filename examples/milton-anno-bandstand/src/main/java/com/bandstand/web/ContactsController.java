/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bandstand.web;

import com.bandstand.domain.Musician;
import com.bandstand.domain.SessionManager;

import io.milton.annotations.AddressBooks;
import io.milton.annotations.ChildrenOf;
import io.milton.annotations.ContactData;
import io.milton.annotations.Get;
import io.milton.annotations.ModifiedDate;
import io.milton.annotations.PutChild;
import io.milton.annotations.ResourceController;
import io.milton.common.ModelAndView;
import io.milton.http.exceptions.BadRequestException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.sourceforge.cardme.engine.VCardEngine;
import net.sourceforge.cardme.io.VCardWriter;
import net.sourceforge.cardme.vcard.VCard;
import net.sourceforge.cardme.vcard.VCardImpl;
import net.sourceforge.cardme.vcard.features.EmailFeature;
import net.sourceforge.cardme.vcard.features.TelephoneFeature;
import net.sourceforge.cardme.vcard.types.BeginType;
import net.sourceforge.cardme.vcard.types.EmailType;
import net.sourceforge.cardme.vcard.types.EndType;
import net.sourceforge.cardme.vcard.types.FormattedNameType;
import net.sourceforge.cardme.vcard.types.NameType;
import net.sourceforge.cardme.vcard.types.TelephoneType;
import net.sourceforge.cardme.vcard.types.UIDType;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For accessing other musicians as contacts via carddav
 *
 * @author brad
 */
@ResourceController
public class ContactsController {

    private static final Logger log = LoggerFactory.getLogger(ContactsController.class);

    @ChildrenOf
    public MusicianAddressBooksHome getAddressBookHome(Musician m) {
        return new MusicianAddressBooksHome(m);
    }

    @ChildrenOf
    @AddressBooks
    public MusicianAddressBook getAddressBook(MusicianAddressBooksHome m) {
        return new MusicianAddressBook(m.musician);
    }

    @ChildrenOf
    public List<MusicianContact> getMusicianContacts(MusicianAddressBook addressBook) {
        List<MusicianContact> list = new ArrayList<MusicianContact>();
        for (Musician m : Musician.findAll(SessionManager.session())) {
            list.add(new MusicianContact(m));
        }
        return list;
    }

    @ContactData
    @Get
    public byte[] getContactData(MusicianContact c) {
        Musician m = c.contact;
        try {
            VCardImpl vcard = new VCardImpl();
            vcard.setBegin(new BeginType());
            vcard.setID(m.getContactUid());
            String uid = m.getContactUid();
            if (uid == null || uid.length() == 0) {
                uid = m.getName();
            }
            vcard.setUID(new UIDType(uid));
            vcard.setFormattedName(new FormattedNameType(m.getGivenName() + " " + m.getSurName()));
            vcard.setName(new NameType(m.getSurName(), m.getGivenName()));
            if (!StringUtils.isBlank(m.getTelephonenumber())) {
                vcard.addTelephoneNumber(new TelephoneType(m.getTelephonenumber()));
            }
            if (!StringUtils.isBlank(m.getMail())) {
                vcard.addEmail(new EmailType(m.getMail()));
            }
            vcard.setEnd(new EndType());
            VCardWriter writer = new VCardWriter();
            writer.setVCard(vcard);
            return writer.buildVCardString().getBytes("UTF-8");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @PutChild
    public MusicianContact createMusicianContact(MusicianAddressBook abook, String newName, byte[] vcardData) throws BadRequestException {

        Transaction tx = SessionManager.session().beginTransaction();
        try {
            VCardEngine cardEngine = new VCardEngine();
            String vc = new String(vcardData);
            System.out.println("---- contact ----");
            System.out.println(vc);
            System.out.println("-------");
            VCard vcard = cardEngine.parse(vc);
            Musician m = new Musician();
            if (vcard.getUID() != null) {
                m.setContactUid(vcard.getUID().getUID());
            } else {
                m.setContactUid(UUID.randomUUID().toString());
            }
            m.setName(newName);
            m.setCreatedDate(new Date());
            m.setGivenName(vcard.getName().getGivenName());
            m.setSurName(vcard.getName().getFamilyName());
            m.setModifiedDate(new Date());
            {
                Iterator<TelephoneFeature> it = vcard.getTelephoneNumbers();
                while (it.hasNext()) {
                    m.setTelephonenumber(it.next().getTelephone());
                }
            }
            {
                Iterator<EmailFeature> itEmails = vcard.getEmails();
                while (itEmails.hasNext()) {
                    m.setMail(itEmails.next().getEmail());
                }
            }

            SessionManager.session().save(m);
            SessionManager.session().flush();
            tx.commit();
            return new MusicianContact(m);
        } catch (Exception e) {
            tx.rollback();
            log.error("exception uploading musician contact", e);
            throw new BadRequestException(e.getMessage());
        }
    }

    @PutChild
    public MusicianContact updateMusicianContact(MusicianContact contact, byte[] vcardData) throws BadRequestException {
        log.info("updateMusicianContact");
        Transaction tx = SessionManager.session().beginTransaction();
        try {
            Musician m = contact.getMusician();
            VCardEngine cardEngine = new VCardEngine();
            String vc = new String(vcardData);
            System.out.println("---- contact ----");
            System.out.println(vc);
            System.out.println("-------");

            VCard vcard = cardEngine.parse(vc);
            if (vcard.getUID() != null) {
                m.setContactUid(vcard.getUID().getUID());
            }

            if (vcard.getName() != null) {
                m.setGivenName(vcard.getName().getGivenName());
                m.setSurName(vcard.getName().getFamilyName());
            } else {
                log.warn("No name feature in supplied vcard: " + vc);
            }
            m.setModifiedDate(new Date());
            {
                Iterator<TelephoneFeature> it = vcard.getTelephoneNumbers();
                while (it.hasNext()) {
                    m.setTelephonenumber(it.next().getTelephone());
                }
            }
            {
                Iterator<EmailFeature> itEmails = vcard.getEmails();
                while (itEmails.hasNext()) {
                    m.setMail(itEmails.next().getEmail());
                }
            }
            m.setModifiedDate(new Date());
            SessionManager.session().save(m);
            SessionManager.session().flush();
            tx.commit();
            return contact;
        } catch (Exception e) {
            tx.rollback();
            log.error("exception uploading musician contact", e);
            throw new BadRequestException(e.getMessage());
        }
    }

    @ModifiedDate
    public Date getContactModDate(MusicianContact c) {
        return c.getMusician().getModifiedDate();
    }

    @Get
    public ModelAndView renderAddressBookPage(MusicianAddressBook addressBook) {
        return new ModelAndView("addressBook", addressBook, "abook");
    }

    public class MusicianContact {

        private final Musician contact;

        public MusicianContact(Musician contact) {
            this.contact = contact;
        }

        public String getId() {
            return contact.getId() + "";
        }

        public String getName() {
            return contact.getName();
        }

        public Musician getMusician() {
            return contact;
        }
    }

    public class MusicianAddressBooksHome {

        private final Musician musician;

        public MusicianAddressBooksHome(Musician musician) {
            this.musician = musician;
        }

        public String getName() {
            return "abooks";
        }
    }

    public class MusicianAddressBook {

        private final Musician musician;

        public MusicianAddressBook(Musician musician) {
            this.musician = musician;
        }

        public String getName() {
            return "default";
        }
    }
}
