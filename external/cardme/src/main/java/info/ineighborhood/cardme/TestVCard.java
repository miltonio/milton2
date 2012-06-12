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

package info.ineighborhood.cardme;

import info.ineighborhood.cardme.io.CompatibilityMode;
import info.ineighborhood.cardme.io.FoldingScheme;
import info.ineighborhood.cardme.io.VCardWriter;
import info.ineighborhood.cardme.util.StringUtil;
import info.ineighborhood.cardme.util.Util;
import info.ineighborhood.cardme.vcard.EncodingType;
import info.ineighborhood.cardme.vcard.VCard;
import info.ineighborhood.cardme.vcard.VCardImpl;
import info.ineighborhood.cardme.vcard.VCardVersion;
import info.ineighborhood.cardme.vcard.errors.VCardError;
import info.ineighborhood.cardme.vcard.errors.VCardErrorHandling;
import info.ineighborhood.cardme.vcard.features.AddressFeature;
import info.ineighborhood.cardme.vcard.features.CategoriesFeature;
import info.ineighborhood.cardme.vcard.features.DisplayableNameFeature;
import info.ineighborhood.cardme.vcard.features.EmailFeature;
import info.ineighborhood.cardme.vcard.features.FormattedNameFeature;
import info.ineighborhood.cardme.vcard.features.LabelFeature;
import info.ineighborhood.cardme.vcard.features.LogoFeature;
import info.ineighborhood.cardme.vcard.features.NameFeature;
import info.ineighborhood.cardme.vcard.features.NicknameFeature;
import info.ineighborhood.cardme.vcard.features.NoteFeature;
import info.ineighborhood.cardme.vcard.features.OrganizationFeature;
import info.ineighborhood.cardme.vcard.features.PhotoFeature;
import info.ineighborhood.cardme.vcard.features.ProfileFeature;
import info.ineighborhood.cardme.vcard.features.SoundFeature;
import info.ineighborhood.cardme.vcard.features.SourceFeature;
import info.ineighborhood.cardme.vcard.features.TelephoneFeature;
import info.ineighborhood.cardme.vcard.types.AddressType;
import info.ineighborhood.cardme.vcard.types.BirthdayType;
import info.ineighborhood.cardme.vcard.types.CategoriesType;
import info.ineighborhood.cardme.vcard.types.ClassType;
import info.ineighborhood.cardme.vcard.types.DisplayableNameType;
import info.ineighborhood.cardme.vcard.types.EmailType;
import info.ineighborhood.cardme.vcard.types.ExtendedType;
import info.ineighborhood.cardme.vcard.types.FormattedNameType;
import info.ineighborhood.cardme.vcard.types.GeographicPositionType;
import info.ineighborhood.cardme.vcard.types.LabelType;
import info.ineighborhood.cardme.vcard.types.LogoType;
import info.ineighborhood.cardme.vcard.types.MailerType;
import info.ineighborhood.cardme.vcard.types.NameType;
import info.ineighborhood.cardme.vcard.types.NicknameType;
import info.ineighborhood.cardme.vcard.types.NoteType;
import info.ineighborhood.cardme.vcard.types.OrganizationType;
import info.ineighborhood.cardme.vcard.types.PhotoType;
import info.ineighborhood.cardme.vcard.types.ProductIdType;
import info.ineighborhood.cardme.vcard.types.ProfileType;
import info.ineighborhood.cardme.vcard.types.RevisionType;
import info.ineighborhood.cardme.vcard.types.RoleType;
import info.ineighborhood.cardme.vcard.types.SortStringType;
import info.ineighborhood.cardme.vcard.types.SoundType;
import info.ineighborhood.cardme.vcard.types.SourceType;
import info.ineighborhood.cardme.vcard.types.TelephoneType;
import info.ineighborhood.cardme.vcard.types.TimeZoneType;
import info.ineighborhood.cardme.vcard.types.TitleType;
import info.ineighborhood.cardme.vcard.types.UIDType;
import info.ineighborhood.cardme.vcard.types.URLType;
import info.ineighborhood.cardme.vcard.types.VersionType;
import info.ineighborhood.cardme.vcard.types.media.AudioMediaType;
import info.ineighborhood.cardme.vcard.types.media.ImageMediaType;
import info.ineighborhood.cardme.vcard.types.parameters.AddressParameterType;
import info.ineighborhood.cardme.vcard.types.parameters.EmailParameterType;
import info.ineighborhood.cardme.vcard.types.parameters.LabelParameterType;
import info.ineighborhood.cardme.vcard.types.parameters.ParameterTypeStyle;
import info.ineighborhood.cardme.vcard.types.parameters.TelephoneParameterType;
import java.io.File;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

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
 * Feb 10, 2010
 */
