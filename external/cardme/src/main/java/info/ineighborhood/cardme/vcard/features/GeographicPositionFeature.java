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
 * <b>3.4.2 GEO Type Definition</b>
 * <ul>
 * 	<li><b>Type name:</b> GEO</li>
 * 	<li><b>Type purpose:</b> To specify information related to the global positioning of the object the vCard represents.</li>
 * 	<li><b>Type encoding:</b> 8bit</li>
 * 	<li><b>Type value:</b> A single structured value consisting of two float values separated by the SEMI-COLON character (ASCII decimal 59).</li>
 * 	<li><b>Type special note:</b> This type specifies information related to the global position of the object associated with the vCard. The value specifies latitude and longitude, in that order (i.e., "LAT LON" ordering). The longitude represents the location east and west of the prime meridian as a positive or negative real number, respectively. The latitude represents the location north and south of the equator as a positive or negative real number, respectively. The longitude and latitude values MUST be specified as decimal degrees and should be specified to six decimal places. This will allow for granularity within a meter of the geographical position. The text components are separated by the SEMI-COLON character (ASCII decimal 59). The simple formula for converting degrees-minutes-seconds into decimal degrees is: decimal = degrees + minutes/60 + seconds/3600.</li>
 * </ul>
 * </p>
 */
public interface GeographicPositionFeature extends TypeTools {
	
	/**
	 * <p>Returns the longitude.</p>
	 *
	 * @return double
	 */
	public double getLongitude();
	
	/**
	 * <p>Returns the latitude.</p>
	 *
	 * @return double
	 */
	public double getLatitude();
	
	/**
	 * <p>Sets the longitude.</p>
	 *
	 * @param longitude
	 */
	public void setLongitude(double longitude);
	
	/**
	 * <p>Sets the longitude</p>
	 *
	 * @param longitude
	 */
	public void setLongitude(String longitude);
	
	/**
	 * <p>Sets the longitude. Decimal = degrees + minutes/60 + seconds/3600.</p>
	 *
	 * @param degrees
	 * @param minutes
	 * @param seconds
	 */
	public void setLongitude(double degrees, double minutes, double seconds);
	
	/**
	 * <p>Sets the latitude.</p>
	 *
	 * @param latitude
	 */
	public void setLatitude(double latitude);
	
	/**
	 * <p>Sets the latitude.</p>
	 *
	 * @param latitude
	 */
	public void setLatitude(String latitude);
	
	/**
	 * <p>Sets the latitude. Decimal = degrees + minutes/60 + seconds/3600.</p>
	 *
	 * @param degrees
	 * @param minutes
	 * @param seconds
	 */
	public void setLatitude(double degrees, double minutes, double seconds);
	
	/**
	 * <p>Returns a full copy of this object.</p>
	 *
	 * @return {@link GeographicPositionFeature}
	 */
	public GeographicPositionFeature clone();
}
