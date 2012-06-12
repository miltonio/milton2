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

package info.ineighborhood.cardme.engine;

import info.ineighborhood.cardme.io.CompatibilityMode;
import info.ineighborhood.cardme.util.Base64Wrapper;
import info.ineighborhood.cardme.util.ISOUtils;
import info.ineighborhood.cardme.util.VCardUtils;
import info.ineighborhood.cardme.vcard.EncodingType;
import info.ineighborhood.cardme.vcard.VCard;
import info.ineighborhood.cardme.vcard.VCardImpl;
import info.ineighborhood.cardme.vcard.VCardType;
import info.ineighborhood.cardme.vcard.VCardVersion;
import info.ineighborhood.cardme.vcard.errors.ErrorSeverity;
import info.ineighborhood.cardme.vcard.errors.VCardBuildException;
import info.ineighborhood.cardme.vcard.errors.VCardError;
import info.ineighborhood.cardme.vcard.errors.VCardException;
import info.ineighborhood.cardme.vcard.features.AddressFeature;
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
import info.ineighborhood.cardme.vcard.types.AddressType;
import info.ineighborhood.cardme.vcard.types.BeginType;
import info.ineighborhood.cardme.vcard.types.BirthdayType;
import info.ineighborhood.cardme.vcard.types.CategoriesType;
import info.ineighborhood.cardme.vcard.types.ClassType;
import info.ineighborhood.cardme.vcard.types.DisplayableNameType;
import info.ineighborhood.cardme.vcard.types.EmailType;
import info.ineighborhood.cardme.vcard.types.EndType;
import info.ineighborhood.cardme.vcard.types.ExtendedType;
import info.ineighborhood.cardme.vcard.types.FormattedNameType;
import info.ineighborhood.cardme.vcard.types.GeographicPositionType;
import info.ineighborhood.cardme.vcard.types.KeyType;
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
import info.ineighborhood.cardme.vcard.types.media.KeyTextType;
import info.ineighborhood.cardme.vcard.types.parameters.AddressParameterType;
import info.ineighborhood.cardme.vcard.types.parameters.EmailParameterType;
import info.ineighborhood.cardme.vcard.types.parameters.KeyParameterType;
import info.ineighborhood.cardme.vcard.types.parameters.LabelParameterType;
import info.ineighborhood.cardme.vcard.types.parameters.LogoParameterType;
import info.ineighborhood.cardme.vcard.types.parameters.PhotoParameterType;
import info.ineighborhood.cardme.vcard.types.parameters.SoundParameterType;
import info.ineighborhood.cardme.vcard.types.parameters.TelephoneParameterType;
import info.ineighborhood.cardme.vcard.types.parameters.XAddressParameterType;
import info.ineighborhood.cardme.vcard.types.parameters.XEmailParameterType;
import info.ineighborhood.cardme.vcard.types.parameters.XLabelParameterType;
import info.ineighborhood.cardme.vcard.types.parameters.XTelephoneParameterType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

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
 * May 6, 2009 - 11:29:02 AM
 * 
 */
public class VCardEngine {

	/**
	 * <p>Selects from a list of application compatibility modes
	 * to use when formatting the output of the vcard. Some applications
	 * expect a certain type of formatting or non-standard types.</p>
	 */
	private CompatibilityMode compatMode = null;
	
	/**
	 * <p>Create a VCard parsing engine and initialize it to
	 * use RFC2426 compatibility mode by default.</p>
	 */
	public VCardEngine() {
		compatMode = CompatibilityMode.RFC2426;
	}
	
	/**
	 * <p>Create a VCard parsing engine with a user
	 * specified compatibility mode.</p>
	 * 
	 * @param compatMode
	 */
	public VCardEngine(CompatibilityMode compatMode) {
		this.compatMode = compatMode;
	}
	
	/**
	 * <p>Sets a specified compatibility mode.</p>
	 *
	 * @see CompatibilityMode
	 * @param compatMode
	 */
	public void setCompatibilityMode(CompatibilityMode compatMode) {
		if(compatMode == null) {
			this.compatMode = CompatibilityMode.RFC2426;
		}
		else {
			this.compatMode = compatMode;
		}
	}
	
	/**
	 * <p>Returns the currently set compatibility mode.
	 * Null if not set.</p>
	 *
	 * @return {@link CompatibilityMode}
	 */
	public CompatibilityMode getCompatibilityMode()
	{
		return compatMode;
	}
	
	/**
	 * <p>Parses the specified VCard file by retrieving the contents
	 * of the file, unfolding it and then parsing it. The returned result
	 * is a VCard java object.</p>
	 *
	 * @param vcardFile
	 * @return {@link VCard}
	 * @throws IOException
	 */
	public VCard parse(File vcardFile) throws IOException
	{
		String vcardStr = getContentFromFile(vcardFile);
		String unfoldedVcardStr = VCardUtils.unfoldVCard(vcardStr);
		return parseVCard(unfoldedVcardStr);
	}
	
	/**
	 * <p>Parses the specified VCard String by retrieving the contents
	 * of the file, unfolding it and then parsing it. The returned result
	 * is a VCard java object.</p>
	 *
	 * @param vcardString
	 * @return {@link VCard}
	 * @throws IOException
	 */
	public VCard parse(String vcardString) throws IOException
	{
		String vcardStr = getContentFromString(vcardString);
		String unfoldedVcardStr = VCardUtils.unfoldVCard(vcardStr);
		return parseVCard(unfoldedVcardStr);
	}
	
	/**
	 * <p>Parses the specified VCard String and returns a VCard java object
	 * with {@link VCardImpl}.setThrowExceptions() set to false. This method
	 * assumes the following:
	 * <ol>
	 * 	<li>The String has only \n end of line markers instead of \r\n</li>
	 * 	<li>All lines in the String are unfolded</li>
	 * </ol>
	 * </p>
	 *
	 * @param vcardStr
	 * @return {@link VCard}
	 */
	private VCard parseVCard(String vcardStr)
	{
		VCardImpl vcard = new VCardImpl();
		vcard.setThrowExceptions(false);
		
		List<String[]> arrayLines = splitLines(vcardStr);
		String[] vLine = null;
		for (int i = 0; i < arrayLines.size(); i++) {
			vLine = arrayLines.get(i);
			String type = vLine[0].trim();	//VCard Type
			String value;
			if( vLine.length > 1) {
				value = vLine[1].trim();	//VCard Value
			} else {
				value = null;
			}
			String paramTypes = null;
			String group = null;
			
			if(type.indexOf('.') != -1) {
				group = type.substring(0, type.indexOf('.'));
				type = type.substring(type.indexOf('.')+1);
			}
			
			if (type.indexOf(';') != -1) {
				paramTypes = type.substring(type.indexOf(';')+1).trim().toUpperCase();
				type = type.substring(0, type.indexOf(';')).trim();
			}
			
			try {
				parseLine(group, type.toUpperCase(), paramTypes, value, vcard);
			}
			catch(VCardBuildException vbe) {
				if(vcard.isThrowExceptions()) {
					throw new VCardException(vbe.getMessage(), vbe);
				}
				else {
					handleError(vcard, vbe.getMessage(), vbe, ErrorSeverity.WARNING);
				}
			}
			
		}

		return vcard;
	}
	
