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

package info.ineighborhood.cardme.vcard;

import info.ineighborhood.cardme.db.Persistable;
import info.ineighborhood.cardme.util.Util;
import info.ineighborhood.cardme.vcard.errors.VCardException;
import info.ineighborhood.cardme.vcard.features.AddressFeature;
import info.ineighborhood.cardme.vcard.features.AgentFeature;
import info.ineighborhood.cardme.vcard.features.BeginFeature;
import info.ineighborhood.cardme.vcard.features.BirthdayFeature;
import info.ineighborhood.cardme.vcard.features.CategoriesFeature;
import info.ineighborhood.cardme.vcard.features.ClassFeature;
import info.ineighborhood.cardme.vcard.features.DisplayableNameFeature;
import info.ineighborhood.cardme.vcard.features.EmailFeature;
import info.ineighborhood.cardme.vcard.features.EndFeature;
import info.ineighborhood.cardme.vcard.features.ExtendedFeature;
import info.ineighborhood.cardme.vcard.features.FormattedNameFeature;
import info.ineighborhood.cardme.vcard.features.GeographicPositionFeature;
import info.ineighborhood.cardme.vcard.features.KeyFeature;
import info.ineighborhood.cardme.vcard.features.LabelFeature;
import info.ineighborhood.cardme.vcard.features.LogoFeature;
import info.ineighborhood.cardme.vcard.features.MailerFeature;
import info.ineighborhood.cardme.vcard.features.NameFeature;
import info.ineighborhood.cardme.vcard.features.NicknameFeature;
import info.ineighborhood.cardme.vcard.features.NoteFeature;
import info.ineighborhood.cardme.vcard.features.OrganizationFeature;
import info.ineighborhood.cardme.vcard.features.PhotoFeature;
import info.ineighborhood.cardme.vcard.features.ProductIdFeature;
import info.ineighborhood.cardme.vcard.features.ProfileFeature;
import info.ineighborhood.cardme.vcard.features.RevisionFeature;
import info.ineighborhood.cardme.vcard.features.RoleFeature;
import info.ineighborhood.cardme.vcard.features.SortStringFeature;
import info.ineighborhood.cardme.vcard.features.SoundFeature;
import info.ineighborhood.cardme.vcard.features.SourceFeature;
import info.ineighborhood.cardme.vcard.features.TelephoneFeature;
import info.ineighborhood.cardme.vcard.features.TimeZoneFeature;
import info.ineighborhood.cardme.vcard.features.TitleFeature;
import info.ineighborhood.cardme.vcard.features.UIDFeature;
import info.ineighborhood.cardme.vcard.features.URLFeature;
import info.ineighborhood.cardme.vcard.features.VersionFeature;
import java.util.Collection;
import java.util.Iterator;

