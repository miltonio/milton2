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

package io.milton.http.fs;

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.resource.Resource;
import io.milton.http.http11.auth.DigestResponse;

/**
 *
 */
public class NullSecurityManager implements io.milton.http.SecurityManager {

    String realm;
    
    public Object authenticate(String user, String password) {
        return user;
    }

    public Object authenticate( DigestResponse digestRequest ) {
        return digestRequest.getUser();
    }



    public boolean authorise(Request request, Method method, Auth auth, Resource resource) {
        return true;
    }

    public String getRealm(String host) {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

	public boolean isDigestAllowed() {
		return true;
	}
	
	
}

