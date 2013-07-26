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

import info.ineighborhood.cardme.engine.VCardEngine;
import info.ineighborhood.cardme.vcard.VCard;
import io.milton.common.ReadingException;
import io.milton.common.StreamUtils;
import io.milton.common.WritingException;
import io.milton.http.Range;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.annotations.BeanPropertyResource;
import io.milton.resource.AddressResource;
import io.milton.resource.GetableResource;
import io.milton.resource.LdapContact;
import io.milton.resource.ReplaceableResource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
@BeanPropertyResource(value = "ldap")
public class TContact extends TResource implements GetableResource, ReplaceableResource, AddressResource, LdapContact {

    private static final Logger log = LoggerFactory.getLogger(TContact.class);
    private String data;
    // LDAP properties
    private String givenName;
    private String surName;
    private String mail;
    private String organizationName;
    private String telephonenumber;

    public TContact(TFolderResource parent, String name) {
        super(parent, name);
    }

    @Override
    protected Object clone(TFolderResource newParent, String destName) {
        TContact e = new TContact((TCalendarResource) newParent, destName);
        e.setData(data);
        return e;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
        out.write(data.getBytes("UTF-8"));
    }

    @Override
    public String getContentType(String accepts) {
        return "text/vcard";
    }

    @Override
    public void replaceContent(InputStream in, Long length) throws BadRequestException, ConflictException, NotAuthorizedException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            StreamUtils.readTo(in, bout);
        } catch (ReadingException ex) {
            throw new RuntimeException(ex);
        } catch (WritingException ex) {
            throw new RuntimeException(ex);
        }
        this.data = bout.toString(); // should check character encoding
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
        VCardEngine engine = new VCardEngine();
        try {
            VCard vcard = engine.parse(data);
            System.out.println("VARD: " + vcard);
            setGivenName(vcard.getName().getGivenName());
            setSurName(vcard.getName().getFamilyName());
            setTelephonenumber(vcard.getTelephoneNumbers().next().getTelephone());
            setMail(vcard.getEmails().next().getEmail());

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    public String getAddressData() {
        return this.data;
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