/**
 * Copyright (c) 2004, Neighborhood Technologies
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * Neither the name of Neighborhood Technologies nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * 
 * @author George El-Haddad
 * @author Jeff Prickett
 * <br/>
 * Feb 4, 2010
 * 
 * <p>
 * This is a VCard interface compliant with RFC-2426 and implements the following
 * sections and sub-sections of the RFC:
 * <pre>
 *    2.1 PREDEFINED TYPE USAGE ......................................5
 *     2.1.1 BEGIN and END Type ......................................5
 *     2.1.2 NAME Type ...............................................5
 *     2.1.3 PROFILE Type ............................................5
 *     2.1.4 SOURCE Type .............................................5
 *    3.1 IDENTIFICATION TYPES .......................................8
 *     3.1.1 FN Type Definition ......................................8
 *     3.1.2 N Type Definition .......................................9
 *     3.1.3 NICKNAME Type Definition ................................9
 *     3.1.4 PHOTO Type Definition ..................................10
 *     3.1.5 BDAY Type Definition ...................................11
 *    3.2 DELIVERY ADDRESSING TYPES .................................11
 *     3.2.1 ADR Type Definition ....................................11
 *     3.2.2 LABEL Type Definition ..................................13
 *    3.3 TELECOMMUNICATIONS ADDRESSING TYPES .......................13
 *     3.3.1 TEL Type Definition ....................................14
 *     3.3.2 EMAIL Type Definition ..................................15
 *     3.3.3 MAILER Type Definition .................................15
 *    3.4 GEOGRAPHICAL TYPES ........................................16
 *     3.4.1 TZ Type Definition .....................................16
 *     3.4.2 GEO Type Definition ....................................16
 *    3.5 ORGANIZATIONAL TYPES ......................................17
 *     3.5.1 TITLE Type Definition ..................................17
 *     3.5.2 ROLE Type Definition ...................................18
 *     3.5.3 LOGO Type Definition ...................................18
 *     3.5.4 AGENT Type Definition ..................................19
 *     3.5.5 ORG Type Definition ....................................20
 *    3.6 EXPLANATORY TYPES .........................................20
 *     3.6.1 CATEGORIES Type Definition .............................20
 *     3.6.2 NOTE Type Definition ...................................21
 *     3.6.3 PRODID Type Definition .................................21
 *     3.6.4 REV Type Definition ....................................22
 *     3.6.5 SORT-STRING Type Definition ............................22
 *     3.6.6 SOUND Type Definition ..................................23
 *     3.6.7 UID Type Definition ....................................24
 *     3.6.8 URL Type Definition ....................................25
 *     3.6.9 VERSION Type Definition ................................25
 *    3.7 SECURITY TYPES ............................................25
 *     3.7.1 CLASS Type Definition ..................................26
 *     3.7.2 KEY Type Definition ....................................26
 *    3.8 EXTENDED TYPES ............................................27
 * </pre>
 * </p>
 */
public interface VCard extends Cloneable, Persistable {

	/**
	 * <p>Returns the beginning type declaration of the VCard.</p>
	 *
	 * @return {@link BeginFeature}
	 */
	public BeginFeature getBegin();
	
	/**
	 * <p>Sets the beginning type declaration of the VCard.</p>
	 *
	 * @param begin
	 */
	public void setBegin(BeginFeature begin);
	
	/**
	 * <p>Returns true if this VCard has a beginning type declaration.</p>
	 *
	 * @return boolean
	 */
	public boolean hasBegin();
	
	/**
	 * <p>Returns the end type declaration of the VCard.</p>
	 *
	 * @return {@link EndFeature}
	 */
	public EndFeature getEnd();
	
	/**
	 * <p>Sets the end type declaration of the VCard.</p>
	 *
	 * @param end
	 */
	public void setEnd(EndFeature end);
	
	/**
	 * <p>Returns true if this VCard has an end type declaration.</p>
	 *
	 * @return boolean
	 */
	public boolean hasEnd();
	
	/**
	 * <p>Returns the displayable name of this vcard.</p>
	 *
	 * @return {@link DisplayableNameFeature}
	 */
	public DisplayableNameFeature getDisplayableNameFeature();
	
	/**
	 * <p>Sets the displayable name of this vcard.</p>
	 *
	 * @param name
	 */
	public void setDisplayableNameFeature(DisplayableNameFeature name);
	
	/**
	 * <p>Returns true if this vcard has a displayable name.</p>
	 *
	 * @return boolean
	 */
	public boolean hasDisplayableNameFeature();
	
	/**
	 * <p>Returns the profile of this vcard.</p>
	 *
	 * @return {@link ProfileFeature}
	 */
	public ProfileFeature getProfile();
	
	/**
	 * <p>Sets the profile of this vcard.</p>
	 *
	 * @param profile
	 */
	public void setProfile(ProfileFeature profile);
	
	/**
	 * <p>Returns true if this vcard has a profile.</p>
	 *
	 * @return boolean
	 */
	public boolean hasProfile();
	
	/**
	 * <p>Returns the source of this vcard.</p>
	 *
	 * @return {@link SourceFeature}
	 */
	public SourceFeature getSource();
	
	/**
	 * <p>Sets the source for this vcard.</p>
	 *
	 * @param source
	 */
	public void setSource(SourceFeature source);
	
