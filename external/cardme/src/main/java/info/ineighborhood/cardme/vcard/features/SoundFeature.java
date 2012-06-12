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

import info.ineighborhood.cardme.vcard.EncodingType;
import info.ineighborhood.cardme.vcard.types.media.AudioMediaType;
import info.ineighborhood.cardme.vcard.types.parameters.SoundParameterType;
import java.net.URI;

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
 * <b>3.6.6 SOUND Type Definition</b>
 * <ul>
 * 	<li><b>Type name:</b> SOUND</li>
 * 	<li><b>Type purpose:</b> To specify a digital sound content information that annotates some aspect of the vCard. By default this type is used to specify the proper pronunciation of the name type value of the vCard.</li>
 * 	<li><b>Type encoding:</b> The encoding MUST be reset to "b" using the ENCODING parameter in order to specify inline, encoded binary data. If the value is referenced by a URI value, then the default encoding of 8bit is used and no explicit ENCODING parameter is needed.</li>
 * 	<li><b>Type value:</b> A single value. The default is binary value. It can also be reset to uri value. The uri value can be used to specify a value outside of this MIME entity.</li>
 * 	<li><b>Type special note:</b> The type can include the type parameter "TYPE" to specify the audio format type. The TYPE parameter values MUST be one of the IANA registered audio formats or a non-standard audio format.</li>
 * </ul>
 * </p>
 */
public interface SoundFeature extends TypeTools, TypeData {
	
	/**
	 * <p>Returns the sound as an array of bytes.</p>
	 *
	 * @return byte[]
	 */
	public byte[] getSound();
	
	/**
	 * <p>Returns the sound's URI.</p>
	 *
	 * @return {@link URI}
	 */
	public URI getSoundURI();
	
	/**
	 * <p>Returns this sound's encoding type.</p>
	 *
	 * @return {@link EncodingType}
	 */
	public EncodingType getEncodingType();
	
	/**
	 * <p>Returns the parameter type.</p>
	 *
	 * @return {@link SoundParameterType}
	 */
	public SoundParameterType getSoundParameterType();
	
	/**
	 * <p>Returns the format type.</p>
	 *
	 * @return {@link AudioMediaType}
	 */
	public AudioMediaType getAudioMediaType();
	
	/**
	 * <p>Sets the sound.</p>
	 *
	 * @param soundBytes
	 */
	public void setSound(byte[] soundBytes);
	
	/**
	 * <p>Sets the sound's URI.</p>
	 *
	 * @param soundUri
	 */
	public void setSoundURI(URI soundUri);
	
	/**
	 * <p>Returns true if a sound exists, URI or in-line data.</p>
	 *
	 * @return boolean
	 */
	public boolean hasSound();
	
	/**
	 * <p>Returns true if the sound has a URI.</p>
	 *
	 * @return boolean
	 */
	public boolean isURI();
	
	/**
	 * <p>Returns true if the sound has in-line data.</p>
	 *
	 * @return boolean
	 */
	public boolean isInline();
	
	/**
	 * <p>Sets the encoding type.</p>
	 *
	 * @param encodingType
	 */
	public void setEncodingType(EncodingType encodingType);
	
	/**
	 * <p>Sets the parameter type.</p>
	 *
	 * @param soundParameterType
	 */
	public void setSoundParameterType(SoundParameterType soundParameterType);
	
	/**
	 * <p>Sets the audio format.</p>
	 *
	 * @param audioMediaType
	 */
	public void setAudioMediaType(AudioMediaType audioMediaType);
	
	/**
	 * <p>Returns true if this sound has a parameter type.</p>
	 *
	 * @return boolean
	 */
	public boolean hasSoundParameterType();
	
	/**
	 * <p>Returns true if this sound has an audio format.</p>
	 *
	 * @return boolean
	 */
	public boolean hasAudioMediaType();
	
	/**
	 * <p>Returns a full copy of this object.</p>
	 *
	 * @return {@link SoundFeature}
	 */
	public SoundFeature clone();
}
