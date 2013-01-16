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
package com.mycontacts.resource;

import com.mycontacts.model.ContactsDao.Contact;
import io.milton.http.Auth;
import io.milton.http.Range;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.GetableResource;
import io.milton.resource.ICalResource;
import io.milton.resource.ReplaceableResource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import org.apache.commons.io.IOUtils;

/**
 *
 *
 * @author brad
 */
public class ContactResource extends AbstractResource implements GetableResource, ReplaceableResource, ICalResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ContactResource.class);
    private final UserAddressBookResource parent;
    private final Contact contact;

    public ContactResource(UserAddressBookResource parent, Contact contact) {
        super(parent, parent.contactManager);
        this.parent = parent;
        this.contact = contact;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
        // we assume that the icalData on the Person object is consistent with other properties
        String ical = contact.getIcalData();
        if( ical != null ) {
            out.write(ical.getBytes("UTF-8"));
        } else {
            log.warn("ICAL data is null on resource: " + contact.getFileName());
        }
    }

    @Override
    public void replaceContent(InputStream in, Long length) throws BadRequestException, ConflictException, NotAuthorizedException {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            IOUtils.copy(in, bout);
            String icalData = bout.toString("UTF-8");
            contactManager.update(contact, icalData);
        } catch (IOException iOException) {
            throw new RuntimeException(iOException);
        }
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public String getContentType(String accept) {
        return "text/vcard";
    }

    @Override
    public String getName() {
        return contact.getFileName();
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    @Override
    public String getICalData() {
        return contact.getIcalData();
    }

    @Override
    public String getUniqueId() {
        return contact.getUid();
    }    
}