	/**
	 * <p>Returns true if this vcard has a source./p>
	 *
	 * @return boolean
	 */
	public boolean hasSource();
	
	/**
	 * <p>Returns the formatted name of this vcard.</p>
	 * 
	 * @return {@link FormattedNameFeature}
	 */
	public FormattedNameFeature getFormattedName();
	
	/**
	 * <p>Sets the formatted name for this vcard. This feature cannot be left
	 * null. If not set then it will use the full name from the name feature
	 * as the formatted name.</p>
	 *
	 * @param formattedName
	 * @throws NullPointerException
	 */
	public void setFormattedName(FormattedNameFeature formattedName) throws NullPointerException;
	
	/**
	 * <p>Returns the name.</p>
	 *
	 * @return {@link NameFeature}
	 */
	public NameFeature getName();
	
	/**
	 * <p>Sets the name for this vcard. This feature cannot be null as all
	 * vcards must have at least a name.</p>
	 *
	 * @param name
	 * @throws NullPointerException
	 */
	public void setName(NameFeature name) throws NullPointerException;
	
	/**
	 * <p>Sets a list of nicknames for this vcard.</p>
	 *
	 * @param nicknames
	 */
	public void setNicknames(NicknameFeature nicknames);
	
	/**
	 * <p>Returns a list of nicknames that this vcard holds.</p>
	 *
	 * @return NicknameFeature
	 */
	public NicknameFeature getNicknames();
	
	/**
	 * <p>Returns true if this vcard has nicknames.</p>
	 *
	 * @return boolean
	 */
	public boolean hasNicknames();
	
	/**
	 * <p>Returns a list of photos contained in this vcard.</p>
	 *
	 * @return {@link Iterator}&lt;PhotoFeature&gt;
	 */
	public Iterator<PhotoFeature> getPhotos();
	
	/**
	 * <p>Adds a photo to this vcard.</p>
	 * 
	 * @param photo
	 */
	public void addPhoto(PhotoFeature photo);
	
	/**
	 * <p>Removes the specified photo.</p>
	 * 
	 * @param photo
	 */
	public void removePhoto(PhotoFeature photo);
	
	/**
	 * <p>Returns true if the specified photo exists in this vcard.</p>
	 * 
	 * @param photo
	 * @return boolean
	 */
	public boolean containsPhoto(PhotoFeature photo);
	
	/**
	 * <p>Adds a collection of photos to this vcard.</p>
	 * 
	 * @param photos
	 */
	public void addAllPhotos(Collection<PhotoFeature> photos);
	
	/**
	 * <p>Clears all photos from this vcard.</p>
	 */
	public void clearPhotos();
	
	/**
	 * <p>Returns true if this vcard has photos.</p>
	 * 
	 * @param boolean
	 */
	public boolean hasPhotos();
	
	/**
	 * <p>Returns the birthday date.</p>
	 *
	 * @return {@link BirthdayFeature}
	 */
	public BirthdayFeature getBirthDay();
	
	/**
	 * <p>Sets the birth date for this vcard.</p>
	 *
	 * @param birthday
	 */
	public void setBirthday(BirthdayFeature birthday);
	
	/**
	 * <p>Returns true if this vcard has been set a birth date.</p>
	 *
	 * @return boolean
	 */
	public boolean hasBirthday();
	
	/**
	 * <p>Returns a list of addresses contained by this vcard.</p>
	 * 
	 * @return {@link Iterator}&lt;AddressFeature&gt;
	 */
	public Iterator<AddressFeature> getAddresses();
	
	/**
	 * <p>Adds an address to this vcard.</p>
	 * 
	 * @param address
	 */
	public void addAddress(AddressFeature address);
	
	/**
	 * <p>Removes the specified address from this vcard.</p>
	 * 
	 * @param address
	 */
	public void removeAddress(AddressFeature address);
	
	/**
	 * <p>Returns true if the specified address exists.</p>
	 * 
	 * @param address
	 * @return boolean
	 */
	public boolean containsAddress(AddressFeature address);
	
	/**
	 * <p>Adds a collection of addresses to this vcard.</p>
	 * 
	 * @param addresses
	 */
	public void addAllAddresses(Collection<AddressFeature> addresses);
	
