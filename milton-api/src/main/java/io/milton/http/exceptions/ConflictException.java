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

package io.milton.http.exceptions;

import io.milton.resource.Resource;

/**
 *  Indicates that the requested operation could not be performed because of
 * prior state. Ie there is an existing resource preventing a new one from being
 * created.
 */
public class ConflictException extends MiltonException {

	private final String message;

	/**
	 * The resource idenfitied by the URI.
	 *
	 * @param r
	 */
	public ConflictException(Resource r) {
		super(r);
		this.message = "Conflict exception: " + r.getName();
	}

	public ConflictException(Resource r, String message) {
		super(r);
		this.message = message;
	}

	public ConflictException() {
		this.message = "Conflict";
	}

	public ConflictException(String message) {
		this.message = message;
	}
	
	

	@Override
	public String getMessage() {
		return message;
	}
}
