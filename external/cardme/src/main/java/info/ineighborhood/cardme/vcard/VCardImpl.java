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

import info.ineighborhood.cardme.db.MarkType;
import info.ineighborhood.cardme.db.Persistable;
import info.ineighborhood.cardme.util.Util;
import info.ineighborhood.cardme.vcard.errors.ErrorSeverity;
import info.ineighborhood.cardme.vcard.errors.VCardError;
import info.ineighborhood.cardme.vcard.errors.VCardErrorHandling;
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
import info.ineighborhood.cardme.vcard.types.VersionType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
 * <br/>
 * Feb 5, 2010
 * 
 * <p>The CardMe implementation of a VCard using all standard interfaces.</p>
 */
public class VCardImpl implements VCard, VCardErrorHandling, Persistable, Serializable {

	private String id = null;
	private MarkType markType = MarkType.UNMARKED;
	private BeginFeature begin = null;
	private EndFeature end = null;
	private DisplayableNameFeature displayableName = null;
	private ProfileFeature profile = null;
	private SourceFeature source = null;
	private FormattedNameFeature formattedName = null;
	private NameFeature name = null;
	private NicknameFeature nicknames = null;
	private List<PhotoFeature> photos = null;
	private BirthdayFeature birthday = null;
	private List<AddressFeature> addresses = null;
	private Map<AddressFeature,LabelFeature> addressLabelMap = null;
	private List<TelephoneFeature> telephoneNumbers = null;
	private List<EmailFeature> emailAddresses = null;
	private MailerFeature mailer = null;
	private TimeZoneFeature timeZone = null;
	private GeographicPositionFeature geographicPosition = null;
	private TitleFeature title = null;
	private RoleFeature role = null;
	private List<LogoFeature> logos = null;
	private List<AgentFeature> agents = null;
	private OrganizationFeature organizations = null;
	private CategoriesFeature categories = null;
	private List<NoteFeature> notes = null;
	private ProductIdFeature productId = null;
	private RevisionFeature revision = null;
	private SortStringFeature sortString = null;
	private List<SoundFeature> sounds = null;
	private UIDFeature uid = null;
	private List<URLFeature> urls = null;
	private VersionFeature version = null;
	private ClassFeature securityClass = null;
	private List<KeyFeature> keys = null;
	private List<ExtendedFeature> extendedTypes = null;
	
	private boolean isValid = true;
	private boolean isThrowsExceptions = true;
	private ProblemSeverity problemSeverity = ProblemSeverity.NONE;
	private List<VCardError> errors = null;
	