	/**
	 * <p>Clear all addresses from this vcard.</p>
	 */
	public void clearAddresses();
	
	/**
	 * <p>Returns true if this vcard has addresses.</p>
	 * 
	 * @return boolean
	 */
	public boolean hasAddresses();
	
	/**
	 * <p>Returns all labels contained in this vcard.</p>
	 *
	 * @return {@link Iterator}&lt;LabelFeature&gt;
	 */
	public Iterator<LabelFeature> getLables();
	
	/**
	 * <p>Returns the label for a given address. This method will
	 * return null if the address does not exist or if a null address
	 * was passed in.</p>
	 * 
	 * @param address
	 * @return {@link LabelFeature}
	 */
	public LabelFeature getLabelFor(AddressFeature address);
	
	/**
	 * <p>Sets a label for a specified address. A VCardException is thrown
	 * if a label is trying to be set on an address that does not exist in
	 * this vcard.</p>
	 *
	 * @param label
	 * @param forAddress
	 * @throws VCardException
	 */
	public void setLabel(LabelFeature label, AddressFeature forAddress) throws VCardException;
	
	/**
	 * <p>Removes a label for a specified address. A VCardException is thrown
	 * if a label is trying to be removed from an address that does not exist
	 * in this vcard.</p>
	 *
	 * @param label
	 * @param forAddress
	 * @throws VCardException
	 */
	public void removeLabel(LabelFeature label, AddressFeature forAddress) throws VCardException;
	
	/**
	 * <p>Returns true if the specified address has a label.</p>
	 *
	 * @param address
	 * @return boolean
	 */
	public boolean hasLabel(AddressFeature address);
	
	/**
	 * <p>Removes all labels associated to all present addresses.</p>
	 */
	public void clearLabels();
	
	/**
	 * <p>Returns true if the specified label exists within this vcard.</p>
	 *
	 * @param label
	 * @return boolean
	 */
	public boolean containsLabel(LabelFeature label);
	
	/**
	 * <p>Returns all telephone numbers contained in this vcard.</p>
	 * 
	 * @return {@link Iterator}&lt;TelephoneFeature&gt;
	 */
	public Iterator<TelephoneFeature> getTelephoneNumbers();
	
	/**
	 * <p>Adds a phone number to this vcard.</p>
	 * 
	 * @param phoneNumber
	 */
	public void addTelephoneNumber(TelephoneFeature phoneNumber);
	
	/**
	 * <p>Removes the specified phone number from this vcard.</p>
	 * 
	 * @param phoneNumber
	 */
	public void removeTelephoneNumber(TelephoneFeature phoneNumber);
	
	/**
	 * <p>Returns true if the the specified phone number exists in this vcard.</p>
	 * 
	 * @param phoneNumber
	 * @return boolean
	 */
	public boolean containsTelephoneNumber(TelephoneFeature phoneNumber);
	
	/**
	 * <p>Adds a collection of phone numbers to this vcard.</p>
	 * 
	 * @param phoneNumbers
	 */
	public void addAllTelephoneNumber(Collection<TelephoneFeature> phoneNumbers);
	
	/**
	 * <p>Clears all phone numbers from this vcard.</p>
	 */
	public void clearTelephoneNumbers();
	
	/**
	 * <p>Returns true if this vcard has phone numbers.</p>
	 * 
	 * @return boolean
	 */
	public boolean hasTelephoneNumbers();
	
	/**
	 * <p>Returns a list of emails contained in this vcard.</p>
	 *
	 * @return {@link Iterator}&lt;EmailFeature&gt;
	 */
	public Iterator<EmailFeature> getEmails();
	
	/**
	 * <p>Adds an email address to this vcard.</p>
	 * 
	 * @param email
	 */
	public void addEmail(EmailFeature email);
	
	/**
	 * <p>Removes an email address from this vcard.</p>
	 * 
	 * @param email
	 */
	public void removeEmail(EmailFeature email);
	
