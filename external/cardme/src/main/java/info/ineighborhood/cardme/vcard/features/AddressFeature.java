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

package info.ineighborhood.cardme.vcard.features;

import info.ineighborhood.cardme.vcard.types.parameters.AddressParameterType;
import info.ineighborhood.cardme.vcard.types.parameters.XAddressParameterType;
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
 * <p><b>RFC 2426</b></br>
 * <b>3.2.1 ADR Type Definition</b>
 * <ul>
 * 	<li><b>Type name:</b> ADR</li>
 * 	<li><b>Type purpose:</b> To specify the components of the delivery address for the vCard object.</li>
 * 	<li><b>Type encoding:</b> 8bit</li>
 * 	<li><b>Type value:</b> A single structured text value, separated by the SEMI-COLON character (ASCII decimal 59).</li>
 * 	<li><b>Type special note:</b> The structured type value consists of a sequence of address components. The component values MUST be specified in their corresponding position. The structured type value corresponds, in sequence, to the post office box; the extended address; the street address; the locality (e.g., city); the region (e.g., state or province); the postal code; the country name. When a component value is missing, the associated component separator MUST still be specified.</li>
 * </ul>
 * </p>
 */
public interface AddressFeature extends TypeTools {
	
	/**
	 * <p>Returns the post office box.</p>
	 *
	 * @return {@link String}
	 */
	public String getPostOfficeBox();
	
	/**
	 * <p>Returns the extended address.</p>
	 *
	 * @return {@link String}
	 */
	public String getExtendedAddress();
	
	/**
	 * <p>Returns the street address.</p>
	 *
	 * @return {@link String}
	 */
	public String getStreetAddress();
	
	/**
	 * <p>Returns the locality.</p>
	 *
	 * @return {@link String}
	 */
	public String getLocality();
	
	/**
	 * <p>Returns the region.</p>
	 *
	 * @return {@link String}
	 */
	public String getRegion();
	
	/**
	 * <p>Returns the postal code.</p>
	 *
	 * @return {@link String}
	 */
	public String getPostalCode();
	
	/**
	 * <p>Returns the country name.</p>
	 *
	 * @return {@link String}
	 */
	public String getCountryName();
	
	/**
	 * <p>Sets the post office box.</p>
	 *
	 * @param postOfficeBox
	 */
	public void setPostOfficeBox(String postOfficeBox);
	
	/**
	 * <p>Sets the extended address.</p>
	 *
	 * @param extendedAddress
	 */
	public void setExtendedAddress(String extendedAddress);
	
	/**
	 * <p>Sets the street address.</p>
	 *
	 * @param streetAddress
	 */
	public void setStreetAddress(String streetAddress);
	
	/**
	 * <p>Sets the locality.</p>
	 *
	 * @param locality
	 */
	public void setLocality(String locality);
	
	/**
	 * <p>Sets the region.</p>
	 *
	 * @param region
	 */
	public void setRegion(String region);
	
	/**
	 * <p>Sets the postal code.</p>
	 *
	 * @param postalCode
	 */
	public void setPostalCode(String postalCode);
	
	/**
	 * <p>Sets the country name.</p>
	 *
	 * @param countryName
	 */
	public void setCountryName(String countryName);
	
	/**
	 * <p>Returns an iterator all parameter types.</p>
	 *
	 * @return {@link Iterator}&lt;AddressParameterType&gt;
	 */
	public Iterator<AddressParameterType> getAddressParameterTypes();
	
	/**
	 * <p>Returns an unmodifiable list of parameter types.</p>
	 *
	 * @return {@link List}&lt;AddressParameterType&gt;
	 */
	public List<AddressParameterType> getAddressParameterTypesList();
	
	/**
	 * <p>Returns the number of parameter types.</p>
	 *
	 * @return int
	 */
	public int getAddressParameterSize();
	
	/**
	 * <p>Adds an address parameter type.</p>
	 *
	 * @param addressParameterType
	 */
	public void addAddressParameterType(AddressParameterType addressParameterType);
	
	/**
	 * <p>Removes a parameter type.</p>
	 *
	 * @param addressParameterType
	 */
	public void removeAddressParameterType(AddressParameterType addressParameterType);
	
	/**
	 * <p>Returns true if the parameter type exists.</p>
	 *
	 * @param addressParameterType
	 * @return boolean
	 */
	public boolean containsAddressParameterType(AddressParameterType addressParameterType);
	
	/**
	 * <p>Removes all address parameter types.</p>
	 */
	public void clearAddressParameterTypes();
	
	/**
	 * <p>Returns an iterator of extended address parameter types.</p>
	 * 
	 * @return {@link Iterator}&lt;XAddressParameterType&gt;
	 */
	public Iterator<XAddressParameterType> getExtendedAddressParameterTypes();
	
	/**
	 * <p>Returns an unmodifiable list of extended parameter types.</p>
	 *
	 * @return {@link List}&lt;XAddressParameterType&gt;
	 */
	public List<XAddressParameterType> getExtendedAddressParameterTypesList();
	
	/**
	 * <p>Returns the number of extended parameter types.</p>
	 *
	 * @return int
	 */
	public int getExtendedAddressParameterSize();
	
	/**
	 * <p>Adds an extended address parameter type.</p>
	 * 
	 * @param xtendedAddressParameterType
	 */
	public void addExtendedAddressParameterType(XAddressParameterType xtendedAddressParameterType);
	
	/**
	 * <p>Removes the specified extended address parameter type.</p>
	 * 
	 * @param xtendedAddressParameterType
	 */
	public void removeExtendedAddressParameterType(XAddressParameterType xtendedAddressParameterType);
	
	/**
	 * <p>Returns true if the specified extended address parameter type exists.</p>
	 * 
	 * @param xtendedAddressParameterType
	 * @return boolean
	 */
	public boolean containsExtendedAddressParameterType(XAddressParameterType xtendedAddressParameterType);
	
	/**
	 * <p>Returns true if this address number has extended parameter types.</p>
	 * 
	 * @return boolean
	 */
	public boolean hasExtendedAddressParameterTypes();
	
	/**
	 * <p>Returns true if the post office box exists.</p>
	 *
	 * @return boolean
	 */
	public boolean hasPostOfficebox();
	
	/**
	 * <p>Returns true if the extended address exists.</p>
	 *
	 * @return boolean
	 */
	public boolean hasExtendedAddress();
	
	/**
	 * <p>Returns true if the locality exists.</p>
	 *
	 * @return boolean
	 */
	public boolean hasLocality();
	
	/**
	 * <p>Returns true if the region exists.</p>
	 *
	 * @return boolean
	 */
	public boolean hasRegion();
	
	/**
	 * <p>Returns true if the postal code exists.</p>
	 *
	 * @return boolean
	 */
	public boolean hasPostalCode();
	
	/**
	 * <p>Returns true if the country name exits.</p>
	 *
	 * @return boolean
	 */
	public boolean hasCountryName();
	
	/**
	 * <p>Returns true if street address exists.</p>
	 *
	 * @return boolean
	 */
	public boolean hasStreetAddress();
	
	/**
	 * <p>Returns true if this address has parameter types.</p>
	 *
	 * @return boolean
	 */
	public boolean hasAddressParameterTypes();
	
	/**
	 * <p>Returns a full copy of this object.</p>
	 *
	 * @return {@link AddressFeature}
	 */
	public AddressFeature clone();
}
