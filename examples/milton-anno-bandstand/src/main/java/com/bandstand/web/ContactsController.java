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
import info.ineighborhood.cardme.io.VCardWriter;
import info.ineighborhood.cardme.vcard.VCardImpl;
import info.ineighborhood.cardme.vcard.types.EmailType;
import info.ineighborhood.cardme.vcard.types.NameType;
import info.ineighborhood.cardme.vcard.types.TelephoneType;
import io.milton.annotations.AddressBooks;
import io.milton.annotations.ChildrenOf;
import io.milton.annotations.ContactData;
import io.milton.annotations.Get;
import io.milton.annotations.ResourceController;
import io.milton.common.ModelAndView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * For accessing other musicians as contacts via carddav
 *
 * @author brad
 */
@ResourceController
public class ContactsController {

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
    public byte[] getContactData(MusicianContact c) {
        Musician m = c.contact;
        try {
            VCardImpl vcard = new VCardImpl();
            vcard.setName(new NameType(m.getSurName(), m.getGivenName()));
            vcard.addTelephoneNumber(new TelephoneType(m.getTelephonenumber()));
            vcard.addEmail(new EmailType(m.getMail()));
            VCardWriter writer = new VCardWriter();
            writer.setVCard(vcard);
            return writer.buildVCardString().getBytes("UTF-8");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
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
        
        public String getName() {
            return contact.getId() + "";
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