	/**
	 * <p>Returns true if the specified email address exists in this vcard.</p>
	 * 
	 * @param email
	 * @return boolean
	 */
	public boolean containsEmail(EmailFeature email);
	
	/**
	 * <p>Adds a collection of email addresses to this vcard.</p>
	 * 
	 * @param emailAddresses
	 */
	public void addAllEmails(Collection<EmailFeature> emailAddresses);
	
	/**
	 * <p>Clears all email addresses from this vcard.</p>
	 */
	public void clearEmails();
	
	/**
	 * <p>Returns true if this vcard has email addresses.</p>
	 * 
	 * @return boolean
	 */
	public boolean hasEmails();
	
	/**
	 * <p>Returns the mailer.</p>
	 *
	 * @return {@link MailerFeature}
	 */
	public MailerFeature getMailer();
	
	/**
	 * <p>Sets the mailer used to create this vcard.</p>
	 *
	 * @param mailer
	 */
	public void setMailer(MailerFeature mailer);
	
	/**
	 * <p>Returns true if this vcard has a mailer.</p>
	 *
	 * @return boolean
	 */
	public boolean hasMailer();
	
	/**
	 * <p>Returns the time zone of this vcard.</p>
	 * 
	 * @return {@link TimeZoneFeature}
	 */
	public TimeZoneFeature getTimeZone();
	
	/**
	 * <p>Sets the time zone for this vcard.</p>
	 *
	 * @param timeZone
	 */
	public void setTimeZone(TimeZoneFeature timeZone);
	
	/**
	 * <p>Returns true if this vcard has a time zone.</p>
	 *
	 * @return boolean
	 */
	public boolean hasTimeZone();
	
	/**
	 * <p>Returns the geographic position of this vcard.</p>
	 *
	 * @return {@link GeographicPositionFeature}
	 */
	public GeographicPositionFeature getGeographicPosition();
	
	/**
	 * <p>Sets the geographical position of this vcard.</p>
	 *
	 * @param geographicPosition
	 */
	public void setGeographicPosition(GeographicPositionFeature geographicPosition);
	
	/**
	 * <p>Returns true if this vcard has geographical positioning data.</p>
	 *
	 * @return boolean
	 */
	public boolean hasGeographicPosition();
	
	/**
	 * <p>Returns the title.</p>
	 * 
	 * @return {@link TitleFeature}
	 */
	public TitleFeature getTitle();
	
	/**
	 * <p>Sets the title for this vcard.</p>
	 *
	 * @param title
	 */
	public void setTitle(TitleFeature title);
	
	/**
	 * <p>Returns true if this vcard has a title.</p>
	 *
	 * @return boolean
	 */
	public boolean hasTitle();
	
	/**
	 * <p>Returns the role.</p>
	 *
	 * @return {@link RoleFeature}
	 */
	public RoleFeature getRole();
	
	/**
	 * <p>Sets a role for this vcard.</p>
	 *
	 * @param role
	 */
	public void setRole(RoleFeature role);
	
	/**
	 * <p>Returns true if this vcard has a role.</p>
	 *
	 * @return boolean
	 */
	public boolean hasRole();
	
	/**
	 * <p>Returns a list of logos contained in this vcard.</p>
	 *
	 * @return {@link Iterator}&lt;LogoFeature&gt;
	 */
	public Iterator<LogoFeature> getLogos();
	
	/**
	 * <p>Adds a logo to this vcard.</p>
	 * 
	 * @param logo
	 */
	public void addLogo(LogoFeature logo);
	
	/**
	 * <p>Removes a logo from this vcard.</p>
	 * 
	 * @param logo
	 */
	public void removeLogo(LogoFeature logo);
	
	/**
	 * <p>Returns true if the specified logo exists.</p>
	 * 
	 * @param logo
	 * @return boolean
	 */
	public boolean containsLogo(LogoFeature logo);
	
	/**
	 * <p>Adds a collection of logos to this vcard.</p>
	 * 
	 * @param logos
	 */
	public void addAllLogos(Collection<LogoFeature> logos);
	
	/**
	 * <p>Clears all logos from this vcard.</p>
	 */
	public void clearLogos();
	
