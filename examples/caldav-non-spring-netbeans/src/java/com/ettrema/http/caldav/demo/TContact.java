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

import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.ReplaceableResource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.property.BeanPropertyResource;
import com.ettrema.http.AddressResource;
import com.ettrema.ldap.LdapContact;
import info.ineighborhood.cardme.engine.VCardEngine;
import info.ineighborhood.cardme.vcard.VCard;
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
@BeanPropertyResource(value="ldap")
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
	protected Object clone(TFolderResource newParent) {
		TContact e = new TContact((TCalendarResource) newParent, name);
		e.setData(data);
		return e;
	}

	@Override
	public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
		out.write(data.getBytes());
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
