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

import info.ineighborhood.cardme.vcard.types.parameters.TelephoneParameterType;
import info.ineighborhood.cardme.vcard.types.parameters.XTelephoneParameterType;
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
 * <p><b>RFC 2426</b><br/>
 * <b>3.3.1 TEL Type Definition</b>
 * <ul>
 * 	<li><b>Type name:</b> TEL</li>
 * 	<li><b>Type purpose:</b> To specify the telephone number for telephony communication with the object the vCard represents.</li>
 * 	<li><b>Type encoding:</b> 8bit</li>
 * 	<li><b>Type value:</b> A single phone-number value.</li>
 * 	<li><b>Type special note:</b> The value of this type is specified in a canonical form in order to specify an unambiguous representation of the globally unique telephone endpoint. This type is based on the X.500 Telephone Number attribute.</li>
 * </ul>
 * </p>
 */
public interface TelephoneFeature extends TypeTools {
	
	/**
	 * <p>Returns the telephone number.</p>
	 *
	 * @return {@link String}
	 */
	public String getTelephone();
	
	/**
	 * <p>Sets the telephone number.</p>
	 *
	 * @param telephone
	 */
	public void setTelephone(String telephone);
	
	/**
	 * <p>Returns an iterator of all parameter types for this telephone number.</p>
	 *
	 * @return {@link Iterator}&lt;TelephoneParameterType&gt;
	 */
	public Iterator<TelephoneParameterType> getTelephoneParameterTypes();
	
	/**
	 * <p>Returns an unmodifiable list of parameter types.</p>
	 *
	 * @return {@link List}&lt;TelephoneParameterType&gt;
	 */
	public List<TelephoneParameterType> getTelephoneParameterTypesList();
	
	/**
	 * <p>Returns the number of parameter types.</p>
	 *
	 * @return int
	 */
	public int getTelephoneParameterSize();
	
	/**
	 * <p>Adds a parameter type.</p>
	 *
	 * @param telephoneParameterType
	 */
	public void addTelephoneParameterType(TelephoneParameterType telephoneParameterType);
	
	/**
	 * <p>Removes a parameter type.</p>
	 *
	 * @param telephoneParameterType
	 */
	public void removeTelephoneParameterType(TelephoneParameterType telephoneParameterType);
	
	/**
	 * <p>Returns true if the parameter type exists.</p>
	 *
	 * @param telephoneParameterType
	 * @return boolean
	 */
	public boolean containsTelephoneParameterType(TelephoneParameterType telephoneParameterType);
	
	/**
	 * <p>Returns true if this telephone number has parameter types.</p>
	 *
	 * @return boolean
	 */
	public boolean hasTelephoneParameterTypes();
	
	/**
	 * <p>Removes all telephone parameter types.</p>
	 */
	public void clearTelephoneParameterTypes();
	
	/**
	 * <p>Returns a list of extended telephone parameter types.</p>
	 * 
	 * @return {@link Iterator}&lt;XTelephoneParameterType&gt;
	 */
	public Iterator<XTelephoneParameterType> getExtendedTelephoneParameterTypes();
	
	/**
	 * <p>Returns an unmodifiable list of extended parameter types.</p>
	 *
	 * @return {@link List}&lt;XTelephoneParameterType&gt;
	 */
	public List<XTelephoneParameterType> getExtendedTelephoneParameterTypesList();
	
	/**
	 * <p>Returns the number of extended parameter types.</p>
	 *
	 * @return int
	 */
	public int getExtendedTelephoneParameterSize();
	
	/**
	 * <p>Adds an extended telephone parameter type.</p>
	 * 
	 * @param xtendedTelephoneParameterType
	 */
	public void addExtendedTelephoneParameterType(XTelephoneParameterType xtendedTelephoneParameterType);
	
	/**
	 * <p>Removes the specified extended telephone parameter type.</p>
	 * 
	 * @param xtendedTelephoneParameterType
	 */
	public void removeExtendedTelephoneParameterType(XTelephoneParameterType xtendedTelephoneParameterType);
	
	/**
	 * <p>Returns true if the specified extended telephone parameter type exists.</p>
	 * 
	 * @param xtendedTelephoneParameterType
	 * @return boolean
	 */
	public boolean containsExtendedTelephoneParameterType(XTelephoneParameterType xtendedTelephoneParameterType);
	
	/**
	 * <p>Returns true if this telephone number has extended parameter types.</p>
	 * 
	 * @return boolean
	 */
	public boolean hasExtendedTelephoneParameterTypes();
	
	/**
	 * <p>Removes all extended telephone parameter types.</p>
	 */
	public void clearExtendedTelephoneParameterTypes();
	
	/**
	 * <p>Returns true if a telephone number exists.</p>
	 *
	 * @return boolean
	 */
	public boolean hasTelephone();
	
	/**
	 * <p>Returns a full copy of this object.</p>
	 *
	 * @return {@link TelephoneFeature}
	 */
	public TelephoneFeature clone();
}