	/**
	 * <p>Returns true if this vcard has logos.</p>
	 * 
	 * @return boolean
	 */
	public boolean hasLogos();
	
	/**
	 * <p>Returns a list of agents contained in this vcard.</p>
	 *
	 * @return {@link Iterator}&lt;AgentFeature&gt;
	 */
	public Iterator<AgentFeature> getAgents();
	
	/**
	 * <p>Adds an agent to this vcard.</p>
	 * 
	 * @param agent
	 */
	public void addAgent(AgentFeature agent);
	
	/**
	 * <p>Removes an agent from this vcard.</p>
	 * 
	 * @param agent
	 */
	public void removeAgent(AgentFeature agent);
	
	/**
	 * <p>Returns true if the specified agent exists in this vcard.</p>
	 * 
	 * @param agent
	 * @return boolean
	 */
	public boolean containsAgent(AgentFeature agent);
	
	/**
	 * <p>Adds a collection of agents to this vcard.</p>
	 * 
	 * @param agents
	 */
	public void addAllAgents(Collection<AgentFeature> agents);
	
	/**
	 * <p>Clears all agents from this vcard.</p>
	 */
	public void clearAgents();
	
	/**
	 * <p>Returns true if this vcard has agents.</p>
	 * 
	 * @return boolean
	 */
	public boolean hasAgents();
	
	/**
	 * <p>Returns a list of organizations from this vcard.</p>
	 *
	 * @return {@link OrganizationFeature}
	 */
	public OrganizationFeature getOrganizations();
	
	/**
	 * <p>Sets a list of organizations for this vcard.</p>
	 *
	 * @param organizations
	 */
	public void setOrganizations(OrganizationFeature organizations);
	
	/**
	 * <p>Returns true if this vcard has organizations.</p>
	 *
	 * @return boolean
	 */
	public boolean hasOrganizations();
	
	/**
	 * <p>Returns the categories of this vcard.</p>
	 * 
	 * @return {@link CategoriesFeature}
	 */
	public CategoriesFeature getCategories();
	
	/**
	 * <p>Sets categories for this vcard.</p>
	 *
	 * @param categories
	 */
	public void setCategories(CategoriesFeature categories);
	
	/**
	 * <p>Returns true if this vcard has categories.</p>
	 *
	 * @return boolean
	 */
	public boolean hasCategories();
	
	/**
	 * <p>Returns a list of notes contained in this vcard.</p>
	 *
	 * @return {@link Iterator}&lt;NoteFeature&gt;
	 */
	public Iterator<NoteFeature> getNotes();
	
	/**
	 * <p>Adds a note to this vcard.</p>
	 * 
	 * @param note
	 */
	public void addNote(NoteFeature note);
	
	/**
	 * <p>Removes the specified note from this vcard.</p>
	 * 
	 * @param note
	 */
	public void removeNote(NoteFeature note);
	
	/**
	 * <p>Returns true if the specified note exists in this vcard.</p>
	 * 
	 * @param note
	 * @return boolean
	 */
	public boolean containsNote(NoteFeature note);
	
	/**
	 * <p>Adds a collection of notes to this vcard.</p>
	 * 
	 * @param notes
	 */
	public void addAllNotes(Collection<NoteFeature> notes);
	
	/**
	 * <p>Clears all notes from this vcard.</p>
	 */
	public void clearNotes();
	
	/**
	 * <p>Returns true if this vcard has notes.</p>
	 * 
	 * @return boolean
	 */
	public boolean hasNotes();
	
	/**
	 * <p>Returns the product id.</p>
	 *
	 * @return {@link ProductIdFeature}
	 */
	public ProductIdFeature getProductId();
	
	/**
	 * <p>Sets the product id for this vcard.</p>
	 *
	 * @param productId
	 */
	public void setProductId(ProductIdFeature productId);
	
	/**
	 * <p>Returns true if this vcard has a product id.</p>
	 *
	 * @return boolean
	 */
	public boolean hasProductId();
	