	/**
	 * <p>Parses a specific line of text from the VCard. This line is partitioned into
	 * the following segments.
	 * <ul>
	 * 	<li><b>group</b> Optional grouping name for the type.</li>
	 * 	<li><b>type</b> The vcard type that identifies this line.</li>
	 * 	<li><b>paramTypes</b> Any parameters that follow the type.</li>
	 * 	<li><b>value</b> The actual value or data of the type.</li>
	 * 	<li><b>vcard</b> The vcard object to append the type to once parsed.</li>
	 * </ul>
	 * </p>
	 *
	 * @param group
	 * @param type
	 * @param paramTypes
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseLine(String group, String type, String paramTypes, String value, VCardImpl vcard) throws VCardBuildException {
		
		VCardType vCardType = null;
		
		//Extended Types are a bit special since they only start with X- and end with anything.
		if(type.startsWith("X-")) {
			vCardType = VCardType.XTENDED;
		}
		else {
			try {
				//Enums do not like hyphens so replace it with an underscore.
				type = type.replaceAll("-", "_");
				vCardType = VCardType.valueOf(type);
			}
			catch(IllegalArgumentException iae) {
				if(vcard.isThrowExceptions()) {
					throw new VCardException(iae.getMessage(), iae);
				}
				else {
					handleError(vcard, "Unrecognizable type name \""+type+"\"", iae, ErrorSeverity.WARNING);
					return;
				}
			}
		}
		
		switch (vCardType)
		{
			case BEGIN:
			{
				parseBeginType(group, value, vcard);
				break;
			}
			
			case END:
			{
				parseEndType(group, value, vcard);
				break;
			}
			
			case VERSION:
			{
				parseVersionType(group, value, vcard);
				break;
			}
			
			case FN:
			{
				parseFnType(group, value, vcard);
				break;
			}
			
			case N:
			{
				parseNType(group, value, vcard);
				break;
			}
			
			case NICKNAME:
			{
				parseNicknameType(group, value, vcard);
				break;
			}
			
			case PHOTO:
			{
				parsePhotoType(group, value, paramTypes, vcard);
				break;
			}
			
			case BDAY:
			{
				parseBDayType(group, value, vcard);
				break;
			}
			
			case ADR:
			{
				parseAdrType(group, value, paramTypes, vcard);
				break;
			}
			
			case LABEL:
			{
				parseLabelType(group, value, paramTypes, vcard);
				break;
			}
			
			case TEL:
			{
				parseTelType(group, value, paramTypes, vcard);
				break;
			}
			
			case EMAIL:
			{
				parseEmailType(group, value, paramTypes, vcard);
				break;
			}
			
			case MAILER:
			{
				parseMailerType(group, value, vcard);
				break;
			}
			
			case TZ:
			{
				parseTzType(group, value, paramTypes, vcard);
				break;
			}
			
			case GEO:
			{
				parseGeoType(group, value, vcard);
				break;
			}
			
			case TITLE:
			{
				parseTitleType(group, value, vcard);
				break;
			}
			
			case ROLE:
			{
				parseRoleType(group, value, vcard);
				break;
			}
			
			case LOGO:
			{
				parseLogoType(group, value, paramTypes, vcard);
				break;
			}
			
			case AGENT:
			{
//TODO				parseAgentType(group, value, vcard);
				break;
			}
			
			case ORG:
			{
				parseOrgType(group, value, vcard);
				break;
			}
			
			case CATEGORIES:
			{
				parseCategoriesType(group, value, vcard);
				break;
			}
			
			case NOTE:
			{
				parseNoteType(group, value, vcard);
				break;
			}
			
			case PRODID:
			{
				parseProdidType(group, value, vcard);
				break;
			}
			
			case REV:
			{
				parseRevType(group, value, vcard);
				break;
			}
			
			case SORT_STRING:
			{
				parseSortStringType(group, value, vcard);
				break;
			}
			
			case SOUND:
			{
				parseSoundType(group, value, paramTypes, vcard);
				break;
			}
			
			case UID:
			{
				parseUidType(group, value, vcard);
				break;
			}
			
			case URL:
			{
				parseUrlType(group, value, vcard);
				break;
			}
			
			case CLASS:
			{
				parseClassType(group, value, vcard);
				break;
			}
			
			case KEY:
			{
				parseKeyType(group, value, paramTypes, vcard);
				break;
			}
			
			case XTENDED:
			{
				parseXtendedType(group, value, type, vcard);
				break;
			}
			
			case NAME:
			{
				parseDisplayableNameType(group, value, vcard);
				break;
			}
			
			case PROFILE:
			{
				parseProfileType(group, value, vcard);
				break;
			}
			
			case SOURCE:
			{
				parseSourceType(group, value, vcard);
				break;
			}

			default:
			{
				throw new VCardBuildException("Unhandled VCard type \""+vCardType.getType()+"\"");
			}
		}
	}
	
	/**
	 * <p>Parses the BEGIN type.</p>
	 * 
	 * @param group
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseBeginType(String group, String value, VCardImpl vcard) throws VCardBuildException {
		try {
			BeginFeature beginFeature = new BeginType();
			if(value.compareToIgnoreCase("VCARD") == 0) {
				if(group != null) {
					beginFeature.setGroup(group);
				}
				
				vcard.setBegin(beginFeature);
			}
			else {
				throw new VCardBuildException("Invalid value for \"BEGIN\" type. Must be \"VCARD\"");
			}
		}
		catch(Exception ex) {
			throw new VCardBuildException("BeginType ("+VCardType.BEGIN.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the END type.</p>
	 *
	 * @param group
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseEndType(String group, String value, VCardImpl vcard) throws VCardBuildException {
		try {
			EndFeature endFeature = new EndType();
			if (value.compareToIgnoreCase("VCARD") == 0) {
				if(group != null) {
					endFeature.setGroup(group);
				}
				
				vcard.setEnd(endFeature);
			}
			else {
				throw new VCardException("Invalid value for \"END\" type. Must be \"VCARD\"");
			}
		}
		catch(Exception ex) {
			throw new VCardBuildException("EndType ("+VCardType.END.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the VERSION type.</p>
	 *
	 * @param group
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseVersionType(String group, String value, VCardImpl vcard) throws VCardBuildException {
		try {
			VersionFeature versionFeature = new VersionType();
			if(value.compareTo(VCardVersion.V3_0.getVersion()) == 0) {
				versionFeature.setVersion(VCardVersion.V3_0);
			}
			else if(value.compareTo(VCardVersion.V2_1.getVersion()) == 0) {
				versionFeature.setVersion(VCardVersion.V2_1);
			}
			else {
				throw new VCardException("Invalid value for \"VERSION\" type. Must be [3.0, 2.1]");
			}
			
			if(group != null) {
				versionFeature.setGroup(group);
			}
			
			vcard.setVersion(versionFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("VersionType ("+VCardType.VERSION.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the FN type.</p>
	 *
	 * @param group
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseFnType(String group, String value, VCardImpl vcard) throws VCardBuildException {
		try {
			FormattedNameFeature formattedNameFeature = new FormattedNameType();
			if(VCardUtils.needsUnEscaping(value)) {
				formattedNameFeature.setFormattedName(VCardUtils.unescapeString(value));
			}
			else {
				formattedNameFeature.setFormattedName(value);
			}
			
			if(group != null) {
				formattedNameFeature.setGroup(group);
			}
			
			vcard.setFormattedName(formattedNameFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("FormattedNameType ("+VCardType.FN.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the N type.</p>
	 *
	 * @param group
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseNType(String group, String value, VCardImpl vcard) throws VCardBuildException {
		try {
			NameFeature nameFeature = new NameType();
			String[] names = value.split(";",5);
			
			if(names[0] != null) {
				if(VCardUtils.needsUnEscaping(names[0])) {
					nameFeature.setFamilyName(VCardUtils.unescapeString(names[0]));					
				}
				else {
					nameFeature.setFamilyName(names[0]);
				}
			}
			else {
				throw new VCardBuildException("NameType ("+VCardType.FN.getType()+") The family name field cannot be left empty.");
			}
			
			if(names[1] != null) {
				if(VCardUtils.needsUnEscaping(names[1])) {
					nameFeature.setGivenName(VCardUtils.unescapeString(names[1]));
				}
				else {
					nameFeature.setGivenName(names[1]);
				}
			}
			else {
				throw new VCardBuildException("NameType ("+VCardType.FN.getType()+") The given name field cannot be left empty.");
			}
			
			if(names[2] != null) {
				String[] addNames = names[2].split(",");
				for(int i = 0; i < addNames.length; i++) {
					if(VCardUtils.needsUnEscaping(addNames[i])) {
						nameFeature.addAdditionalName(VCardUtils.unescapeString(addNames[i]));
					}
					else {
						nameFeature.addAdditionalName(addNames[i]);
					}
				}
			}
			
			if(names[3] != null) {
				String[] prefixes = names[3].split(",");
				for(int i = 0; i < prefixes.length; i++) {
					if(VCardUtils.needsUnEscaping(prefixes[i])) {
						nameFeature.addHonorificPrefix(VCardUtils.unescapeString(prefixes[i]));
					}
					else {
						nameFeature.addHonorificPrefix(prefixes[i]);
					}
				}
			}
			
			if(names[4] != null) {
				String[] suffixes = names[4].split(",");
				for(int i = 0; i < suffixes.length; i++) {
					if(VCardUtils.needsUnEscaping(suffixes[i])) {
						nameFeature.addHonorificSuffix(VCardUtils.unescapeString(suffixes[i]));
					}
					else {
						nameFeature.addHonorificSuffix(suffixes[i]);
					}
				}
			}
			
			if(group != null) {
				nameFeature.setGroup(group);
			}
			
			vcard.setName(nameFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("NameType ("+VCardType.N.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the Nickname type.</p>
	 *
	 * @param group
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseNicknameType(String group, String value, VCardImpl vcard) throws VCardBuildException {
		try {
			NicknameFeature nicknameFeature = new NicknameType();
			String[] nicknames = value.split(",");
			for(int i = 0; i < nicknames.length; i++) {
				if(VCardUtils.needsUnEscaping(nicknames[i])) {
					nicknameFeature.addNickname(VCardUtils.unescapeString(nicknames[i]));
				}
				else {
					nicknameFeature.addNickname(nicknames[i]);
				}
			}
			
			if(group != null) {
				nicknameFeature.setGroup(group);
			}
			
			vcard.setNicknames(nicknameFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("NicknameType ("+VCardType.NICKNAME.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the PHOTO type.</p>
	 *
	 * @param group
	 * @param value
	 * @param paramTypes
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parsePhotoType(String group, String value, String paramTypes, VCardImpl vcard) throws VCardBuildException {
		try {
			PhotoFeature photoFeature = new PhotoType();
			String[] params = paramTypes.split(";");
			boolean isBinary = false;
			
			//Parse the parameter types
			for(int i = 0; i < params.length; i++) {
				String[] paramType = null;
				
				switch(getCompatibilityMode())
				{
					case I_PHONE:
					case MAC_ADDRESS_BOOK:
					{
						if(params[i].contains("=")) {
							//For proper vcard parameter types
							paramType = params[i].trim().split("=");
						}
						else {
							//When the parameter types are missing we try to guess what they are.
							//We really should not as it breaks RFC rules but some apps do broken exports.
							
							if(params[i].equals(EncodingType.BASE64.getType())) {
								paramType = new String[] {PhotoParameterType.ENCODING.getTypeName(), params[i]};
							}
							else if(params[i].equals(EncodingType.BINARY.getType())) {
								paramType = new String[] {PhotoParameterType.ENCODING.getTypeName(), params[i]};
							}
							else if(params[i].equals("URI")) {
								paramType = new String[] {PhotoParameterType.VALUE.getTypeName(), params[i]};
							}
							else {
								//If we are here then we can only assume this is
								//a IANA registered image type (or custom type). 
								paramType = new String[] {PhotoParameterType.TYPE.getTypeName(), params[i]};
							}
						}
						
						break;
					}
					
					case EVOLUTION:
					case KDE_ADDRESS_BOOK:
					case MS_OUTLOOK:
					case RFC2426:
					default:
					{
						if(params[i].contains("=")) {
							paramType = params[i].trim().split("=");
						}
						else {
							/*
							 * Type special notes: The type can include the type parameter "TYPE" to
							 * specify the graphic image format type. The TYPE parameter values MUST
							 * be one of the IANA registered image formats or a non-standard image format.
							 */
							
