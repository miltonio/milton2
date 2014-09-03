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

package io.milton.ldap;

import io.milton.resource.LdapContact;
import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author brad
 */
public class MapContact extends HashMap<String, String> implements LdapContact {

	private final String userName;

	public MapContact(String id) {
		this.userName = id;
	}
	
		
	@Override
	public String getUniqueId() {
		return userName + hashCode();
	}

	@Override
	public Date getCreateDate() {
		return null;
	}

	@Override
	public String getName() {
		return userName;
	}

	@Override
	public Object authenticate(String user, String password) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean authorise(Request request, Method method, Auth auth) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getRealm() {
		return "ldapRealm";
	}

	@Override
	public Date getModifiedDate() {
		return null;
	}

	@Override
	public String checkRedirect(Request request) {
		return null;
	}
	
}