	/**
	 * <p>Returns the revision date of this vcard.</p>
	 *
	 * @return {@link RevisionFeature}
	 */
	public RevisionFeature getRevision();
	
	/**
	 * <p>Sets the revision date of this vcard.</p>
	 *
	 * @param revision
	 */
	public void setRevision(RevisionFeature revision);
	
	/**
	 * <p>Returns true if this vcard has a revision date.</p>
	 *
	 * @return boolean
	 */
	public boolean hasRevision();
	
	/**
	 * <p>Returns the sort string of this vcard.</p>
	 *
	 * @return {@link SortStringFeature}
	 */
	public SortStringFeature getSortString();
	
	/**
	 * <p>Sets the sort string for this vcard.</p>
	 *
	 * @param sortString
	 */
	public void setSortString(SortStringFeature sortString);
	
	/**
	 * <p>Returns true if this vcard has a sort string.</p>
	 *
	 * @return boolean
	 */
	public boolean hasSortString();
	
	/**
	 * <p>Returns a list of sounds contained in this vcard.</p>
	 *
	 * @return {@link Iterator}&lt;SoundFeature&gt;
	 */
	public Iterator<SoundFeature> getSounds();
	
	/**
	 * <p>Adds a sound to this vcard.</p>
	 * 
	 * @param sound
	 */
	public void addSound(SoundFeature sound);
	
	/**
	 * <p>Removes a sound from this vcard.</p>
	 * 
	 * @param sound
	 */
	public void removeSound(SoundFeature sound);
	
	/**
	 * <p>Returns true if the specified sound exists.</p>
	 * 
	 * @param sound
	 * @return boolean
	 */
	public boolean containsSound(SoundFeature sound);
	
	/**
	 * <p>Adds a collection of sounds to this vcard.</p>
	 * 
	 * @param sounds
	 */
	public void addAllSounds(Collection<SoundFeature> sounds);
	
	/**
	 * <p>Clears all sounds from this vcard.</p>
	 */
	public void clearSounds();
	
	/**
	 * <p>Returns true if this vcard has sounds.</p>
	 * 
	 * @return boolean
	 */
	public boolean hasSounds();
	
	/**
	 * <p>Returns the UID of this vcard.</p>
	 *
	 * @return {@link UIDFeature}
	 */
	public UIDFeature getUID();
	
	/**
	 * <p>Sets a UID for this vcard.</p>
	 *
	 * @param uid
	 */
	public void setUID(UIDFeature uid);
	
	/**
	 * <p>Returns true if this vcard has a UID.</p>
	 *
	 * @return boolean
	 */
	public boolean hasUID();
	
	/**
	 * <p>Returns a list of URL contained in this vcard.</p>
	 *
	 * @return {@link Iterator}&lt;URLFeature&gt;
	 */
	public Iterator<URLFeature> getURLs();
	
	/**
	 * <p>Adds a URL to this vcard.</p>
	 *
	 * @param url
	 */
	public void addURL(URLFeature url);
	
	/**
	 * <p>Removes a URL from this vcard.</p>
	 *
	 * @param url
	 */
	public void removeURL(URLFeature url);
	
	/**
	 * <p>Returns true if the specified URL exists in this vcard.</p>
	 *
	 * @param url
	 * @return boolean
	 */
	public boolean containsURL(URLFeature url);
	
	/**
	 * <p>Adds a collection of URLs to this vcard.</p>
	 *
	 * @param urls
	 */
	public void addAllURLs(Collection<URLFeature> urls);
	
	/**
	 * <p>Clears all URLs from this vcard.</p>
	 */
	public void clearURLs();
	
	/**
	 * <p>Returns true if this card has URLs.</p>
	 *
	 * @return boolean
	 */
	public boolean hasURLs();
	
	/**
	 * <p>Returns the version of this vcard.</p>
	 *
	 * @return {@link VersionFeature}
	 */
	public VersionFeature getVersion();
	
	/**
	 * <p>Sets the version for this vcard. This feature cannot
	 * be left null, and if not set it will use a default.</p>
	 *
	 * @param version
	 */
	public void setVersion(VersionFeature version) throws NullPointerException;
	
