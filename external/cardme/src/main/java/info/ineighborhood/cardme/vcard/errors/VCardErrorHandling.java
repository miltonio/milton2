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

package info.ineighborhood.cardme.vcard.errors;

import info.ineighborhood.cardme.vcard.ProblemSeverity;
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
 * @author Wolfgang Fahl
 * <br/>
 * Feb 5, 2010
 *
 */
public interface VCardErrorHandling {
	
	/**
	 * <p>Returns a list of errors that were gathered.</p>
	 *
	 * @return {@link List}&lt;VCardError&gt;
	 */
	public List<VCardError> getErrors();
	
	/**
	 * <p>Returns the problem severity of the last error that occurred.</p>
	 *
	 * @return {@link ProblemSeverity}
	 */
	public ProblemSeverity getProblemSeverity();
	
//	/**
//	 * <p>Sets the validity of this vcard.</p>
//	 *
//	 * @param valid
//	 * 	- true for this vcard to be valid
//	 */
//	public void setValid(boolean valid) ;
	
	/**
	 * <p>Returns true if this vcard is valid.</p>
	 *
	 * @return boolean
	 * 	- true if this vcard is valid
	 */
	public boolean isValid();
	
	/**
	 * <p>Set this vcard to throw exception on the event of an error,
	 * or silently add them to an error list if the flag is lowered.</p>
	 *
	 * @param throwExceptions
	 * 	- true to throw exceptions, false to silently add to a list
	 */
	public void setThrowExceptions(boolean throwExceptions);
	
	/**
	 * <p>Returns true if this vcard is set to throw exceptions on
	 * the event that an error occurs.</p>
	 *
	 * @return boolean
	 * 	- true if this vcard will throw exceptions
	 */
	public boolean isThrowExceptions();
	
	/**
	 * <p>Adds an error to this vcard.</p>
	 *
	 * @param error
	 * 	- the error to add
	 */
	public void addError(VCardError error);
	
	/**
	 * <p>Adds the parameters of the error to this vcard. A {@link VCardError} object
	 * gets constructed automatically from the parameters.</p>
	 *
	 * @param errorMessage
	 * 	- the error message
	 * @param severity
	 * 	- the severity of the error
	 * @param error
	 * 	- the exception that was caught
	 */
	public void addError(String errorMessage, ErrorSeverity severity, Throwable error);
	
	/**
	 * <p>Clears the error list.</p>
	 */
	public void clearErrors();
	
	/**
	 * <p>Returns true if the error list is not empty.</p>
	 *
	 * @return boolean
	 * 	- true if this vcard has errors
	 */
	public boolean hasErrors();
}
