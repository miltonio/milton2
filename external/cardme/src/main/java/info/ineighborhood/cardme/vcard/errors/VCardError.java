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

import info.ineighborhood.cardme.util.Util;

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
 * Feb 5, 2010
 *
 */
public class VCardError implements Cloneable {

	private String errorMessage = null;
	private ErrorSeverity severity = ErrorSeverity.NONE;
	private Throwable error = null;
	
	public VCardError() {
		
	}
	
	public VCardError(Throwable error, ErrorSeverity severity) {
		this(null, error, severity);
	}
	
	public VCardError(String errorMessage, Throwable error, ErrorSeverity severity) {
		setErrorMessage(errorMessage);
		setError(error);
		setSeverity(severity);
	}
	
	/**
	 * <p>Returns the user defined error message or the message that
	 * is present in the throwable object if the error message is null.</p>
	 *
	 * @return {@link String}
	 * 	- the error message
	 */
	public String getErrorMessage()
	{
		if(errorMessage == null) {
			return error.getMessage();
		}
		else {
			return errorMessage;
		}
	}
	
	/**
	 * <p>Returns the severity of the error.
	 * <ul>
	 * 	<li>FATAL</li>
	 * 	<li>WARNING</li>
	 * 	<li>NONE</li>
	 * </ul>
	 * </p>
	 *
	 * @see ErrorSeverity
	 * @return {@link ErrorSeverity}
	 * 	- the severity of the error
	 */
	public ErrorSeverity getSeverity()
	{
		return severity;
	}
	
	/**
	 * <p>Returns the throwable object that was caught as an exception.</p>
	 *
	 * @return {@link Throwable}
	 * 	- the error that was caught
	 */
	public Throwable getError()
	{
		return error;
	}
	
	/**
	 * <p>Sets the error message. If set to null then
	 * the error message from the throwable object will be used.</p>
	 *
	 * @param errorMessage
	 * 	- the error message
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	/**
	 * <p>Sets the error that occurred.</p>
	 *
	 * @param error
	 * 	- the exception that was caught
	 */
	public void setError(Throwable error) {
		this.error = error;
	}
	
	/**
	 * <p>Sets the severity of the error.</p>
	 *
	 * @see ErrorSeverity
	 * @param severity
	 * 	- the severity of the error
	 */
	public void setSeverity(ErrorSeverity severity) {
		this.severity = severity;
	}
	
	/**
	 * <p>Performs a java style equality with one extra bit of checking.
	 * In the end we check if the hash codes of both objects are equal.
	 * The hash code is determined by the overridden hash code function.</p>
	 * 
	 * @param obj
	 * @return boolean
	 */
	@Override
	public boolean equals(Object obj)
	{
		if(obj != null) {
			if(obj instanceof VCardError) {
				if(this == obj || ((VCardError)obj).hashCode() == this.hashCode()) {
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
	 * <p>Generates a unique hash code based on all the data
	 * contained within in the object.</p>
	 * 
	 * @see Util#generateHashCode(String...)
	 * @return int
	 */
	@Override
	public int hashCode()
	{
		return Util.generateHashCode(toString());
	}

	/**
	 * <p>Concatenates all data types in the object and returns it.</p>
	 * 
	 * @return {@link String}
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName());
		sb.append("[ ");
		
		if(errorMessage != null) {
			sb.append(errorMessage);
			sb.append(",");
		}
		
		sb.append(severity.toString());
		sb.append(",");
		
		if(error != null) {
			sb.append(error.getMessage());
			sb.append(",");
		}
		
		sb.deleteCharAt(sb.length()-1);	//Remove last comma.
		sb.append(" ]");
		return sb.toString();
	}
	
	/**
	 * <p>Returns a cloned copy of this object.</p>
	 * 
	 * @return {@link VCardError}
	 */
	@Override
	public VCardError clone()
	{
		VCardError cloned = new VCardError();
		
		if(errorMessage != null) {
			cloned.setErrorMessage(new String(errorMessage));
		}
		
		if(error != null) {
			cloned.setError(new Throwable(error));
		}
		
		cloned.setSeverity(severity);
		return cloned;
	}
}