public class TestVCard {

	/**
	 * run this test from the command line
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			testVcard();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private static VCard getFullVCardNoErrors() throws Exception {
		VCard vcard = new VCardImpl();
		vcard.setVersion(new VersionType(VCardVersion.V3_0));
		
		DisplayableNameFeature displayableName = new DisplayableNameType();
		displayableName.setName("VCard for John Doe");
		vcard.setDisplayableNameFeature(displayableName);
		
		ProfileFeature profile = new ProfileType();
		profile.setProfile("VCard");
		vcard.setProfile(profile);
		
		SourceFeature source = new SourceType();
		source.setSource("Whatever");
		vcard.setSource(source);
		
		NameFeature name = new NameType();
		name.setFamilyName("Doe");
		name.setGivenName("John");
		name.addHonorificPrefix("Mr.");
		name.addHonorificSuffix("I");
		name.addAdditionalName("Johny");
		vcard.setName(name);
		
		FormattedNameFeature formattedName = new FormattedNameType();
		formattedName.setFormattedName("John \"Johny\" Doe");
		vcard.setFormattedName(formattedName);
		
		NicknameFeature nicknames = new NicknameType();
		nicknames.addNickname("Johny");
		nicknames.addNickname("JayJay");
		vcard.setNicknames(nicknames);
		
		CategoriesFeature categories = new CategoriesType();
		categories.addCategory("Category 1");
		categories.addCategory("Category 2");
		categories.addCategory("Category 3");
		vcard.setCategories(categories);
		
		vcard.setSecurityClass(new ClassType("Public"));
		vcard.setProductId(new ProductIdType("31e78c0d-fb07-479d-b6af-95a9a3f2916f"));
		vcard.setSortString(new SortStringType("JOHN"));
		
		vcard.setMailer(new MailerType("Mozilla Thunderbird"));
		vcard.setTitle(new TitleType("Generic Accountant"));
		vcard.setRole(new RoleType("Counting Money"));
		
		OrganizationFeature organizations = new OrganizationType();
		organizations.addOrganization("IBM");
		organizations.addOrganization("SUN");
		vcard.setOrganizations(organizations);
		
		vcard.setUID(new UIDType("c0ff639f-9633-4e57-bcfd-55079cfd9d65"));
		vcard.addURL(new URLType(new URL("http://www.sun.com")));
		vcard.setGeographicPosition(new GeographicPositionType(3.4f, -2.6f));

		Calendar birthday = Calendar.getInstance();
		birthday.clear();
		birthday.set(Calendar.YEAR, 1980);
		birthday.set(Calendar.MONTH, 4);
		birthday.set(Calendar.DAY_OF_MONTH, 21);
		vcard.setBirthday(new BirthdayType(birthday));

		vcard.setRevision(new RevisionType(Calendar.getInstance()));
		vcard.setTimeZone(new TimeZoneType(Calendar.getInstance().getTimeZone()));
		
		AddressFeature address1 = new AddressType();
		address1.setExtendedAddress("");
		address1.setCountryName("U.S.A.");
		address1.setLocality("New York");
		address1.setRegion("New York");
		address1.setPostalCode("NYC887");
		address1.setPostOfficeBox("25334");
		address1.setStreetAddress("South cresent drive, Building 5, 3rd floor");
		address1.addAddressParameterType(AddressParameterType.HOME);
		address1.addAddressParameterType(AddressParameterType.PARCEL);
		address1.addAddressParameterType(AddressParameterType.PREF);
		vcard.addAddress(address1);

		LabelFeature labelForAddress1 = new LabelType();
		labelForAddress1.addLabelParameterType(LabelParameterType.HOME);
		labelForAddress1.addLabelParameterType(LabelParameterType.PARCEL);
		labelForAddress1.addLabelParameterType(LabelParameterType.PREF);
		labelForAddress1.setLabel("John Doe\nNew York, NewYork,\nSouth Crecent Drive,\nBuilding 5, floor 3,\nUSA");
		vcard.setLabel(labelForAddress1, address1);
		
		TelephoneFeature telephone = new TelephoneType();
		telephone.setTelephone("+1 (212) 204-34456");
		telephone.addTelephoneParameterType(TelephoneParameterType.CELL);
		telephone.addTelephoneParameterType(TelephoneParameterType.HOME);
		telephone.setParameterTypeStyle(ParameterTypeStyle.PARAMETER_VALUE_LIST);
		vcard.addTelephoneNumber(telephone);
		
		TelephoneFeature telephone2 = new TelephoneType();
		telephone2.setTelephone("00-1-212-555-7777");
		telephone2.addTelephoneParameterType(TelephoneParameterType.FAX);
		telephone2.addTelephoneParameterType(TelephoneParameterType.WORK);
		telephone2.setParameterTypeStyle(ParameterTypeStyle.PARAMETER_LIST);
		vcard.addTelephoneNumber(telephone2);
		
		EmailFeature email = new EmailType();
		email.setEmail("john.doe@ibm.com");
		email.addEmailParameterType(EmailParameterType.IBMMAIL);
		email.addEmailParameterType(EmailParameterType.INTERNET);
		email.addEmailParameterType(EmailParameterType.PREF);
		vcard.addEmail(email);
		vcard.addEmail(new EmailType("billy_bob@gmail.com"));
		
		NoteFeature note = new NoteType();
		note.setNote("THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\"\nAND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE\nIMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE\nARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE\nLIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR\nCONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF\nSUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS\nINTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN\nCONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)\nARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE\nPOSSIBILITY OF SUCH DAMAGE.");
		vcard.addNote(note);
		
		PhotoFeature photo1 = new PhotoType();
		photo1.setCompression(true);
		photo1.setEncodingType(EncodingType.BINARY);
		photo1.setImageMediaType(ImageMediaType.PNG);
		byte[] tuxPicture1 = Util.getFileAsBytes(new File("test/images/smallTux.png"));
		photo1.setPhoto(tuxPicture1);
		vcard.addPhoto(photo1);
		
		LogoFeature logo = new LogoType();
		logo.setCompression(false);
		logo.setEncodingType(EncodingType.BINARY);
		logo.setImageMediaType(ImageMediaType.PNG);
		byte[] tuxPicture2 = Util.getFileAsBytes(new File("test/images/smallTux.png"));
		logo.setLogo(tuxPicture2);
		vcard.addLogo(logo);
		
		SoundFeature sound = new SoundType();
		sound.setCompression(true);
		sound.setEncodingType(EncodingType.BINARY);
		sound.setAudioMediaType(AudioMediaType.OGG);
		sound.setSoundURI(new File("test/images/smallTux.png").toURI());
		vcard.addSound(sound);
		
		vcard.addExtendedType(new ExtendedType("X-GENERATOR", "Cardme Generator"));
		vcard.addExtendedType(new ExtendedType("X-LONG-STRING", "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"));
		
		((VCardErrorHandling)vcard).setThrowExceptions(false);
		
		return vcard;
	}
	
	private static VCard getFullVCardAllErrors() throws Exception {
		VCard vcard = new VCardImpl();
		vcard.setVersion(new VersionType(VCardVersion.V3_0));
		
		NameFeature name = new NameType();
		name.setFamilyName("Doe");
		name.setGivenName("John");
		name.addHonorificPrefix("Mr.");
		name.addHonorificSuffix("I");
		name.addAdditionalName("Johny");
		vcard.setName(name);
		
		FormattedNameFeature formattedName = new FormattedNameType();
		formattedName.setFormattedName(null);
		vcard.setFormattedName(formattedName);
		
		NicknameFeature nicknames = new NicknameType();
		nicknames.addNickname("Johny");
		nicknames.addNickname("JayJay");
		vcard.setNicknames(nicknames);
		
		CategoriesFeature categories = new CategoriesType();
		categories.addCategory("Category 1");
		categories.addCategory("Category 2");
		categories.addCategory("Category 3");
		vcard.setCategories(categories);
		
		vcard.setSecurityClass(new ClassType());
		vcard.setProductId(new ProductIdType());
		vcard.setSortString(new SortStringType());
		
		vcard.setMailer(new MailerType());
		vcard.setTitle(new TitleType());
		vcard.setRole(new RoleType());
		
		OrganizationFeature organizations = new OrganizationType();
		organizations.addOrganization("IBM");
		organizations.addOrganization("SUN");
		vcard.setOrganizations(organizations);
		
		vcard.setUID(new UIDType());
		vcard.addURL(new URLType());
		vcard.setGeographicPosition(new GeographicPositionType());
		vcard.setBirthday(new BirthdayType());

		vcard.setRevision(new RevisionType());
		vcard.setTimeZone(new TimeZoneType());
		
		AddressFeature address1 = new AddressType();
		vcard.addAddress(address1);

		LabelFeature labelForAddress1 = new LabelType();
		vcard.setLabel(labelForAddress1, address1);
		
		TelephoneFeature telephone = new TelephoneType();
		vcard.addTelephoneNumber(telephone);
		
		TelephoneFeature telephone2 = new TelephoneType();
		vcard.addTelephoneNumber(telephone2);
		
		EmailFeature email = new EmailType();
		vcard.addEmail(email);
		
		NoteFeature note = new NoteType();
		vcard.addNote(note);
		
		PhotoFeature photo1 = new PhotoType();
		vcard.addPhoto(photo1);
		
		LogoFeature logo = new LogoType();
		vcard.addLogo(logo);
		
		SoundFeature sound = new SoundType();
		vcard.addSound(sound);
		vcard.addExtendedType(new ExtendedType("X-MISC", "Something"));
		
		((VCardErrorHandling)vcard).setThrowExceptions(false);
		
		return vcard;
	}
	
	/**
	 * test vcard interfaces
	 * @throws Exception 
	 */
	public static void testVcard() throws Exception {
		
		// Create VCard Writer
		VCardWriter vcardWriter = new VCardWriter();
		
		// Set VCard Writer Parameters
		vcardWriter.setOutputVersion(VCardVersion.V3_0);
		vcardWriter.setFoldingScheme(FoldingScheme.MIME_DIR);
		vcardWriter.setCompatibilityMode(CompatibilityMode.RFC2426);
		
		// Get a VCard
		VCard fullNoErrorVCard = getFullVCardNoErrors();
		VCard fullAllErrorVCard = getFullVCardAllErrors();
		
		// Set it on the Writer.
		vcardWriter.setVCard(fullNoErrorVCard);
		
		// Write the VCard
		String vcardString = vcardWriter.buildVCardString();
		
		System.out.println("Full VCard No Error");
		System.out.println("----------------");
		System.out.println(vcardString);
		if(vcardWriter.hasErrors()) {
			System.out.println("Errors\n----------------");
			List<VCardError> errors = ((VCardErrorHandling)vcardWriter.getVCard()).getErrors();
			for(int i = 0; i < errors.size(); i++) {
				System.out.println(errors.get(i).getErrorMessage());
			}
		}
		
		System.out.println("----------------");
		
		
		
		// Write the VCard
		vcardWriter.setVCard(fullAllErrorVCard);
		vcardString = vcardWriter.buildVCardString();
		
		System.out.println("Full VCard With Errors");
		System.out.println("----------------");
		System.out.println(vcardString);
		if(vcardWriter.hasErrors()) {
			System.out.println("Errors\n----------------");
			List<VCardError> errors = ((VCardErrorHandling)vcardWriter.getVCard()).getErrors();
			for(int i = 0; i < errors.size(); i++) {
				System.out.println(StringUtil.formatException(errors.get(i).getError()));
			}
		}
		
		System.out.println("----------------");
	}
}
