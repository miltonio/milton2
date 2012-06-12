/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