	/**
	 * <p>Returns the security class of this vcard.</p>
	 * 
	 * @return {@link ClassFeature}
	 */
	public ClassFeature getSecurityClass();
	
	/**
	 * <p>Sets a security class for this vcard.</p>
	 *
	 * @param securityClass
	 */
	public void setSecurityClass(ClassFeature securityClass);
	
	/**
	 * <p>Returns true if this vcard has been assigned a security class.</p>
	 *
	 * @return boolean
	 */
	public boolean hasSecurityClass();
	
	/**
	 * <p>Returns a list of keys contained in this vcard.</p>
	 * 
	 * @return {@link Iterator}<KeyFeature>
	 */
	public Iterator<KeyFeature> getKeys();
	
	/**
	 * <p>Adds a key to this vcard.</p>
	 * 
	 * @param key
	 */
	public void addKey(KeyFeature key);
	
	/**
	 * <p>Removes the specified key from this vcard.</p>
	 * 
	 * @param key
	 */
	public void removeKey(KeyFeature key);
	
	/**
	 * <p>Returns true if the specified key exists.</p>
	 * 
	 * @param key
	 * @return boolean
	 */
	public boolean containsKey(KeyFeature key);
	
	/**
	 * <p>Adds a collection of keys to this vcard.</p>
	 * 
	 * @param keys
	 */
	public void addAllKeys(Collection<KeyFeature> keys);
	
	/**
	 * <p>Clears all keys from this vcard.</p>
	 */
	public void clearKeys();
	
	/**
	 * <p>Returns true if this vcard has keys.</p>
	 * 
	 * @return boolean
	 */
	public boolean hasKeys();
	
	/**
	 * <p>Returns a list of extended types contained in this vcard.</p>
	 * 
	 * @return {@link Iterator}&lt;ExtendedFeature&gt;
	 */
	public Iterator<ExtendedFeature> getExtendedTypes();
	
	/**
	 * <p>Adds an extended type to this vcard.</p>
	 *
	 * @param extension
	 */
	public void addExtendedType(ExtendedFeature extension);
	
	/**
	 * <p>Removes the specified extended type from this vcard.</p>
	 *
	 * @param extension
	 */
	public void removeExtendedType(ExtendedFeature extension);
	
	/**
	 * <p>Returns true if the specified extended type exists in this vcard.</p>
	 *
	 * @param extension
	 * @return boolean
	 */
	public boolean containsExtendedType(ExtendedFeature extension);
	
	/**
	 * <p>Adds a collection of extended types to this vcard.</p>
	 *
	 * @param extensions
	 */
	public void addAllExtendedTypes(Collection<ExtendedFeature> extensions);
	
	/**
	 * <p>Clears all extended types from this vcard.</p>
	 */
	public void clearExtendedTypes();
	
	/**
	 * <p>Returns true if this vcard has extended types.</p>
	 *
	 * @return boolean
	 */
	public boolean hasExtendedTypes();
	
	/**
	 * <p>Clears all features in this vcard and resets the
	 * mandatory features of FormattedName, Name and Version
	 * to default values. Note that Name feature will be reset
	 * with empty strings.</p>
	 */
	public void clear();
	
	/**
	 * <p>Performs a java style equality with one extra bit of checking.
	 * In the end we check if the hash codes of both objects are equal.
	 * The hash code is determined by the overridden hash code function.</p>
	 * 
	 * @param obj
	 * @return boolean
	 */
	@Override
	public abstract boolean equals(Object obj);
	
	/**
	 * <p>Generates a unique hash code based on all the data
	 * contained within in the object.</p>
	 * 
	 * @see Util#generateHashCode(String...)
	 * @return int
	 */
	@Override
	public abstract int hashCode();
	
	/**
	 * <p>Concatenates all data types in the object and returns it.</p>
	 * 
	 * @return {@link String}
	 */
	@Override
	public abstract String toString();
	
	/**
	 * <p>Returns a new clone of the vcard.</p>
	 *
	 * @return {@link VCard}
	 */
	public abstract VCard clone();
}