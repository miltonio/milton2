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

import info.ineighborhood.cardme.vcard.types.parameters.TimeZoneParameterType;
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
 * Feb 4, 2010
 * 
 * <p><b>RFC 2426</b><br/>
 * <b>3.4.1 TZ Type Definition</b>
 * <ul>
 * 	<li><b>Type name:</b> TZ</li>
 * 	<li><b>Type purpose:</b> To specify information related to the time zone of the object the vCard represents.</li>
 * 	<li><b>Type encoding:</b> 8bit</li>
 * 	<li><b>Type value:</b> The default is a single utc-offset value. It can also be reset to a single text value.</li>
 * 	<li><b>Type special note:</b> The type value consists of a single value.</li>
 * </ul>
 * </p>
 */
public interface TimeZoneFeature extends TypeTools {
	
	/**
	 * <p>Returns the hour offset.</p>
	 *
	 * @return int
	 */
	public int getHourOffset();
	
	/**
	 * <p>Returns the minute offset.</p>
	 *
	 * @return int
	 */
	public int getMinuteOffset();
	
	/**
	 * <p>Returns the time zone.</p>
	 *
	 * @return {@link TimeZone}
	 */
	public TimeZone getTimeZone();
	
	/**
	 * <p>Returns the time zone in ISO-8601 format.</p>
	 *
	 * @return {@link String}
	 */
	public String getIso8601Offset();
	
	/**
	 * <p>Returns the time zone as a text value.</p>
	 *
	 * @return {@link String}
	 */
	public String getTextValue();
	
	/**
	 * <p>Returns the parameter type.</p>
	 *
	 * @return {@link TimeZoneParameterType}
	 */
	public TimeZoneParameterType getTimeZoneParameterType();
	
	/**
	 * <p>Sets the hour offset.</p>
	 *
	 * @param hourOffset
	 */
	public void setHourOffset(int hourOffset);
	
	/**
	 * <p>Sets the minute offset.</p>
	 *
	 * @param minuteOffset
	 */
	public void setMinuteOffset(int minuteOffset);
	
	/**
	 * <p>Sets the time zone</p>
	 *
	 * @param timeZone
	 */
	public void setTimeZone(TimeZone timeZone);
	
	/**
	 * <p>Sets the parameter type.</p>
	 *
	 * @param timeZoneParameterType
	 */
	public void setTimeZoneParameterType(TimeZoneParameterType timeZoneParameterType);
	
	/**
	 * <p>Parses a time zone in ISO-8601 format.</p>
	 *
	 * @param iso8601Offset
	 */
	public void parseTimeZoneOffset(String iso8601Offset);
	
	/**
	 * <p>Sets the time zone as a text value.</p>
	 *
	 * @param textValue
	 */
	public void setTextValue(String textValue);
	
	/**
	 * <p>Returns a full copy of this object.</p>
	 *
	 * @return {@link TimeZoneFeature}
	 */
	public TimeZoneFeature clone();
}