							paramType = new String[] {PhotoParameterType.TYPE.getTypeName(), params[i]};
						}
						
						break;
					}
				}
				
				/*
				 * ENCODING=b would look like
				 * paramType[0] = ENCODING
				 * paramType[1] = b
				 */
				
				PhotoParameterType photoParamType = PhotoParameterType.valueOf(paramType[0]);
				switch(photoParamType)
				{
					case ENCODING:
					{
						if(paramType[1].compareToIgnoreCase(EncodingType.BINARY.getType()) == 0) {
							photoFeature.setEncodingType(EncodingType.BINARY);
							isBinary = true;
						}
						else if(paramType[1].compareToIgnoreCase(EncodingType.BASE64.getType()) == 0) {
							photoFeature.setEncodingType(EncodingType.BINARY);
							isBinary = true;
						}
						else {
							throw new VCardBuildException("PhotoType ("+VCardType.PHOTO.getType()+") Invalid encoding type \""+paramType[1]+"\"");
						}
						
						break;
					}
					
					case TYPE:
					{
						ImageMediaType mediaType = null;
						try {
							mediaType = ImageMediaType.valueOf(paramType[1]);
							photoFeature.setImageMediaType(mediaType);
						}
						catch(IllegalArgumentException iae) {
							mediaType = ImageMediaType.NON_STANDARD;
							mediaType.setTypeName(paramType[1].trim());
							mediaType.setIanaRegisteredName(paramType[1].trim());
							mediaType.setExtension(paramType[1].trim());
						}
						finally {
							photoFeature.setImageMediaType(mediaType);
						}
						
						break;
					}
					
					case VALUE:
					{
						if(paramType[1].compareToIgnoreCase("URI") == 0) {
							photoFeature.setEncodingType(EncodingType.EIGHT_BIT);
							isBinary = false;
						}
						else {
							throw new VCardBuildException("PhotoType ("+VCardType.PHOTO.getType()+") Invalid value type \""+paramType[1]+"\"");
						}
						
						break;
					}
				}
			}
			
			if(isBinary) {
				byte[] photoBytes = Base64Wrapper.decode(value);
				photoFeature.setCompression(true);
				photoFeature.setPhoto(photoBytes);
			}
			else {
				URI photoUri = new URI(value);
				photoFeature.setPhotoURI(photoUri);
			}
			
			if(group != null) {
				photoFeature.setGroup(group);
			}
			
			vcard.addPhoto(photoFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("PhotoType ("+VCardType.PHOTO.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the BDAY type.</p>
	 *
	 * @param group
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseBDayType(String group, String value, VCardImpl vcard) throws VCardBuildException {
		try {
			BirthdayFeature birthdayFeature = new BirthdayType();
			if(value.matches(ISOUtils.ISO8601_DATE_EXTENDED_REGEX)) {
				//Example: 1996-04-15
				String[] date = value.split("-");
				Calendar cal = Calendar.getInstance();
				cal.clear();
				cal.set(Calendar.YEAR, Integer.parseInt(date[0]));
				cal.set(Calendar.MONTH, Integer.parseInt(date[1])-1);
				cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[2]));
				birthdayFeature.setBirthday(cal);
			}
			else if(value.matches(ISOUtils.ISO8601_DATE_BASIC_REGEX)) {
				//Example: 19960415
				String year = value.substring(0, 4);
				String month = value.substring(4,6);
				String day = value.substring(6);
				
				Calendar cal = Calendar.getInstance();
				cal.clear();
				cal.set(Calendar.YEAR, Integer.parseInt(year));
				cal.set(Calendar.MONTH, Integer.parseInt(month)-1);
				cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
				birthdayFeature.setBirthday(cal);
			}
			else if(value.matches(ISOUtils.ISO8601_UTC_TIME_BASIC_REGEX)) {
				//Example: 19960415T231000Z
				String year = value.substring(0, 4);
				String month = value.substring(4, 6);
				String day = value.substring(6, 8);
				String hour = value.substring(9, 11);
				String minute = value.substring(11, 13);
				String seconds = value.substring(13, 15);
				
				Calendar cal = Calendar.getInstance();
				cal.clear();
				cal.set(Calendar.YEAR, Integer.parseInt(year));
				cal.set(Calendar.MONTH, Integer.parseInt(month)-1);
				cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
				cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
				cal.set(Calendar.MINUTE, Integer.parseInt(minute));
				cal.set(Calendar.SECOND, Integer.parseInt(seconds));
				birthdayFeature.setBirthday(cal);
			}
			else if(value.matches(ISOUtils.ISO8601_UTC_TIME_EXTENDED_REGEX)) {
				//Example: 1996-04-15T23:10:00Z
				String[] split = value.toUpperCase().substring(0, value.indexOf('Z')).split("T");
				String[] date = split[0].split("-");
				String[] time = split[1].split(":");
				
				Calendar cal = Calendar.getInstance();
				cal.clear();
				cal.set(Calendar.YEAR, Integer.parseInt(date[0]));
				cal.set(Calendar.MONTH, Integer.parseInt(date[1])-1);
				cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[2]));
				cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
				cal.set(Calendar.MINUTE, Integer.parseInt(time[1]));
				cal.set(Calendar.SECOND, Integer.parseInt(time[2]));
				birthdayFeature.setBirthday(cal);
			}
			else if(value.matches(ISOUtils.ISO8601_TIME_EXTENDED_REGEX)) {
				//Example: 1996-04-15T23:10:00-06:00
				String[] split = value.toUpperCase().split("T");
				String[] date = split[0].split("-");
				String time = split[1];
				
				//23:10:00-06:00
				String hour = time.substring(0, 2);
				String minute = time.substring(3, 5);
				String seconds = time.substring(6, 8);
				String operator = time.substring(8, 9);
				String offsHour = time.substring(9, 11);
				String offsMinute = time.substring(12);
				
				
				Calendar cal = Calendar.getInstance();
				cal.clear();
				cal.set(Calendar.YEAR, Integer.parseInt(date[0]));
				cal.set(Calendar.MONTH, Integer.parseInt(date[1])-1);
				cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[2]));
				cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
				cal.set(Calendar.MINUTE, Integer.parseInt(minute));
				cal.set(Calendar.SECOND, Integer.parseInt(seconds));
				
				if(operator.compareTo("-") == 0) {
					offsHour = "-"+offsHour;
				}
				
				int offsetMillis = Integer.parseInt(offsHour) + (Integer.parseInt(offsMinute) / 10);
				offsetMillis = (((offsetMillis * 60) * 60) * 1000);
				
				cal.set(Calendar.ZONE_OFFSET, offsetMillis);
				birthdayFeature.setBirthday(cal);
			}
			else {
				throw new VCardBuildException("BirthdayType ("+VCardType.BDAY.getType()+") Birthday value is not a valid ISO-8601 text.");
			}
			
			if(group != null) {
				birthdayFeature.setGroup(group);
			}
			
			vcard.setBirthday(birthdayFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("BirthdayType ("+VCardType.BDAY.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the ADR type.</p>
	 *
	 * @param group
	 * @param value
	 * @param paramTypes
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseAdrType(String group, String value, String paramTypes, VCardImpl vcard) throws VCardBuildException {
		try {
			AddressFeature addressFeature = new AddressType();
			if(paramTypes != null) {
				if(paramTypes.indexOf(';') != -1) {
					//Parameter List Style
					//Example: TYPE=home;TYPE=parcel;TYPE=postal;TYPE=pref
					
					String[] list = paramTypes.split(";");
					for(int i = 0; i < list.length; i++) {
						String paramType = list[i];
						
						//Only do this to remove the TYPE prefix on certain occasions
						String paramValue = null;
						if(paramType.toUpperCase().contains("TYPE")) {
							paramValue = paramType.substring(paramType.indexOf('=')+1);
						}
						else {
							paramValue = paramType;
						}
						
						try {
							AddressParameterType addrParamType = AddressParameterType.valueOf(paramValue);
							addressFeature.addAddressParameterType(addrParamType);
						}
						catch(IllegalArgumentException iae) {
							XAddressParameterType xAddrType = null;
							if(paramValue.indexOf('=') != -1) {
								String[] pTmp = paramValue.split("=");
								xAddrType = new XAddressParameterType(pTmp[0], pTmp[1]);
								pTmp[0] = null;
								pTmp[1] = null;
							}
							else {
								xAddrType = new XAddressParameterType(paramValue);
							}
							
							addressFeature.addExtendedAddressParameterType(xAddrType);
						}
					}
				}
				else {
					String[] list = paramTypes.substring(paramTypes.indexOf('=')+1).split(",");
					for(int i = 0; i < list.length; i++) {
						String paramValue = list[i];
						
						try {
							AddressParameterType addrParamType = AddressParameterType.valueOf(paramValue);
							addressFeature.addAddressParameterType(addrParamType);
						}
						catch(IllegalArgumentException iae) {
							XAddressParameterType xAddrType = null;
							if(paramValue.indexOf('=') != -1) {
								String[] pTmp = paramValue.split("=");
								xAddrType = new XAddressParameterType(pTmp[0], pTmp[1]);
								pTmp[0] = null;
								pTmp[1] = null;
							}
							else {
								xAddrType = new XAddressParameterType(paramValue);
							}
							
							addressFeature.addExtendedAddressParameterType(xAddrType);
						}
					}
				}
			}
			
			String[] address = value.split(";",7);
			String postOfficeBox = address[0];
			String extendedAddress = address[1];
			String streetAddress = address[2];
			String locality = address[3];
			String region = address[4];
			String postalCode = address[5];
			String countryName = address[6];
			
			if(postOfficeBox != null) {
				if(VCardUtils.needsUnEscaping(postOfficeBox)) {
					addressFeature.setPostOfficeBox(VCardUtils.unescapeString(postOfficeBox));
				}
				else {
					addressFeature.setPostOfficeBox(postOfficeBox);
				}
			}
			
			if(extendedAddress != null) {
				if(VCardUtils.needsUnEscaping(extendedAddress)) {
					addressFeature.setExtendedAddress(VCardUtils.unescapeString(extendedAddress));
				}
				else {
					addressFeature.setExtendedAddress(extendedAddress);
				}
			}
			
			if(streetAddress != null) {
				if(VCardUtils.needsUnEscaping(streetAddress)) {
					addressFeature.setStreetAddress(VCardUtils.unescapeString(streetAddress));
				}
				else {
					addressFeature.setStreetAddress(streetAddress);
				}
			}
			
			if(locality != null) {
				if(VCardUtils.needsUnEscaping(locality)) {
					addressFeature.setLocality(VCardUtils.unescapeString(locality));
				}
				else {
					addressFeature.setLocality(locality);
				}
			}
			
			if(region != null) {
				if(VCardUtils.needsUnEscaping(region)) {
					addressFeature.setRegion(VCardUtils.unescapeString(region));
				}
				else {
					addressFeature.setRegion(region);
				}
			}
			
			if(postalCode != null) {
				if(VCardUtils.needsUnEscaping(postalCode)) {
					addressFeature.setPostalCode(VCardUtils.unescapeString(postalCode));
				}
				else {
					addressFeature.setPostalCode(postalCode);
				}
			}
			
			if(countryName != null) {
				if(VCardUtils.needsUnEscaping(countryName)) {
					addressFeature.setCountryName(VCardUtils.unescapeString(countryName));
				}
				else {
					addressFeature.setCountryName(countryName);
				}
			}
			
			if(group != null) {
				addressFeature.setGroup(group);
			}
			
			vcard.addAddress(addressFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("AddressType ("+VCardType.ADR.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the LABEL type.</p>
	 *
	 * @param group
	 * @param value
	 * @param paramTypes
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseLabelType(String group, String value, String paramTypes, VCardImpl vcard) throws VCardBuildException {
		try {
			LabelFeature labelFeature = new LabelType();
			if(paramTypes != null) {
				if(paramTypes.indexOf(';') != -1) {
					//Parameter List Style
					//Example: TYPE=home;TYPE=parcel;TYPE=postal;TYPE=pref
					String[] list = paramTypes.split(";");
					for(int i = 0; i < list.length; i++) {
						String paramType = list[i];
						
						//Only do this to remove the TYPE prefix on certain occasions
						String paramValue = null;
						if(paramType.toUpperCase().contains("TYPE")) {
							paramValue = paramType.substring(paramType.indexOf('=')+1);
						}
						else {
							paramValue = paramType;
						}
						
						try {
							LabelParameterType labelParamType = LabelParameterType.valueOf(paramValue);
							labelFeature.addLabelParameterType(labelParamType);
						}
						catch(IllegalArgumentException iae) {
							XLabelParameterType xLabelType = null;
							if(paramValue.indexOf('=') != -1) {
								String[] pTmp = paramValue.split("=");
								xLabelType = new XLabelParameterType(pTmp[0], pTmp[1]);
								pTmp[0] = null;
								pTmp[1] = null;
							}
							else {
								xLabelType = new XLabelParameterType(paramValue);
							}
							
							labelFeature.addExtendedLabelParameterType(xLabelType);
						}
					}
				}
				else {
					//Parameter Value List Style
					//Example: TYPE=home,parcel,postal,intl
					String[] list = paramTypes.substring(paramTypes.indexOf('=')+1).split(",");
					for(int i = 0; i < list.length; i++) {
						String paramValue = list[i];
						
						try {
							LabelParameterType labelParamType = LabelParameterType.valueOf(paramValue);
							labelFeature.addLabelParameterType(labelParamType);
						}
						catch(IllegalArgumentException iae) {
							XLabelParameterType xLabelType = null;
							if(paramValue.indexOf('=') != -1) {
								String[] pTmp = paramValue.split("=");
								xLabelType = new XLabelParameterType(pTmp[0], pTmp[1]);
								pTmp[0] = null;
								pTmp[1] = null;
							}
							else {
								xLabelType = new XLabelParameterType(paramValue);
							}
							
							labelFeature.addExtendedLabelParameterType(xLabelType);
						}
					}
				}
			}
			
			if(VCardUtils.needsUnEscaping(value)) {
				labelFeature.setLabel(VCardUtils.unescapeString(value));
			}
			else {
				labelFeature.setLabel(value);
			}
			
			if(group != null) {
				labelFeature.setGroup(group);
			}
			
			boolean match = false;
			Iterator<AddressFeature> addrIter = vcard.getAddresses();
			while(addrIter.hasNext() && !match) {
				
				//Get address and all its parameter and extended parameter types
				AddressFeature addr = addrIter.next();
				List<AddressParameterType> aPrmList = addr.getAddressParameterTypesList();
				List<XAddressParameterType> aXPrmList = addr.getExtendedAddressParameterTypesList();
				
				Iterator<LabelParameterType> lPrmIter = labelFeature.getLabelParameterTypes();
				Iterator<XLabelParameterType> lXPrmIter = labelFeature.getExtendedLabelParameterTypes();
				
				//See how many address parameter types match each label parameter type
				int paramsMatched = 0;
				while(lPrmIter.hasNext()) {
					LabelParameterType labelParamType = lPrmIter.next();
					for(int i = 0; i < aPrmList.size(); i++) {
						if(aPrmList.get(i).getType().equals(labelParamType.getType())) {
							paramsMatched++;
						}
					}
				}
				
				//See how many extended address parameter types match each extended label parameter type
				int xparamsMatched = 0;
				while(lXPrmIter.hasNext()) {
					XLabelParameterType xlabelParamType = lXPrmIter.next();
					for(int i = 0; i < aXPrmList.size(); i++) {
						if(aXPrmList.get(i).getType().equals(xlabelParamType.getType())) {
							xparamsMatched++;
						}
					}
				}
				
				//If the number of matching parameter types match between the label
				//and the address then this label belongs to the respective address.
				if(paramsMatched == labelFeature.getLabelParameterSize() && xparamsMatched == labelFeature.getExtendedLabelParameterSize()) {
					//Only set the label on the address if it does not already have one 
					if(!vcard.hasLabel(addr)) {
						vcard.setLabel(labelFeature, addr);
					}
					else {
						vcard.addError("Label with duplicate parameter tpyes was detected and ignored. Label -> "+labelFeature.toString(), ErrorSeverity.WARNING, new VCardBuildException("Duplicate label"));
					}
					
					match = true;
				}
			}
		}
		catch(Exception ex) {
			throw new VCardBuildException("LabelType ("+VCardType.LABEL.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the TEL type.</p>
	 *
	 * @param group
	 * @param value
	 * @param paramTypes
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseTelType(String group, String value, String paramTypes, VCardImpl vcard) throws VCardBuildException {
		try {
			TelephoneFeature telephoneFeature = new TelephoneType();
			if(paramTypes != null) {
				if(paramTypes.indexOf(';') != -1) {
					//Parameter List Style
					//Example: TYPE=cell;TYPE=home;TYPE=fax;TYPE=X-SAT-PHONE;TYPE=X-PRIORITY=1
					
					String[] list = paramTypes.split(";");
					for(int i = 0; i < list.length; i++) {
						String paramType = list[i];
						
						//Only do this to remove the TYPE prefix on certain occasions
						String paramValue = null;
						if(paramType.toUpperCase().contains("TYPE")) {
							paramValue = paramType.substring(paramType.indexOf('=')+1);
						}
						else {
							paramValue = paramType;
						}
						
						try {
							TelephoneParameterType telephoneParamType = TelephoneParameterType.valueOf(paramValue);
							telephoneFeature.addTelephoneParameterType(telephoneParamType);
						}
						catch(IllegalArgumentException iae) {
							XTelephoneParameterType xTelType = null;
							if(paramValue.indexOf('=') != -1) {
								String[] pTmp = paramValue.split("=");
								xTelType = new XTelephoneParameterType(pTmp[0], pTmp[1]);
								pTmp[0] = null;
								pTmp[1] = null;
							}
							else {
								xTelType = new XTelephoneParameterType(paramValue);
							}
							
							telephoneFeature.addExtendedTelephoneParameterType(xTelType);
						}
					}
				}
				else {
					//Parameter Value List Style
					//Example: TYPE=cell,home,fax,x-priority=1,x-satphone
					
					String[] list = paramTypes.substring(paramTypes.indexOf('=')+1).split(",");
					for(int i = 0; i < list.length; i++) {
						String paramValue = list[i];
						
						try {
							TelephoneParameterType telephoneParamType = TelephoneParameterType.valueOf(paramValue);
							telephoneFeature.addTelephoneParameterType(telephoneParamType);
						}
						catch(IllegalArgumentException iae) {
							XTelephoneParameterType xTelType = null;
							if(paramValue.indexOf('=') != -1) {
								String[] pTmp = paramValue.split("=");
								xTelType = new XTelephoneParameterType(pTmp[0], pTmp[1]);
								pTmp[0] = null;
								pTmp[1] = null;
							}
							else {
								xTelType = new XTelephoneParameterType(paramValue);
							}
							
							telephoneFeature.addExtendedTelephoneParameterType(xTelType);
						}
					}
				}
			}
			
			if(VCardUtils.needsUnEscaping(value)) {
				telephoneFeature.setTelephone(VCardUtils.unescapeString(value));
			}
			else {
				telephoneFeature.setTelephone(value);
			}
			
			if(group != null) {
				telephoneFeature.setGroup(group);
			}
			
			vcard.addTelephoneNumber(telephoneFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("TelephoneType ("+VCardType.TEL.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the EMAIL type.</p>
	 *
	 * @param group
	 * @param value
	 * @param paramTypes
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseEmailType(String group, String value, String paramTypes, VCardImpl vcard) throws VCardBuildException {
		try {
			EmailFeature emailFeature = new EmailType();
			if(paramTypes != null) {
				if(paramTypes.indexOf(';') != -1) {
					//Parameter List Style
					//Example: TYPE=pref;TYPE=internet
					String[] list = paramTypes.split(";");
					for(int i = 0; i < list.length; i++) {
						String paramType = list[i];
						
						//Only do this to remove the TYPE prefix on certain occasions
						String paramValue = null;
						if(paramType.toUpperCase().contains("TYPE")) {
							paramValue = paramType.substring(paramType.indexOf('=')+1);
						}
						else {
							paramValue = paramType;
						}
						
						try {
							EmailParameterType emailParamType = EmailParameterType.valueOf(paramValue);
							emailFeature.addEmailParameterType(emailParamType);
						}
						catch(IllegalArgumentException iae) {
							XEmailParameterType xEmailType = null;
							if(paramValue.indexOf('=') != -1) {
								String[] pTmp = paramValue.split("=");
								xEmailType = new XEmailParameterType(pTmp[0], pTmp[1]);
								pTmp[0] = null;
								pTmp[1] = null;
							}
							else {
								xEmailType = new XEmailParameterType(paramValue);
							}
							
							emailFeature.addExtendedEmailParameterType(xEmailType);
						}
					}
				}
				else {
					//Parameter Value List Style
					//Example: TYPE=pref,internet
					String[] list = paramTypes.substring(paramTypes.indexOf('=')+1).split(",");
					for(int i = 0; i < list.length; i++) {
						String paramValue = list[i];
						
						try {
							EmailParameterType emailParamType = EmailParameterType.valueOf(paramValue);
							emailFeature.addEmailParameterType(emailParamType);
						}
						catch(IllegalArgumentException iae) {
							XEmailParameterType xEmailType = null;
							if(paramValue.indexOf('=') != -1) {
								String[] pTmp = paramValue.split("=");
								xEmailType = new XEmailParameterType(pTmp[0], pTmp[1]);
								pTmp[0] = null;
								pTmp[1] = null;
							}
							else {
								xEmailType = new XEmailParameterType(paramValue);
							}
							
							emailFeature.addExtendedEmailParameterType(xEmailType);
						}
					}
				}
			}
			
			emailFeature.setEmail(value);
			
			if(group != null) {
				emailFeature.setGroup(group);
			}
			
			vcard.addEmail(emailFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("EmailType ("+VCardType.EMAIL.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the MAILER type.</p>
	 *
	 * @param group
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseMailerType(String group, String value, VCardImpl vcard) throws VCardBuildException {
		try {
			MailerFeature mailerFeature = new MailerType();
			if(VCardUtils.needsUnEscaping(value)) {
				mailerFeature.setMailer(VCardUtils.unescapeString(value));
			}
			else {
				mailerFeature.setMailer(value);
			}
			
			if(group != null) {
				mailerFeature.setGroup(group);
			}
			
			vcard.setMailer(mailerFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("MailerType ("+VCardType.MAILER.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the TZ type.</p>
	 *
	 * @param group
	 * @param value
	 * @param paramTypes
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseTzType(String group, String value, String paramTypes, VCardImpl vcard) throws VCardBuildException {
		try {
			TimeZoneFeature timeZoneFeature = new TimeZoneType();
			if(paramTypes != null) {
				//VALUE=TEXT
				//-05:00; EST; Raleigh/North America
				String paramValue = paramTypes.substring(paramTypes.indexOf('=')+1);
				if(paramValue.compareToIgnoreCase("TEXT") == 0) {
					timeZoneFeature.setTextValue(value);
				}
			}
			else {
				if(value.matches(ISOUtils.ISO8601_TIMEZONE_BASIC_REGEX)) {
					//-500 or -0500
					if(value.startsWith("-")) {
						String hour = null;
						String minute = null;
						if(value.length() == 4) {
							hour = value.substring(0, 2);
							minute = value.substring(2);
						}
						else if(value.length() == 5) {
							hour = value.substring(0, 3);
							minute = value.substring(3);
						}
						else {
							throw new VCardBuildException("TimeZoneType ("+VCardType.TZ.getType()+") Timezone value is not a valid ISO-8601 text.");
						}
							
						int offsetMillis = Integer.parseInt(hour) + (Integer.parseInt(minute) / 10);
						offsetMillis = (((offsetMillis * 60) * 60) * 1000);
						
						TimeZone tz = TimeZone.getDefault();
						tz.setRawOffset(offsetMillis);
						timeZoneFeature.setTimeZone(tz);
						
					}
					else {
						//500 or 0500
						String hour = null;
						String minute = null;
						if(value.length() == 3) {
							hour = value.substring(0, 1);
							minute = value.substring(1);
						}
						else if(value.length() == 4) {
							hour = value.substring(0, 2);
							minute = value.substring(2);
						}
						else {
							throw new VCardBuildException("TimeZoneType ("+VCardType.TZ.getType()+") Timezone value is not a valid ISO-8601 text.");
						}
						
						int offsetMillis = Integer.parseInt(hour) + (Integer.parseInt(minute) / 10);
						offsetMillis = (((offsetMillis * 60) * 60) * 1000);
						
						TimeZone tz = TimeZone.getDefault();
						tz.setRawOffset(offsetMillis);
						timeZoneFeature.setTimeZone(tz);
					}
				}
				else if(value.matches(ISOUtils.ISO8601_TIMEZONE_EXTENDED_REGEX)) {
					//-5:00 or -05:00 or 5:00 or 05:00
					String[] split = value.split(":");
					String hour = split[0];
					String minute = split[1];
					
					int offsetMillis = Integer.parseInt(hour) + (Integer.parseInt(minute) / 10);
					offsetMillis = (((offsetMillis * 60) * 60) * 1000);
					
					TimeZone tz = TimeZone.getDefault();
					tz.setRawOffset(offsetMillis);
					timeZoneFeature.setTimeZone(tz);
				}
				else {
					throw new VCardBuildException("TimeZoneType ("+VCardType.TZ.getType()+") Timezone value is not a valid ISO-8601 text.");
				}
			}
			
			if(group != null) {
				timeZoneFeature.setGroup(group);
			}
			
			vcard.setTimeZone(timeZoneFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("TimeZoneType ("+VCardType.TZ.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the GEO type.</p>
	 *
	 * @param group
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseGeoType(String group, String value, VCardImpl vcard) throws VCardBuildException {
		try {
			GeographicPositionFeature geographicPositionFeature = new GeographicPositionType();
			if(value.matches("-?\\d{1,3}\\.\\d{1,6}\\;-?\\d{1,3}\\.\\d{1,6}")) {
				String[] geo = value.split(";");
				String lat = geo[0];
				String lon = geo[1];
				geographicPositionFeature.setLatitude(Float.parseFloat(lat));
				geographicPositionFeature.setLongitude(Float.parseFloat(lon));
				
				if(group != null) {
					geographicPositionFeature.setGroup(group);
				}
				
				vcard.setGeographicPosition(geographicPositionFeature);
			}
			else {
				throw new VCardBuildException("GeographicPositionType ("+VCardType.GEO.getType()+") GeographicPositionType is not valid.");
			}
		}
		catch(Exception ex) {
			throw new VCardBuildException("GeographicPositionType ("+VCardType.GEO.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the TITLE type.</p>
	 *
	 * @param group
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseTitleType(String group, String value, VCardImpl vcard) throws VCardBuildException {
		try {
			TitleFeature titleFeature = new TitleType();
			if(VCardUtils.needsUnEscaping(value)) {
				titleFeature.setTitle(VCardUtils.unescapeString(value));
			}
			else {
				titleFeature.setTitle(value);
			}
			
			if(group != null) {
				titleFeature.setGroup(group);
			}
			
			vcard.setTitle(titleFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("TitleType ("+VCardType.TITLE.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the ROLE type.</p>
	 *
	 * @param group
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseRoleType(String group, String value, VCardImpl vcard) throws VCardBuildException {
		try {
			RoleFeature roleFeature = new RoleType();
			if(VCardUtils.needsUnEscaping(value)) {
				roleFeature.setRole(VCardUtils.unescapeString(value));
			}
			else {
				roleFeature.setRole(value);
			}
			
			if(group != null) {
				roleFeature.setGroup(group);
			}
			
			vcard.setRole(roleFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("RoleType ("+VCardType.ROLE.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the LOGO type.</p>
	 *
	 * @param group
	 * @param value
	 * @param paramTypes
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseLogoType(String group, String value, String paramTypes, VCardImpl vcard) throws VCardBuildException {
		try {
			LogoFeature logoFeature = new LogoType();
			String[] params = paramTypes.split(";");
			boolean isBinary = false;
			
			//Parse the parameter types
			for(int i = 0; i < params.length; i++) {
				String[] paramType = null;
				
				switch(getCompatibilityMode())
				{
					case I_PHONE:
					case MAC_ADDRESS_BOOK:
					{
						if(params[i].contains("=")) {
							//For proper vcard parameter types
							paramType = params[i].trim().split("=");
						}
						else {
							//When the parameter types are missing we try to guess what they are.
							//We really should not as it breaks RFC rules but some apps do broken exports.
							
							if(params[i].equals(EncodingType.BASE64.getType())) {
								paramType = new String[] {PhotoParameterType.ENCODING.getTypeName(), params[i]};
							}
							else if(params[i].equals(EncodingType.BINARY.getType())) {
								paramType = new String[] {PhotoParameterType.ENCODING.getTypeName(), params[i]};
							}
							else if(params[i].equals("URI")) {
								paramType = new String[] {PhotoParameterType.VALUE.getTypeName(), params[i]};
							}
							else {
								//If we are here then we can only assume this is
								//a IANA registered image type (or custom type). 
								paramType = new String[] {PhotoParameterType.TYPE.getTypeName(), params[i]};
							}
						}
						
						break;
					}
					
					case EVOLUTION:
					case KDE_ADDRESS_BOOK:
					case MS_OUTLOOK:
					case RFC2426:
					default:
					{
						if(params[i].contains("=")) {
							paramType = params[i].trim().split("=");
						}
						else {
							/*
							 * Type special notes: The type can include the type parameter "TYPE" to
							 * specify the graphic image format type. The TYPE parameter values MUST
							 * be one of the IANA registered image formats or a non-standard image format.
							 */
							
							paramType = new String[] {PhotoParameterType.TYPE.getTypeName(), params[i]};
						}
						
						break;
					}
				}
				
				/*
				 * ENCODING=b would look like
				 * paramType[0] = ENCODING
				 * paramType[1] = b
				 */
				
				LogoParameterType logoParamType = LogoParameterType.valueOf(paramType[0]);
				switch(logoParamType)
				{
					case ENCODING:
					{
						if(paramType[1].compareToIgnoreCase(EncodingType.BINARY.getType()) == 0) {
							logoFeature.setEncodingType(EncodingType.BINARY);
							isBinary = true;
						}
						else if(paramType[1].compareToIgnoreCase(EncodingType.BASE64.getType()) == 0) {
							logoFeature.setEncodingType(EncodingType.BINARY);
							isBinary = true;
						}
						else {
							throw new VCardBuildException("LogoType ("+VCardType.LOGO.getType()+") Invalid encoding type \""+paramType[1]+"\"");
						}
						
						break;
					}
					
					case TYPE:
					{
						ImageMediaType mediaType = null;
						try {
							mediaType = ImageMediaType.valueOf(paramType[1]);
							logoFeature.setImageMediaType(mediaType);
						}
						catch(IllegalArgumentException iae) {
							mediaType = ImageMediaType.NON_STANDARD;
							mediaType.setTypeName(paramType[1].trim());
							mediaType.setIanaRegisteredName(paramType[1].trim());
							mediaType.setExtension(paramType[1].trim());
						}
						finally {
							logoFeature.setImageMediaType(mediaType);
						}
						
						break;
					}
					
					case VALUE:
					{
						if(paramType[1].compareToIgnoreCase("URI") == 0) {
							logoFeature.setEncodingType(EncodingType.EIGHT_BIT);
							isBinary = false;
						}
						else {
							throw new VCardBuildException("LogoType ("+VCardType.LOGO.getType()+") Invalid value type \""+paramType[1]+"\"");
						}
						
						break;
					}
				}
			}
			
			if(isBinary) {
				byte[] logoBytes = Base64Wrapper.decode(value);
				logoFeature.setCompression(true);
				logoFeature.setLogo(logoBytes);
			}
			else {
				URI logoUri = new URI(value);
				logoFeature.setLogoURI(logoUri);
			}
			
			if(group != null) {
				logoFeature.setGroup(group);
			}
			
			vcard.addLogo(logoFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("LogoType ("+VCardType.LOGO.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the ORG type.</p>
	 *
	 * @param group
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseOrgType(String group, String value, VCardImpl vcard) throws VCardBuildException {
		try {
			OrganizationFeature organizationFeature = new OrganizationType();
			
			/*
			 * If escaped semi-colons exist in the list then replace them
			 * with this temporary sequence. Then after splitting put it
			 * back in.
			 */
			if(value.contains("\\;")) {
				value = value.replaceAll("\\\\;", "!SEMI!");
			}
			
			String[] orgs = value.split(";");
			for(int i = 0; i < orgs.length; i++) {
				if(VCardUtils.needsUnEscaping(orgs[i])) {
					String unesc = VCardUtils.unescapeString(orgs[i]);
					organizationFeature.addOrganization(unesc.replaceAll("\\!SEMI\\!", ";"));
				}
				else {
					organizationFeature.addOrganization(orgs[i].replaceAll("\\!SEMI\\!", ";"));
				}
			}
			
			if(group != null) {
				organizationFeature.setGroup(group);
			}
			
			vcard.setOrganizations(organizationFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("OrganizationType ("+VCardType.ORG.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the CATEGORIES type.</p>
	 *
	 * @param group
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseCategoriesType(String group, String value, VCardImpl vcard) throws VCardBuildException {
		try {
			CategoriesFeature categoriesFeature = new CategoriesType();
			String[] categories = null;
			switch(compatMode)
			{
				case KDE_ADDRESS_BOOK:
				{
					if(VCardUtils.needsUnEscaping(value)) {
						categories = VCardUtils.unescapeString(value).split(",");
					}
					else {
						categories = value.split(",");
					}
					break;
				}
				
				default:
				{
					if(value.contains("\\,")) {
						value = value.replaceAll("\\\\,", " ");
					}
					
					categories = value.split(",");
					break;
				}
			}
			
			for(int i = 0; i < categories.length; i++) {
				if(VCardUtils.needsUnEscaping(categories[i])) {
					categoriesFeature.addCategory(VCardUtils.unescapeString(categories[i]));
				}
				else {
					categoriesFeature.addCategory(categories[i]);
				}
			}
			
			if(group != null) {
				categoriesFeature.setGroup(group);
			}
			
			vcard.setCategories(categoriesFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("CategoriesType ("+VCardType.CATEGORIES.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the NOTE type.</p>
	 *
	 * @param group
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseNoteType(String group, String value, VCardImpl vcard) throws VCardBuildException {
		try {
			NoteFeature noteFeature = new NoteType();
			if(VCardUtils.needsUnEscaping(value)) {
				noteFeature.setNote(VCardUtils.unescapeString(value));
			}
			else {
				noteFeature.setNote(value);
			}
			
			if(group != null) {
				noteFeature.setGroup(group);
			}
			
			vcard.addNote(noteFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("NoteType ("+VCardType.NOTE.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the PRODID type.</p>
	 *
	 * @param group
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseProdidType(String group, String value, VCardImpl vcard) throws VCardBuildException {
		try {
			ProductIdFeature productIdFeature = new ProductIdType();
			productIdFeature.setProductId(value);
			
			if(group != null) {
				productIdFeature.setGroup(group);
			}
			
			vcard.setProductId(productIdFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("ProductIdType ("+VCardType.PRODID.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the REV type.</p>
	 *
	 * @param group
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseRevType(String group, String value, VCardImpl vcard) throws VCardBuildException {
		try {
			RevisionFeature revisionFeature = new RevisionType();
			if(value.matches(ISOUtils.ISO8601_DATE_EXTENDED_REGEX)) {
				//Example: 1996-04-15
				String[] date = value.split("-");
				Calendar cal = Calendar.getInstance();
				cal.clear();
				cal.set(Calendar.YEAR, Integer.parseInt(date[0]));
				cal.set(Calendar.MONTH, Integer.parseInt(date[1])-1);
				cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[2]));
				revisionFeature.setRevision(cal);
			}
			else if(value.matches(ISOUtils.ISO8601_DATE_BASIC_REGEX)) {
				//Example: 19960415
				String year = value.substring(0, 4);
				String month = value.substring(4,6);
				String day = value.substring(6);
				
				Calendar cal = Calendar.getInstance();
				cal.clear();
				cal.set(Calendar.YEAR, Integer.parseInt(year));
				cal.set(Calendar.MONTH, Integer.parseInt(month)-1);
				cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
				revisionFeature.setRevision(cal);
			}
			else if(value.matches(ISOUtils.ISO8601_UTC_TIME_BASIC_REGEX)) {
				//Example: 19960415T231000Z
				String year = value.substring(0, 4);
				String month = value.substring(4, 6);
				String day = value.substring(6, 8);
				String hour = value.substring(9, 11);
				String minute = value.substring(11, 13);
				String seconds = value.substring(13, 15);
				
				Calendar cal = Calendar.getInstance();
				cal.clear();
				cal.set(Calendar.YEAR, Integer.parseInt(year));
				cal.set(Calendar.MONTH, Integer.parseInt(month)-1);
				cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
				cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
				cal.set(Calendar.MINUTE, Integer.parseInt(minute));
				cal.set(Calendar.SECOND, Integer.parseInt(seconds));
				revisionFeature.setRevision(cal);
			}
			else if(value.matches(ISOUtils.ISO8601_UTC_TIME_EXTENDED_REGEX)) {
				//Example: 1996-04-15T23:10:00Z
				String[] split = value.toUpperCase().substring(0, value.indexOf('Z')).split("T");
				String[] date = split[0].split("-");
				String[] time = split[1].split(":");
				
				Calendar cal = Calendar.getInstance();
				cal.clear();
				cal.set(Calendar.YEAR, Integer.parseInt(date[0]));
				cal.set(Calendar.MONTH, Integer.parseInt(date[1])-1);
				cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[2]));
				cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
				cal.set(Calendar.MINUTE, Integer.parseInt(time[1]));
				cal.set(Calendar.SECOND, Integer.parseInt(time[2]));
				revisionFeature.setRevision(cal);
			}
			else if(value.matches(ISOUtils.ISO8601_TIME_EXTENDED_REGEX)) {
				//Example: 1996-04-15T23:10:00-06:00
				String[] split = value.toUpperCase().split("T");
				String[] date = split[0].split("-");
				String time = split[1];
				
				//23:10:00-06:00
				String hour = time.substring(0, 2);
				String minute = time.substring(3, 5);
				String seconds = time.substring(6, 8);
				String operator = time.substring(8, 9);
				String offsHour = time.substring(9, 11);
				String offsMinute = time.substring(12);
				
				
				Calendar cal = Calendar.getInstance();
				cal.clear();
				cal.set(Calendar.YEAR, Integer.parseInt(date[0]));
				cal.set(Calendar.MONTH, Integer.parseInt(date[1])-1);
				cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[2]));
				cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
				cal.set(Calendar.MINUTE, Integer.parseInt(minute));
				cal.set(Calendar.SECOND, Integer.parseInt(seconds));
				
				if(operator.compareTo("-") == 0) {
					offsHour = "-"+offsHour;
				}
				
				int offsetMillis = Integer.parseInt(offsHour) + (Integer.parseInt(offsMinute) / 10);
				offsetMillis = (((offsetMillis * 60) * 60) * 1000);
				
				cal.set(Calendar.ZONE_OFFSET, offsetMillis);
				revisionFeature.setRevision(cal);
			}
			else {
				throw new VCardBuildException("RevisionType ("+VCardType.REV.getType()+") Revision value is not a valid ISO-8601 text.");
			}
			
			if(group != null) {
				revisionFeature.setGroup(group);
			}
			
			vcard.setRevision(revisionFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("RevisionType ("+VCardType.REV.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the SORT-STRING type.</p>
	 *
	 * @param group
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseSortStringType(String group, String value, VCardImpl vcard) throws VCardBuildException {
		try {
			SortStringFeature sortStringFeature = new SortStringType();
			sortStringFeature.setSortString(value);
			
			if(group != null) {
				sortStringFeature.setGroup(group);
			}
			
			vcard.setSortString(sortStringFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("SortStringType ("+VCardType.SORT_STRING.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the SOUND type.</p>
	 *
	 * @param group
	 * @param value
	 * @param paramTypes
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseSoundType(String group, String value, String paramTypes, VCardImpl vcard) throws VCardBuildException {
		try {
			SoundFeature soundFeature = new SoundType();
			String[] params = paramTypes.split(";");
			boolean isBinary = false;
			
			//Parse the parameter types
			for(int i = 0; i < params.length; i++) {
				String[] paramType = null;
				
				switch(getCompatibilityMode())
				{
					case I_PHONE:
					case MAC_ADDRESS_BOOK:
					{
						if(params[i].contains("=")) {
							//For proper vcard parameter types
							paramType = params[i].trim().split("=");
						}
						else {
							//When the parameter types are missing we try to guess what they are.
							//We really should not as it breaks RFC rules but some apps do broken exports.
							
							if(params[i].equals(EncodingType.BASE64.getType())) {
								paramType = new String[] {PhotoParameterType.ENCODING.getTypeName(), params[i]};
							}
							else if(params[i].equals(EncodingType.BINARY.getType())) {
								paramType = new String[] {PhotoParameterType.ENCODING.getTypeName(), params[i]};
							}
							else if(params[i].equals("URI")) {
								paramType = new String[] {PhotoParameterType.VALUE.getTypeName(), params[i]};
							}
							else {
								//If we are here then we can only assume this is
								//a IANA registered image type (or custom type). 
								paramType = new String[] {PhotoParameterType.TYPE.getTypeName(), params[i]};
							}
						}
						
						break;
					}
					
					case EVOLUTION:
					case KDE_ADDRESS_BOOK:
					case MS_OUTLOOK:
					case RFC2426:
					default:
					{
						if(params[i].contains("=")) {
							paramType = params[i].trim().split("=");
						}
						else {
							/*
							 * Type special notes: The type can include the type parameter "TYPE" to
							 * specify the audio format type. The TYPE parameter values MUST be one
							 * of the IANA registered audio formats or a non-standard audio format.
							 */
							
							paramType = new String[] {PhotoParameterType.TYPE.getTypeName(), params[i]};
						}
						
						break;
					}
				}
				
				/*
				 * ENCODING=b would look like
				 * paramType[0] = ENCODING
				 * paramType[1] = b
				 */
				
				SoundParameterType soundParamType = SoundParameterType.valueOf(paramType[0]);
				switch(soundParamType)
				{
					case ENCODING:
					{
						if(paramType[1].compareToIgnoreCase(EncodingType.BINARY.getType()) == 0) {
							soundFeature.setEncodingType(EncodingType.BINARY);
							isBinary = true;
						}
						else if(paramType[1].compareToIgnoreCase(EncodingType.BASE64.getType()) == 0) {
							soundFeature.setEncodingType(EncodingType.BINARY);
							isBinary = true;
						}
						else {
							throw new VCardBuildException("SoundType ("+VCardType.SOUND.getType()+") Invalid encoding type \""+paramType[1]+"\"");
						}
						
						break;
					}
					
					case TYPE:
					{
						AudioMediaType mediaType = null;
						try {
							mediaType = AudioMediaType.valueOf(paramType[1]);
							soundFeature.setAudioMediaType(mediaType);
						}
						catch(IllegalArgumentException iae) {
							mediaType = AudioMediaType.NON_STANDARD;
							mediaType.setTypeName(paramType[1].trim());
							mediaType.setIanaRegisteredName(paramType[1].trim());
							mediaType.setExtension(paramType[1].trim());
						}
						finally {
							soundFeature.setAudioMediaType(mediaType);
						}
						
						break;
					}
					
					case VALUE:
					{
						if(paramType[1].compareToIgnoreCase("URI") == 0) {
							soundFeature.setEncodingType(EncodingType.EIGHT_BIT);
							isBinary = false;
						}
						else {
							throw new VCardBuildException("SoundType ("+VCardType.SOUND.getType()+") Invalid value type \""+paramType[1]+"\"");
						}
						
						break;
					}
				}
			}
			
			if(isBinary) {
				byte[] soundBytes = Base64Wrapper.decode(value);
				soundFeature.setCompression(true);
				soundFeature.setSound(soundBytes);
			}
			else {
				URI soundUri = new URI(value);
				soundFeature.setSoundURI(soundUri);
			}
			
			if(group != null) {
				soundFeature.setGroup(group);
			}
			
			vcard.addSound(soundFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("SoundType ("+VCardType.SOUND.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the UID type.</p>
	 *
	 * @param
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseUidType(String group, String value, VCardImpl vcard) throws VCardBuildException {
		try {
			UIDFeature uidFeature = new UIDType();
			uidFeature.setUID(value);
			
			if(group != null) {
				uidFeature.setGroup(group);
			}
			
			vcard.setUID(uidFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("UIDType ("+VCardType.UID.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the URL type.</p>
	 *
	 * @param group
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseUrlType(String group, String value, VCardImpl vcard) throws VCardBuildException {
		try {
			URLFeature urlFeature = new URLType();
			if(VCardUtils.needsUnEscaping(value)) {
				urlFeature.setURL(new URL(VCardUtils.unescapeString(value)));
			}
			else {
				urlFeature.setURL(new URL(value));
			}
			
			if(group != null) {
				urlFeature.setGroup(group);
			}
			
			vcard.addURL(urlFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("URLType ("+VCardType.URL.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the CLASS type.</p>
	 *
	 * @param group
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseClassType(String group, String value, VCardImpl vcard) throws VCardBuildException {
		try {
			ClassFeature classFeature = new ClassType();
			classFeature.setSecurityClass(value);
			
			if(group != null) {
				classFeature.setGroup(group);
			}
			
			vcard.setSecurityClass(classFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("ClassType ("+VCardType.CLASS.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the KEY type.</p>
	 *
	 * @param group
	 * @param value
	 * @param paramTypes
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseKeyType(String group, String value, String paramTypes, VCardImpl vcard) throws VCardBuildException {
		try {
			KeyFeature keyFeature = new KeyType();
			String[] params = paramTypes.split(";");
			
			//Parse the parameter types
			for(int i = 0; i < params.length; i++) {
				String[] paramType = null;
				
				switch(getCompatibilityMode())
				{
					case I_PHONE:
					case MAC_ADDRESS_BOOK:
					{
						if(params[i].contains("=")) {
							//For proper vcard parameter types
							paramType = params[i].trim().split("=");
						}
						else {
							//When the parameter types are missing we try to guess what they are.
							//We really should not as it breaks RFC rules but some apps do broken exports.
							
							if(params[i].equals(EncodingType.BASE64.getType())) {
								paramType = new String[] {PhotoParameterType.ENCODING.getTypeName(), params[i]};
							}
							else if(params[i].equals(EncodingType.BINARY.getType())) {
								paramType = new String[] {PhotoParameterType.ENCODING.getTypeName(), params[i]};
							}
							else if(params[i].equals("URI")) {
								paramType = new String[] {PhotoParameterType.VALUE.getTypeName(), params[i]};
							}
							else {
								//If we are here then we can only assume this is
								//a IANA registered image type (or custom type). 
								paramType = new String[] {PhotoParameterType.TYPE.getTypeName(), params[i]};
							}
						}
						
						break;
					}
					
					case EVOLUTION:
					case KDE_ADDRESS_BOOK:
					case MS_OUTLOOK:
					case RFC2426:
					default:
					{
						if(params[i].contains("=")) {
							paramType = params[i].trim().split("=");
						}
						else {
							/*
							 * Type special notes: The type can also include the type parameter TYPE
							 * to specify the public key or authentication certificate format. The
							 * parameter type should specify an IANA registered public key or
							 * authentication certificate format. The parameter type can also
							 * specify a non-standard format.
							 */
							
							paramType = new String[] {KeyParameterType.TYPE.getTypeName(), params[i]};
						}
						
						break;
					}
				}
				
				/*
				 * ENCODING=b would look like
				 * paramType[0] = ENCODING
				 * paramType[1] = b
				 */
				
				KeyParameterType keyParamType = KeyParameterType.valueOf(paramType[0]);
				switch(keyParamType)
				{
					case ENCODING:
					{
						if(paramType[1].compareToIgnoreCase(EncodingType.BINARY.getType()) == 0) {
							keyFeature.setEncodingType(EncodingType.BINARY);
						}
						else if(paramType[1].compareToIgnoreCase(EncodingType.BASE64.getType()) == 0) {
							keyFeature.setEncodingType(EncodingType.BINARY);
						}
						else {
							throw new VCardBuildException("KeyType ("+VCardType.KEY.getType()+") Invalid encoding type \""+paramType[1]+"\"");
						}
						
						break;
					}
					
					case TYPE:
					{
						KeyTextType keyTextType = null;
						try {
							keyTextType = KeyTextType.valueOf(paramType[1]);
							keyFeature.setKeyTextType(keyTextType);
						}
						catch(IllegalArgumentException iae) {
							keyTextType = KeyTextType.NON_STANDARD;
							keyTextType.setTypeName(paramType[1].trim());
							keyTextType.setIanaRegisteredName(paramType[1].trim());
							keyTextType.setExtension(paramType[1].trim());
						}
						finally {
							keyFeature.setKeyTextType(keyTextType);
						}
						
						break;
					}
				}
			}
			
			byte[] keyBytes = Base64Wrapper.decode(value);
			keyFeature.setCompression(true);
			keyFeature.setKey(keyBytes);
			
			if(group != null) {
				keyFeature.setGroup(group);
			}
			
			vcard.addKey(keyFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("KeyType ("+VCardType.KEY.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the EXTENDED type.</p>
	 *
	 * @param group
	 * @param value
	 * @param typeName
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseXtendedType(String group, String value, String typeName, VCardImpl vcard) throws VCardBuildException {
		try {
			
			ExtendedFeature extendedFeature = new ExtendedType();
			extendedFeature.setExtensionName(typeName);
			extendedFeature.setExtensionData(value);
			
			if(group != null) {
				extendedFeature.setGroup(group);
			}
			
			vcard.addExtendedType(extendedFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("ExtendedType ("+VCardType.XTENDED.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the NAME type.</p>
	 *
	 * @param group
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseDisplayableNameType(String group, String value, VCardImpl vcard) throws VCardBuildException {
		try {
			DisplayableNameFeature displayableNameFeature = new DisplayableNameType();
			displayableNameFeature.setName(value);
			
			if(group != null) {
				displayableNameFeature.setGroup(group);
			}
			
			vcard.setDisplayableNameFeature(displayableNameFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("DisplayableNameType ("+VCardType.NAME.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the PROFILE type.</p>
	 *
	 * @param group
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseProfileType(String group, String value, VCardImpl vcard) throws VCardBuildException {
		try {
			ProfileFeature profileFeature = new ProfileType();
			profileFeature.setProfile(value);
			
			if(group != null) {
				profileFeature.setGroup(group);
			}
			
			vcard.setProfile(profileFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("ProfileType ("+VCardType.PROFILE.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Parses the SOURCE type.</p>
	 *
	 * @param group
	 * @param value
	 * @param vcard
	 * @throws VCardBuildException
	 */
	private void parseSourceType(String group, String value, VCardImpl vcard) throws VCardBuildException {
		try {
			SourceFeature sourceFeature = new SourceType();
			sourceFeature.setSource(value);
			
			if(group != null) {
				sourceFeature.setGroup(group);
			}
			
			vcard.setSource(sourceFeature);
		}
		catch(Exception ex) {
			throw new VCardBuildException("SourceType ("+VCardType.SOURCE.getType()+") ["+ex.getClass().getName()+"] "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Creates a VCardError object and sets the specified error information
	 * in it and adds it to the VCard currently being parses.</p>
	 *
	 * @see VCardError
	 * @see ErrorSeverity
	 * 
	 * @param vcard
	 * @param errorMessage
	 * @param exception
	 * @param severity
	 */
	private void handleError(VCardImpl vcard, String errorMessage, Throwable exception, ErrorSeverity severity) {
		VCardError vError = new VCardError();
		vError.setErrorMessage(errorMessage);
		vError.setSeverity(severity);
		
		if(exception != null) {
			vError.setError(exception);
		}
		
		vcard.addError(vError);
	}
	
	/**
	 * <p>Splits each line of the vcard into an array of 2 cells.
	 * The first cell contains the VCard type (may or may not include
	 * parameter types.) The second cell contains the Type values (may
	 * or may not contains comma or semicolon delimited lists.)</p>
	 * 
	 * @param vcardString
	 * @return List&lt;String[]&gt;
	 */
	private List<String[]> splitLines(String vcardString)
	{
		String[] strArray = vcardString.split("\n");
		List<String[]> arrayLines = new ArrayList<String[]>(strArray.length);
		String line = null;

		for (int i = 0; i < strArray.length; i++) {
			line = strArray[i];
			String[] subLine = line.split(":", 2);
			arrayLines.add(subLine);
		}

		return arrayLines;
	}
	
	/**
	 * <p>Returns the contents of the vcard file where each line is delimited with
	 * the standard java EOL char '\n' This is still a folded vcard String.</p>
	 * 
	 * @param vcardFile
	 * @return {@link String}
	 * @throws IOException
	 */
	private String getContentFromFile(File vcardFile) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(vcardFile));
		String line = "";
		StringBuilder sb = new StringBuilder();

		while((line = br.readLine()) != null) {
			// skips blank lines
			if(!line.matches("$")) {
				sb.append(line);
				sb.append("\n");
			}
		}

		br.close();
		return sb.toString();
	}
	
	/**
	 * <p>Returns the contents of the vcard String where each line is delimited with
	 * the standard java EOL char '\n' This is still a folded vcard String.</p>
	 *
	 * @param vcardString
	 * @return {@link String}
	 * @throws IOException
	 */
	private String getContentFromString(String vcardString) throws IOException
	{
		BufferedReader br = new BufferedReader(new StringReader(vcardString));
		String line = "";
		StringBuilder sb = new StringBuilder();

		while((line = br.readLine()) != null) {
			// skips blank lines
			if(!line.matches("$")) {
				sb.append(line);
				sb.append("\n");
			}
		}

		br.close();
		return sb.toString();
	}
}
