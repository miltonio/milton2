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

package info.ineighborhood.cardme.vcard.types;

import info.ineighborhood.cardme.util.Util;
import info.ineighborhood.cardme.vcard.EncodingType;
import info.ineighborhood.cardme.vcard.VCardType;
import info.ineighborhood.cardme.vcard.features.NameFeature;
import info.ineighborhood.cardme.vcard.types.parameters.ParameterTypeStyle;
import java.util.ArrayList;
import java.util.Iterator;
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
 * Feb 4, 2010
 * 
 * 
 */
public class NameType extends Type implements NameFeature {

	private String familyName = null;
	private String givenName = null;
	private List<String> additionalNames = null;
	private List<String> honorificPrefixes = null;
	private List<String> honorificSuffixes = null;
	
	public NameType() {
		this(null, null);
	}
	
	public NameType(String familyName) {
		this(familyName, null);
	}
	
	public NameType(String familyName, String givenName) {
		super(EncodingType.EIGHT_BIT, ParameterTypeStyle.PARAMETER_VALUE_LIST);
		setFamilyName(familyName);
		setGivenName(givenName);
		additionalNames = new ArrayList<String>();
		honorificPrefixes = new ArrayList<String>();
		honorificSuffixes = new ArrayList<String>();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getFamilyName()
	{
		return familyName;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getGivenName()
	{
		return givenName;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Iterator<String> getAdditionalNames()
	{
		return additionalNames.listIterator();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Iterator<String> getHonorificPrefixes()
	{
		return honorificPrefixes.listIterator();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Iterator<String> getHonorificSuffixes()
	{
		return honorificSuffixes.listIterator();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addAdditionalName(String additionalName) {
		additionalNames.add(additionalName);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addHonorificPrefix(String honorificPrefix) {
		honorificPrefixes.add(honorificPrefix);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addHonorificSuffix(String honorificSuffix) {
		honorificSuffixes.add(honorificSuffix);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeAdditionalName(String additionalName) {
		additionalNames.remove(additionalName);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeHonorificPrefix(String honorificPrefix) {
		honorificPrefixes.remove(honorificPrefix);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeHonorificSuffix(String honorificSuffix) {
		honorificSuffixes.remove(honorificSuffix);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsAdditionalName(String additionalName)
	{
		return additionalNames.contains(additionalName);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsHonorificPrefix(String honorificPrefix)
	{
		return honorificPrefixes.contains(honorificPrefix);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsHonorificSuffix(String honorificSuffix)
	{
		return honorificSuffixes.contains(honorificSuffix);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasFamilyName()
	{
		return familyName != null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasGivenName()
	{
		return givenName != null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasAdditionalNames()
	{
		return !additionalNames.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasHonorificPrefixes()
	{
		return !honorificPrefixes.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasHonorificSuffixes()
	{
		return !honorificSuffixes.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearAdditionalNames() {
		additionalNames.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearHonorificPrefixes() {
		honorificPrefixes.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearHonorificSuffixes() {
		honorificSuffixes.clear();
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTypeString()
	{
		return VCardType.N.getType();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj)
	{
		if(obj != null) {
			if(obj instanceof NameType) {
				if(this == obj || ((NameType)obj).hashCode() == this.hashCode()) {
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
		if(encodingType != null) {
			sb.append(encodingType.getType());
			sb.append(",");
		}
		
		if(!honorificPrefixes.isEmpty()) {
			for(int i = 0; i < honorificPrefixes.size(); i++) {
				sb.append(honorificPrefixes.get(i));
				sb.append(",");
			}
		}
		
		if(familyName != null) {
			sb.append(familyName);
			sb.append(",");
		}
		
		if(givenName != null) {
			sb.append(givenName);
			sb.append(",");
		}
		
		if(!additionalNames.isEmpty()) {
			for(int i = 0; i < additionalNames.size(); i++) {
				sb.append(additionalNames.get(i));
				sb.append(",");
			}
		}
		
		if(!honorificSuffixes.isEmpty()) {
			for(int i = 0; i < honorificSuffixes.size(); i++) {
				sb.append(honorificSuffixes.get(i));
				sb.append(",");
			}
		}

		if(super.id != null) {
			sb.append(super.id);
			sb.append(",");
		}
		
		sb.deleteCharAt(sb.length()-1);	//Remove last comma.
		sb.append(" ]");
		return sb.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public NameFeature clone()
	{
		NameType cloned = new NameType();
		
		if(familyName != null) {
			cloned.setFamilyName(new String(familyName));
		}
		
		if(givenName != null) {
			cloned.setGivenName(new String(givenName));
		}
		
		if(!additionalNames.isEmpty()) {
			for(int i = 0; i < additionalNames.size(); i++) {
				cloned.addAdditionalName(new String(additionalNames.get(i)));
			}
		}
		
		if(!honorificPrefixes.isEmpty()) {
			for(int i = 0; i < honorificPrefixes.size(); i++) {
				cloned.addHonorificPrefix(new String(honorificPrefixes.get(i)));
			}
		}
		
		if(!honorificSuffixes.isEmpty()) {
			for(int i = 0; i < honorificSuffixes.size(); i++) {
				cloned.addHonorificSuffix(new String(honorificSuffixes.get(i)));
			}
		}
		
		cloned.setParameterTypeStyle(getParameterTypeStyle());
		cloned.setEncodingType(getEncodingType());
		cloned.setID(getID());
		return cloned;
	}
}
