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

import info.ineighborhood.cardme.vcard.types.parameters.LabelParameterType;
import info.ineighborhood.cardme.vcard.types.parameters.XLabelParameterType;
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
 * <b>3.2.2 LABEL Type Definition</b>
 * <ul>
 * 	<li><b>Type name:</b> LABEL</li>
 * 	<li><b>Type purpose:</b> To specify the formatted text corresponding to delivery address of the object the vCard represents.</li>
 * 	<li><b>Type encoding:</b> 8bit</li>
 * 	<li><b>Type value:</b> A single structured text value, separated by the SEMI-COLON character (ASCII decimal 59).</li>
 * 	<li><b>Type special note:</b> The type value is formatted text that can be used to present a delivery address label for the vCard object. The type can include the type parameter "TYPE" to specify delivery label type. The TYPE parameter values can include "dom" to indicate a domestic delivery label; "intl" to indicate an international delivery label; "postal" to indicate a postal delivery label; "parcel" to indicate a parcel delivery label; "home" to indicate a delivery label for a residence; "work" to indicate delivery label for a place of work; and "pref" to indicate the preferred delivery label when more than one label is specified. These type parameter values can be specified as a parameter list (i.e., "TYPE=dom;TYPE=postal") or as a value list (i.e., "TYPE=dom,postal"). This type is based on semantics of the X.520 geographical and postal addressing attributes. The default is "TYPE=intl,postal,parcel,work". The default can be overridden to some other set of values by specifying one or more alternate values. For example, the default can be reset to "TYPE=intl,post,parcel,home" to specify an international delivery label for both postal and parcel delivery to a residential location.</li>
 * </ul>
 * </p>
 */
public interface LabelFeature extends TypeTools {
	
	/**
	 * <p>Returns the label.</p>
	 *
	 * @return {@link String}
	 */
	public String getLabel();
	
	/**
	 * <p>Sets the label.</p>
	 *
	 * @param label
	 */
	public void setLabel(String label);
	
	/**
	 * <p>Returns an iterator of all parameter types for this label.</p>
	 *
	 * @return {@link Iterator}&lt;LabelParameterType&gt;
	 */
	public Iterator<LabelParameterType> getLabelParameterTypes();
	
	/**
	 * <p>Returns an unmodifiable list of parameter types.</p>
	 *
	 * @return {@link List}&lt;LabelParameterType&gt;
	 */
	public List<LabelParameterType> getLabelParameterTypesList();
	
	/**
	 * <p>Returns the number of parameter types.</p>
	 *
	 * @return int
	 */
	public int getLabelParameterSize();
	
	/**
	 * <p>Adds a parameter type.</p>
	 *
	 * @param labelParameterType
	 */
	public void addLabelParameterType(LabelParameterType labelParameterType);
	
	/**
	 * <p>Removes a parameter type.</p>
	 *
	 * @param labelParameterType
	 */
	public void removeLabelParameterType(LabelParameterType labelParameterType);
	
	/**
	 * <p>Returns true if the parameter types exists.</p>
	 *
	 * @param labelParameterType
	 * @return boolean
	 */
	public boolean containsLabelParameterType(LabelParameterType labelParameterType);
	
	/**
	 * <p>Returns true if this label has parameter types.</p>
	 *
	 * @return boolean
	 */
	public boolean hasLabelParameterTypes();
	
	/**
	 * <p>Removes all label parameter types.</p>
	 */
	public void clearLabelParameterTypes();
	
	/**
	 * <p>Returns an iterator of extended label parameter types.</p>
	 * 
	 * @return {@link Iterator}&lt;XLabelParameterType&gt;
	 */
	public Iterator<XLabelParameterType> getExtendedLabelParameterTypes();
	
	/**
	 * <p>Returns an unmodifiable list of extended parameter types.</p>
	 *
	 * @return {@link List}&lt;XLabelParameterType&gt;
	 */
	public List<XLabelParameterType> getExtendedLabelParameterTypesList();
	
	/**
	 * <p>Returns the number of extended parameter types.</p>
	 *
	 * @return int
	 */
	public int getExtendedLabelParameterSize();
	
	/**
	 * <p>Adds an extended label parameter type.</p>
	 * 
	 * @param xtendedLabelParameterType
	 */
	public void addExtendedLabelParameterType(XLabelParameterType xtendedLabelParameterType);
	
	/**
	 * <p>Removes the specified extended label parameter type.</p>
	 * 
	 * @param xtendedLabelParameterType
	 */
	public void removeExtendedLabelParameterType(XLabelParameterType xtendedLabelParameterType);
	
	/**
	 * <p>Returns true if the specified extended label parameter type exists.</p>
	 * 
	 * @param xtendedLabelParameterType
	 * @return boolean
	 */
	public boolean containsExtendedLabelParameterType(XLabelParameterType xtendedLabelParameterType);
	
	/**
	 * <p>Returns true if this label number has extended parameter types.</p>
	 * 
	 * @return boolean
	 */
	public boolean hasExtendedLabelParameterTypes();
	
	/**
	 * <p>Removes all extended label parameter types.</p>
	 */
	public void clearExtendedLabelParameterTypes();
	
	/**
	 * <p>Returns true if a label exists.</p>
	 *
	 * @return boolean
	 */
	public boolean hasLabel();
	
	/**
	 * <p>Returns a full copy of this object.</p>
	 *
	 * @return {@link LabelFeature}
	 */
	public LabelFeature clone();
}