	/**
	 * <p>Initialize the VCard with Version 3.0
	 * and create all data structures even if they
	 * are left empty.</p>
	 */
	public VCardImpl() {
		version = new VersionType(VCardVersion.V3_0);
		photos = new ArrayList<PhotoFeature>();
		addresses = new ArrayList<AddressFeature>();
		addressLabelMap = new HashMap<AddressFeature,LabelFeature>();
		telephoneNumbers = new ArrayList<TelephoneFeature>();
		emailAddresses = new ArrayList<EmailFeature>();
		logos = new ArrayList<LogoFeature>();
		agents = new ArrayList<AgentFeature>();
		notes = new ArrayList<NoteFeature>();
		sounds = new ArrayList<SoundFeature>();
		urls = new ArrayList<URLFeature>();
		keys = new ArrayList<KeyFeature>();
		extendedTypes = new ArrayList<ExtendedFeature>();
		errors = new ArrayList<VCardError>();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public BeginFeature getBegin()
	{
		return begin;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public EndFeature getEnd()
	{
		return end;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public DisplayableNameFeature getDisplayableNameFeature()
	{
		return displayableName;
	}

	/**
	 * {@inheritDoc}
	 */
	public ProfileFeature getProfile()
	{
		return profile;
	}

	/**
	 * {@inheritDoc}
	 */
	public SourceFeature getSource()
	{
		return source;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasBegin()
	{
		return begin != null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasEnd()
	{
		return end != null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasDisplayableNameFeature()
	{
		return displayableName != null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasProfile()
	{
		return profile != null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasSource()
	{
		return source != null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setBegin(BeginFeature begin) throws NullPointerException {
		if(begin == null) {
			throw new NullPointerException("begin cannot be set to null.");
		}
		
		this.begin = begin;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setEnd(EndFeature end) throws NullPointerException {
		if(end == null) {
			throw new NullPointerException("end cannot be set to null.");
		}
		
		this.end = end;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setDisplayableNameFeature(DisplayableNameFeature name) {
		this.displayableName = name;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setProfile(ProfileFeature profile) {
		this.profile = profile;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setSource(SourceFeature source) {
		this.source = source;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public FormattedNameFeature getFormattedName()
	{
		return formattedName;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setFormattedName(FormattedNameFeature formattedName) throws NullPointerException {
		if(formattedName == null) {
			throw new NullPointerException("formattedName cannot be set to null.");
		}
		
		this.formattedName = formattedName;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public NameFeature getName()
	{
		return name;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setName(NameFeature name) throws NullPointerException {
		if(name == null) {
			throw new NullPointerException("name cannot be set to null.");
		}
		
		this.name = name;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setNicknames(NicknameFeature nicknames) {
		this.nicknames = nicknames;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public NicknameFeature getNicknames()
	{
		return nicknames;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasNicknames()
	{
		return nicknames != null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Iterator<PhotoFeature> getPhotos()
	{
		return photos.iterator();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addPhoto(PhotoFeature photo) throws NullPointerException {
		if(photo == null) {
			throw new NullPointerException("Cannot add a null photo.");
		}
		
		photos.add(photo);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void removePhoto(PhotoFeature photo) throws NullPointerException {
		if(photo == null) {
			throw new NullPointerException("Cannot remove a null photo.");
		}
		
		photos.remove(photo);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean containsPhoto(PhotoFeature photo)
	{
		if(photo == null) {
			return false;
		}
		else {
			return photos.contains(photo);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addAllPhotos(Collection<PhotoFeature> photos) throws NullPointerException {
		if(photos == null) {
			throw new NullPointerException("Cannot add a null collection of photos.");
		}
		
		this.photos.addAll(photos);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void clearPhotos() {
		photos.clear();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasPhotos()
	{
		return !photos.isEmpty();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public BirthdayFeature getBirthDay()
	{
		return birthday;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setBirthday(BirthdayFeature birthday) {
		this.birthday = birthday;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasBirthday()
	{
		return birthday != null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Iterator<AddressFeature> getAddresses()
	{
		return addresses.iterator();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addAddress(AddressFeature address) throws NullPointerException {
		if(address == null) {
			throw new NullPointerException("Cannot add a null address.");
		}
		
		addresses.add(address);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void removeAddress(AddressFeature address) throws NullPointerException {
		if(address == null) {
			throw new NullPointerException("Cannot remove a null address.");
		}
		
		addresses.remove(address);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean containsAddress(AddressFeature address)
	{
		if(address == null) {
			return false;
		}
		else {
			return addresses.contains(address);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addAllAddresses(Collection<AddressFeature> addresses) throws NullPointerException {
		if(addresses == null) {
			throw new NullPointerException("Cannot add a null collection of addresses.");
		}
		
		this.addresses.addAll(addresses);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void clearAddresses() {
		addresses.clear();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasAddresses()
	{
		return !addresses.isEmpty();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Iterator<LabelFeature> getLables()
	{
		return addressLabelMap.values().iterator();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public LabelFeature getLabelFor(AddressFeature address)
	{
		if(address == null) {
			return null;
		}
		else {
			if(addressLabelMap.containsKey(address)) {
				return addressLabelMap.get(address);
			}
			else {
				return null;
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setLabel(LabelFeature label, AddressFeature forAddress) throws NullPointerException, VCardException {
		if(label == null) {
			throw new NullPointerException("Cannot set a null label for an address.");
		}
		
		if(forAddress == null) {
			throw new NullPointerException("Cannot set a label for a null address.");
		}
		
		if(addresses.contains(forAddress)) {
			addressLabelMap.put(forAddress, label);
		}
		else {
			throw new VCardException("Trying to set a label for a non-existing address.");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void removeLabel(LabelFeature label, AddressFeature forAddress) throws NullPointerException, VCardException {
		if(label == null) {
			throw new NullPointerException("Cannot remove a null label from an address.");
		}
		
		if(forAddress == null) {
			throw new NullPointerException("Cannot remove a label from a null address.");
		}
		
		if(addresses.contains(forAddress)) {
			addressLabelMap.remove(forAddress);
		}
		else {
			throw new VCardException("Trying to remove a label for a non-existing address.");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasLabel(AddressFeature address)
	{
		if(address == null) {
			return false;
		}
		else {
			return addressLabelMap.containsKey(address);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void clearLabels() {
		addressLabelMap.clear();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean containsLabel(LabelFeature label)
	{
		if(label == null) {
			return false;
		}
		else {
			return addressLabelMap.containsValue(label);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Iterator<TelephoneFeature> getTelephoneNumbers()
	{
		return telephoneNumbers.iterator();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addTelephoneNumber(TelephoneFeature phoneNumber) throws NullPointerException {
		if(phoneNumber == null) {
			throw new NullPointerException("Cannot add a null phone number.");
		}
		
		telephoneNumbers.add(phoneNumber);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void removeTelephoneNumber(TelephoneFeature phoneNumber) throws NullPointerException {
		if(phoneNumber == null) {
			throw new NullPointerException("Cannot remove a null phone number.");
		}
		
		telephoneNumbers.remove(phoneNumber);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean containsTelephoneNumber(TelephoneFeature phoneNumber)
	{
		if(phoneNumber == null) {
			return false;
		}
		else {
			return telephoneNumbers.contains(phoneNumber);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addAllTelephoneNumber(Collection<TelephoneFeature> phoneNumbers) throws NullPointerException {
		if(phoneNumbers == null) {
			throw new NullPointerException("Cannot add a null collection of phone numbers.");
		}
		
		this.telephoneNumbers.addAll(phoneNumbers);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void clearTelephoneNumbers() {
		telephoneNumbers.clear();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasTelephoneNumbers()
	{
		return !telephoneNumbers.isEmpty();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Iterator<EmailFeature> getEmails()
	{
		return emailAddresses.iterator();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addEmail(EmailFeature email) throws NullPointerException {
		if(email == null) {
			throw new NullPointerException("Cannot add a null email.");
		}
		
		emailAddresses.add(email);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void removeEmail(EmailFeature email) throws NullPointerException {
		if(email == null) {
			throw new NullPointerException("Cannot remove a null email.");
		}
		
		emailAddresses.remove(email);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean containsEmail(EmailFeature email)
	{
		if(email == null) {
			return false;
		}
		else {
			return emailAddresses.contains(email);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addAllEmails(Collection<EmailFeature> emailAddresses) throws NullPointerException {
		if(emailAddresses == null) {
			throw new NullPointerException("Cannot add a null collection of emails.");
		}
		
		this.emailAddresses.addAll(emailAddresses);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void clearEmails() {
		emailAddresses.clear();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasEmails()
	{
		return !emailAddresses.isEmpty();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public MailerFeature getMailer()
	{
		return mailer;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setMailer(MailerFeature mailer) {
		this.mailer = mailer;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasMailer()
	{
		return mailer != null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public TimeZoneFeature getTimeZone()
	{
		return timeZone;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setTimeZone(TimeZoneFeature timeZone) {
		this.timeZone = timeZone;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasTimeZone()
	{
		return timeZone != null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public GeographicPositionFeature getGeographicPosition()
	{
		return geographicPosition;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setGeographicPosition(GeographicPositionFeature geographicPosition) {
		this.geographicPosition = geographicPosition;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasGeographicPosition()
	{
		return geographicPosition != null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public TitleFeature getTitle()
	{
		return title;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setTitle(TitleFeature title) {
		this.title = title;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasTitle()
	{
		return title != null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public RoleFeature getRole()
	{
		return role;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setRole(RoleFeature role) {
		this.role = role;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasRole()
	{
		return role != null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Iterator<LogoFeature> getLogos()
	{
		return logos.iterator();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addLogo(LogoFeature logo) throws NullPointerException {
		if(logo == null) {
			throw new NullPointerException("Cannot add a null logo.");
		}
		
		logos.add(logo);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void removeLogo(LogoFeature logo) throws NullPointerException {
		if(logo == null) {
			throw new NullPointerException("Cannot remove a null logo.");
		}
		
		logos.remove(logo);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean containsLogo(LogoFeature logo)
	{
		if(logo == null) {
			return false;
		}
		else {
			return logos.contains(logo);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addAllLogos(Collection<LogoFeature> logos) throws NullPointerException {
		if(logos == null) {
			throw new NullPointerException("Cannot add a null collection of logos.");
		}
		
		this.logos.addAll(logos);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void clearLogos() {
		logos.clear();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasLogos()
	{
		return !logos.isEmpty();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Iterator<AgentFeature> getAgents()
	{
		return agents.iterator();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addAgent(AgentFeature agent) throws NullPointerException {
		if(agent == null) {
			throw new NullPointerException("Cannot add a null agent.");
		}
		
		agents.add(agent);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void removeAgent(AgentFeature agent) throws NullPointerException {
		if(agent == null) {
			throw new NullPointerException("Cannot remove a null agent.");
		}
		
		agents.remove(agent);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean containsAgent(AgentFeature agent)
	{
		return agents.contains(agent);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addAllAgents(Collection<AgentFeature> agents) throws NullPointerException {
		if(agents == null) {
			throw new NullPointerException("Cannot add a null collection of agents.");
		}
		
		this.agents.addAll(agents);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void clearAgents() {
		agents.clear();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasAgents()
	{
		return !agents.isEmpty();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public OrganizationFeature getOrganizations()
	{
		return organizations;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setOrganizations(OrganizationFeature organizations) throws NullPointerException {
		this.organizations = organizations;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasOrganizations()
	{
		return organizations != null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public CategoriesFeature getCategories()
	{
		return categories;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setCategories(CategoriesFeature categories) {
		this.categories = categories;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasCategories()
	{
		return categories != null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Iterator<NoteFeature> getNotes()
	{
		return notes.iterator();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addNote(NoteFeature note) throws NullPointerException {
		if(note == null) {
			throw new NullPointerException("Cannot add a null note.");
		}
		
		notes.add(note);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void removeNote(NoteFeature note) throws NullPointerException {
		if(note == null) {
			throw new NullPointerException("Cannot remove a null note.");
		}
		
		notes.remove(note);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean containsNote(NoteFeature note)
	{
		return notes.contains(note);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addAllNotes(Collection<NoteFeature> notes) {
		if(notes == null) {
			throw new NullPointerException("Cannot add a null collection of notes.");
		}
		
		this.notes.addAll(notes);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void clearNotes() {
		notes.clear();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasNotes()
	{
		return !notes.isEmpty();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ProductIdFeature getProductId()
	{
		return productId;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setProductId(ProductIdFeature productId) {
		this.productId = productId;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasProductId()
	{
		return productId != null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public RevisionFeature getRevision()
	{
		return revision;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setRevision(RevisionFeature revision) {
		this.revision = revision;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasRevision()
	{
		return revision != null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public SortStringFeature getSortString()
	{
		return sortString;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setSortString(SortStringFeature sortString) {
		this.sortString = sortString;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasSortString()
	{
		return sortString != null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Iterator<SoundFeature> getSounds()
	{
		return sounds.iterator();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addSound(SoundFeature sound) throws NullPointerException {
		if(sound == null) {
			throw new NullPointerException("Cannot add a null sound.");
		}
		
		sounds.add(sound);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void removeSound(SoundFeature sound) throws NullPointerException {
		if(sound == null) {
			throw new NullPointerException("Cannot remove a null sound.");
		}
		
		sounds.remove(sound);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean containsSound(SoundFeature sound)
	{
		if(sound == null) {
			return false;
		}
		else {
			return sounds.contains(sound);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addAllSounds(Collection<SoundFeature> sounds) throws NullPointerException {
		if(sounds == null) {
			throw new NullPointerException("Cannot add a null collection of sounds.");
		}
		
		this.sounds.addAll(sounds);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void clearSounds() {
		sounds.clear();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasSounds()
	{
		return !sounds.isEmpty();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public UIDFeature getUID()
	{
		return uid;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setUID(UIDFeature uid) {
		this.uid = uid;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasUID()
	{
		return uid != null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Iterator<URLFeature> getURLs()
	{
		return urls.iterator();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addURL(URLFeature url) throws NullPointerException {
		if(url == null) {
			throw new NullPointerException("Cannot add a null URL.");
		}
		
		urls.add(url);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void removeURL(URLFeature url) throws NullPointerException {
		if(url == null) {
			throw new NullPointerException("Cannot remove a null URL.");
		}
		
		urls.remove(url);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean containsURL(URLFeature url)
	{
		if(url == null) {
			return false;
		}
		else {
			return urls.contains(url);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addAllURLs(Collection<URLFeature> urls) throws NullPointerException {
		if(urls == null) {
			throw new NullPointerException("Cannot add a null collection of urls.");
		}
		
		this.urls.addAll(urls);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void clearURLs() {
		urls.clear();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasURLs()
	{
		return !urls.isEmpty();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public VersionFeature getVersion()
	{
		return version;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setVersion(VersionFeature version) throws NullPointerException {
		if(version == null) {
			throw new NullPointerException("version cannot be null.");
		}
		
		this.version = version;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ClassFeature getSecurityClass()
	{
		return securityClass;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setSecurityClass(ClassFeature securityClass) {
		this.securityClass = securityClass;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasSecurityClass()
	{
		return securityClass != null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Iterator<KeyFeature> getKeys()
	{
		return keys.iterator();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addKey(KeyFeature key) throws NullPointerException {
		if(key == null) {
			throw new NullPointerException("Cannot add a null key.");
		}
		
		keys.add(key);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void removeKey(KeyFeature key) throws NullPointerException {
		if(key == null) {
			throw new NullPointerException("Cannot remove a null key.");
		}
		
		keys.remove(key);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean containsKey(KeyFeature key)
	{
		if(key == null) {
			return false;
		}
		else {
			return keys.contains(key);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addAllKeys(Collection<KeyFeature> keys) throws NullPointerException {
		if(keys == null) {
			throw new NullPointerException("Cannot add a null collection of keys.");
		}
		
		this.keys.addAll(keys);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void clearKeys() {
		keys.clear();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasKeys()
	{
		return !keys.isEmpty();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Iterator<ExtendedFeature> getExtendedTypes()
	{
		return extendedTypes.iterator();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addExtendedType(ExtendedFeature extension) throws NullPointerException {
		if(extension == null) {
			throw new NullPointerException("Cannot add a null extension.");
		}
		
		extendedTypes.add(extension);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void removeExtendedType(ExtendedFeature extension) throws NullPointerException {
		if(extension == null) {
			throw new NullPointerException("Cannot remove a null extension.");
		}
		
		extendedTypes.remove(extension);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean containsExtendedType(ExtendedFeature extension)
	{
		if(extension == null) {
			return false;
		}
		else {
			return extendedTypes.contains(extension);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addAllExtendedTypes(Collection<ExtendedFeature> extensions) throws NullPointerException {
		if(extensions == null) {
			throw new NullPointerException("Cannot add a null collection of extensions.");
		}
		
		extendedTypes.addAll(extensions);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void clearExtendedTypes() {
		extendedTypes.clear();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasExtendedTypes()
	{
		return !extendedTypes.isEmpty();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void clear() {
		name.clearAdditionalNames();
		name.clearHonorificPrefixes();
		name.clearHonorificSuffixes();
		name.setFamilyName("");
		name.setGivenName("");
		formattedName.setFormattedName("");
		nicknames = null;
		photos.clear();
		birthday = null;
		addresses.clear();
		addressLabelMap.clear();
		telephoneNumbers.clear();
		emailAddresses.clear();
		mailer = null;
		timeZone = null;
		geographicPosition = null;
		title = null;
		role = null;
		logos.clear();
		agents.clear();
		organizations = null;
		categories = null;
		notes.clear();
		productId = null;
		revision = null;
		sortString = null;
		sounds.clear();
		uid = null;
		urls.clear();
		version.setVersion(VCardVersion.V3_0);
		securityClass = null;
		keys.clear();
		extendedTypes.clear();
		clearErrors();
	}
	
	// --------- Error Handling Implementation
	
	/**
	 * {@inheritDoc}
	 */
	public List<VCardError> getErrors()
	{
		return errors;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ProblemSeverity getProblemSeverity()
	{
		return problemSeverity;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isValid()
	{
		return isValid;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setThrowExceptions(boolean isThrowsExceptions) {
		this.isThrowsExceptions = isThrowsExceptions;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isThrowExceptions()
	{
		return isThrowsExceptions;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addError(VCardError error) {
		if(error != null) {
			errors.add(error);
			if(isValid) {
				isValid = false;
			}
			
			switch(error.getSeverity())
			{
				case FATAL:
				{
					switch(problemSeverity)
					{
						case NONE:
						case HINT:
						case WARNING:
						{
							problemSeverity = ProblemSeverity.ERROR;
							break;
						}
					}
					
					break;
				}
					
				case WARNING:
				{
					switch(problemSeverity)
					{
						case NONE:
						case HINT:
						{
							problemSeverity = ProblemSeverity.WARNING;
							break;
						}
					}
					
					break;
				}
					
				case NONE:
				{
					problemSeverity = ProblemSeverity.NONE;
					break;
				}
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addError(String errorMessage, ErrorSeverity severity, Throwable error) {
		if(error != null && severity != null) {
			addError(new VCardError(errorMessage, error, severity));
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void clearErrors() {
		errors.clear();
		problemSeverity = ProblemSeverity.NONE;
		isValid = true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasErrors()
	{
		return !errors.isEmpty();
	}
	
	// --------- Persistence Code
	
	/**
	 * {@inheritDoc}
	 */
	public void setID(String id) {
		this.id = id;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getID()
	{
		return id;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public MarkType getMarkType()
	{
		return markType;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void mark(MarkType markType) {
		this.markType = markType;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void unmark() {
		markType = MarkType.UNMARKED;
	}
	
	// --------- Equality checking and hash code
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj)
	{
		if(obj != null) {
			if(obj instanceof VCardImpl) {
				if(this == obj || ((VCardImpl)obj).hashCode() == this.hashCode()) {
					return true;
				}
				else {
					return false;
				}
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		return Util.generateHashCode(toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName());
		sb.append("[ ");
		
		if(begin != null) {
			sb.append(begin.toString());
			sb.append(",");
		}
		
		if(end != null) {
			sb.append(end.toString());
			sb.append(",");
		}
		
		if(displayableName != null) {
			sb.append(displayableName.toString());
			sb.append(",");
		}
		
		if(profile != null) {
			sb.append(profile.toString());
			sb.append(",");
		}
		
		if(source != null) {
			sb.append(source.toString());
			sb.append(",");
		}
		
		if(formattedName != null) {
			sb.append(formattedName.toString());
			sb.append(",");
		}
		
		if(name != null) {
			sb.append(name.toString());
			sb.append(",");
		}
		
		if(nicknames != null) {
			sb.append(nicknames.toString());
			sb.append(",");
		}
		
		if(!photos.isEmpty()) {
			for(int i = 0; i < photos.size(); i++) {
				sb.append(photos.get(i).toString());
				sb.append(",");
			}
		}
		
		if(birthday != null) {
			sb.append(birthday.toString());
			sb.append(",");
		}
		
		if(!addresses.isEmpty()) {
			for(int i = 0; i < addresses.size(); i++) {
				sb.append(addresses.toString());
				sb.append(",");
			}
		}
		
		if(!addressLabelMap.isEmpty()) {
			Iterator<LabelFeature> iter = addressLabelMap.values().iterator();
			while(iter.hasNext()) {
				sb.append(iter.next().toString());
				sb.append(",");
			}
		}
		
		if(!telephoneNumbers.isEmpty()) {
			for(int i = 0; i < telephoneNumbers.size(); i++) {
				sb.append(telephoneNumbers.get(i).toString());
				sb.append(",");
			}
		}
		
		if(!emailAddresses.isEmpty()) {
			for(int i = 0; i < emailAddresses.size(); i++) {
				sb.append(emailAddresses.get(i).toString());
				sb.append(",");
			}
		}
		
		if(mailer != null) {
			sb.append(mailer.toString());
			sb.append(",");
		}
		
		if(timeZone != null) {
			sb.append(timeZone.toString());
			sb.append(",");
		}
		
		if(geographicPosition != null) {
			sb.append(geographicPosition.toString());
			sb.append(",");
		}
		
		if(title != null) {
			sb.append(title.toString());
			sb.append(",");
		}
		
		if(role != null) {
			sb.append(role.toString());
			sb.append(",");
		}
		
		if(!logos.isEmpty()) {
			for(int i = 0; i < logos.size(); i++) {
				sb.append(logos.get(i).toString());
				sb.append(",");
			}
		}
		
		if(!agents.isEmpty()) {
			for(int i = 0; i < agents.size(); i++) {
				sb.append(agents.get(i).toString());
				sb.append(",");
			}
		}
		
		if(organizations != null) {
			sb.append(organizations.toString());
			sb.append(",");
		}
		
		if(categories != null) {
			sb.append(categories.toString());
			sb.append(",");
		}
		
		if(!notes.isEmpty()) {
			for(int i = 0; i < notes.size(); i++) {
				sb.append(notes.get(i).toString());
				sb.append(",");
			}
		}
		
		if(productId != null) {
			sb.append(productId.toString());
			sb.append(",");
		}
		
		if(revision != null) {
			sb.append(revision.toString());
			sb.append(",");
		}
		
		if(sortString != null) {
			sb.append(sortString.toString());
			sb.append(",");
		}
		
		if(!sounds.isEmpty()) {
			for(int i = 0; i < sounds.size(); i++) {
				sb.append(sounds.get(i).toString());
			}
		}
		
		if(uid != null) {
			sb.append(uid.toString());
			sb.append(",");
		}
		
		if(!urls.isEmpty()) {
			for(int i = 0; i < urls.size(); i++) {
				sb.append(urls.get(i).toString());
				sb.append(",");
			}
		}
		
		if(version != null) {
			sb.append(version.toString());
			sb.append(",");
		}
		
		if(securityClass != null) {
			sb.append(securityClass.toString());
			sb.append(",");
		}
		
		if(!keys.isEmpty()) {
			for(int i = 0; i < keys.size(); i++) {
				sb.append(keys.get(i).toString());
				sb.append(",");
			}
		}
		
		if(!extendedTypes.isEmpty()) {
			for(int i = 0; i < extendedTypes.size(); i++) {
				sb.append(extendedTypes.get(i).toString());
				sb.append(",");
			}
		}
		
		sb.append(problemSeverity.toString());
		sb.append(",");
		
		if(!errors.isEmpty()) {
			for(int i = 0; i < errors.size(); i++) {
				sb.append(errors.get(i).toString());
				sb.append(",");
			}
		}
		
		sb.deleteCharAt(sb.length()-1);	//Remove last comma.
		sb.append(" ]");
		return sb.toString();
	}
	
	@Override
	public VCard clone()
	{
		VCardImpl cloned = new VCardImpl();
		
		if(begin != null) {
			cloned.setBegin(begin.clone());
		}
		
		if(end != null) {
			cloned.setEnd(end.clone());
		}
		
		if(displayableName != null) {
			cloned.setDisplayableNameFeature(displayableName.clone());
		}
		
		if(profile != null) {
			cloned.setProfile(profile.clone());
		}
		
		if(source != null) {
			cloned.setSource(source.clone());
		}
		
		if(formattedName != null) {
			cloned.setFormattedName(formattedName.clone());
		}
		
		if(name != null) {
			cloned.setName(name.clone());
		}
		
		if(hasNicknames()) {
			cloned.setNicknames(nicknames.clone());
		}
		
		if(hasPhotos()) {
			Iterator<PhotoFeature> iter = getPhotos();
			while(iter.hasNext()) {
				PhotoFeature photo = iter.next();
				cloned.addPhoto(photo.clone());
			}
		}
		
		if(hasBirthday()) {
			cloned.setBirthday(birthday.clone());
		}
		
		if(hasAddresses()) {
			Iterator<AddressFeature> iter = getAddresses();
			while(iter.hasNext()) {
				AddressFeature address = iter.next();
				AddressFeature clonedAddress = address.clone(); 
				cloned.addAddress(clonedAddress);
				
				if(hasLabel(address)) {
					LabelFeature label = getLabelFor(address);
					cloned.setLabel(label.clone(), clonedAddress);
				}
			}
		}
		
		if(hasTelephoneNumbers()) {
			Iterator<TelephoneFeature> iter = getTelephoneNumbers();
			while(iter.hasNext()) {
				TelephoneFeature telephone = iter.next();
				cloned.addTelephoneNumber(telephone.clone());
			}
		}
		
		if(hasEmails()) {
			Iterator<EmailFeature> iter = getEmails();
			while(iter.hasNext()) {
				EmailFeature email = iter.next();
				cloned.addEmail(email.clone());
			}
		}
		
		if(hasMailer()) {
			cloned.setMailer(mailer.clone());
		}
		
		if(hasTimeZone()) {
			cloned.setTimeZone(timeZone.clone());
		}
		
		if(hasGeographicPosition()) {
			cloned.setGeographicPosition(geographicPosition.clone());
		}
		
		if(hasTitle()) {
			cloned.setTitle(title.clone());
		}
		
		if(hasRole()) {
			cloned.setRole(role.clone());
		}
		
		if(hasLogos()) {
			Iterator<LogoFeature> iter = getLogos();
			while(iter.hasNext()) {
				LogoFeature logo = iter.next();
				cloned.addLogo(logo.clone());
			}
		}
		
		if(hasAgents()) {
			Iterator<AgentFeature> iter = getAgents();
			while(iter.hasNext()) {
				AgentFeature agent = iter.next();
				cloned.addAgent(agent.clone());
			}
		}
		
		if(hasOrganizations()) {
			cloned.setOrganizations(organizations.clone());
		}
		
		if(hasCategories()) {
			cloned.setCategories(categories.clone());
		}
		
		if(hasNotes()) {
			Iterator<NoteFeature> iter = getNotes();
			while(iter.hasNext()) {
				NoteFeature note = iter.next();
				cloned.addNote(note.clone());
			}
		}
		
		if(hasProductId()) {
			cloned.setProductId(productId.clone());
		}
		
		if(hasRevision()) {
			cloned.setRevision(revision.clone());
		}
		
		if(hasSortString()) {
			cloned.setSortString(sortString.clone());
		}
		
		if(hasSounds()) {
			Iterator<SoundFeature> iter = getSounds();
			while(iter.hasNext()) {
				SoundFeature sound = iter.next();
				cloned.addSound(sound.clone());
			}
		}
		
		if(hasUID()) {
			cloned.setUID(uid.clone());
		}
		
		if(hasURLs()) {
			Iterator<URLFeature> iter = getURLs();
			while(iter.hasNext()) {
				URLFeature url = iter.next();
				cloned.addURL(url.clone());
			}
		}
		
		if(version != null) {
			cloned.setVersion(version.clone());
		}
		
		if(hasSecurityClass()) {
			cloned.setSecurityClass(securityClass.clone());
		}
		
		if(hasKeys()) {
			Iterator<KeyFeature> iter = getKeys();
			while(iter.hasNext()) {
				KeyFeature key = iter.next();
				cloned.addKey(key.clone());
			}
		}
		
		if(hasExtendedTypes()) {
			Iterator<ExtendedFeature> iter = getExtendedTypes();
			while(iter.hasNext()) {
				ExtendedFeature extension = iter.next();
				cloned.addExtendedType(extension.clone());
			}
		}
		
		cloned.setThrowExceptions(isThrowsExceptions);
		if(hasErrors()) {
			List<VCardError> errs = getErrors();
			for(int i = 0; i < errs.size(); i++) {
				VCardError err = errs.get(i);
				cloned.addError(err.clone());
			}
		}
		
		if(id != null) {
			cloned.setID(new String(id));
		}
		
		return cloned;
	}
}
