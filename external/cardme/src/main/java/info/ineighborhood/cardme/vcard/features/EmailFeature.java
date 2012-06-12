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

import info.ineighborhood.cardme.vcard.types.parameters.EmailParameterType;
import info.ineighborhood.cardme.vcard.types.parameters.XEmailParameterType;
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
 * <b>3.3.2 EMAIL Type Definition</b>
 * <ul>
 * 	<li><b>Type name:</b> EMAIL</li>
 * 	<li><b>Type purpose:</b> To specify the electronic mail address for communication with the object the vCard represents.</li>
 * 	<li><b>Type encoding:</b> 8bit</li>
 * 	<li><b>Type value:</b> A single text value.</li>
 * 	<li><b>Type special note:</b> The type can include the type parameter "TYPE" to specify the format or preference of the electronic mail address. The TYPE parameter values can include: "internet" to indicate an Internet addressing type, "x400" to indicate a X.400 addressing type or "pref" to indicate a preferred-use email address when more than one is specified. Another IANA registered address type can also be specified. The default email type is "internet". A non-standard value can also be specified.</li>
 * </ul>
 * </p>
 */
public interface EmailFeature extends TypeTools {
	
	/**
	 * <p>Returns the email.</p>
	 *
	 * @return {@link String}
	 */
	public String getEmail();
	
	/**
	 * <p>Sets the email.</p>
	 *
	 * @param email
	 */
	public void setEmail(String email);
	
	/**
	 * <p>Returns an iterator of all parameter types.</p>
	 *
	 * @return {@link Iterator}&lt;EmailParameterType&gt;
	 */
	public Iterator<EmailParameterType> getEmailParameterTypes();
	
	/**
	 * <p>Returns an unmodifiable list of parameter types.</p>
	 *
	 * @return {@link List}&lt;EmailParameterType&gt;
	 */
	public List<EmailParameterType> getEmailParameterTypesList();
	
	/**
	 * <p>Returns the number of parameter types.</p>
	 *
	 * @return int
	 */
	public int getEmailParameterSize();
	
	/**
	 * <p>Adds a parameter type.</p>
	 *
	 * @param emailParameterType
	 */
	public void addEmailParameterType(EmailParameterType emailParameterType);
	
	/**
	 * <p>Removes a parameter type.</p>
	 *
	 * @param emailParameterType
	 */
	public void removeEmailParameterType(EmailParameterType emailParameterType);
	
	/**
	 * <p>Returns true if the parameter type exists.</p>
	 *
	 * @param emailParameterType
	 * @return boolean
	 */
	public boolean containsEmailParameterType(EmailParameterType emailParameterType);
	
	/**
	 * <p>Returns true if this email has parameter types.</p>
	 *
	 * @return boolean
	 */
	public boolean hasEmailParameterTypes();
	
	/**
	 * <p>Removes all email parameter types.</p>
	 */
	public void clearEmailParameterTypes();
	
	/**
	 * <p>Returns an iterator of extended email parameter types.</p>
	 * 
	 * @return {@link Iterator}&lt;XEmailParameterType&gt;
	 */
	public Iterator<XEmailParameterType> getExtendedEmailParameterTypes();
	
	/**
	 * <p>Returns an unmodifiable list of extended parameter types.</p>
	 *
	 * @return {@link List}&lt;EmailParameterType&gt;
	 */
	public List<XEmailParameterType> getExtendedEmailParameterTypesList();
	
	/**
	 * <p>Returns the number of extended parameter types.</p>
	 *
	 * @return int
	 */
	public int getExtendedEmailParameterSize();
	
	/**
	 * <p>Adds an extended email parameter type.</p>
	 * 
	 * @param xtendedEmailParameterType
	 */
	public void addExtendedEmailParameterType(XEmailParameterType xtendedEmailParameterType);
	
	/**
	 * <p>Removes the specified extended email parameter type.</p>
	 * 
	 * @param xtendedEmailParameterType
	 */
	public void removeExtendedEmailParameterType(XEmailParameterType xtendedEmailParameterType);
	
	/**
	 * <p>Returns true if the specified extended email parameter type exists.</p>
	 * 
	 * @param xtendedEmailParameterType
	 * @return boolean
	 */
	public boolean containsExtendedEmailParameterType(XEmailParameterType xtendedEmailParameterType);
	
	/**
	 * <p>Returns true if this email number has extended parameter types.</p>
	 * 
	 * @return boolean
	 */
	public boolean hasExtendedEmailParameterTypes();
	
	/**
	 * <p>Removes all extended email parameter types.</p>
	 */
	public void clearExtendedEmailParameterTypes();
	
	/**
	 * <p>Returns true if the email exists.</p>
	 *
	 * @return boolean
	 */
	public boolean hasEmail();
	
	/**
	 * <p>Returns a full copy of this object.</p>
	 *
	 * @return {@link EmailFeature}
	 */
	public EmailFeature clone();
}
