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

package io.milton.servlet;

import io.milton.http.Cookie;

/**
 * Implementation of milton's cookie interface which just wraps a javax.servlet cookie
 *
 * The reference to the wrapped cookie is final, but the cookie itself is
 * mutable.
 *
 * @author brad
 */
public class ServletCookie implements Cookie {

    private final javax.servlet.http.Cookie cookie;

    public ServletCookie( javax.servlet.http.Cookie cookie ) {
        this.cookie = cookie;
    }

    public javax.servlet.http.Cookie getWrappedCookie() {
        return cookie;
    }



	@Override
    public int getVersion() {
        return cookie.getVersion();
    }

	@Override
    public void setVersion( int version ) {
        cookie.setVersion( version );
    }

	@Override
    public String getName() {
        return cookie.getName();
    }

	@Override
    public String getValue() {
        return cookie.getValue();
    }

	@Override
    public void setValue( String value ) {
        cookie.setValue( value );
    }

	@Override
    public boolean getSecure() {
        return cookie.getSecure();
    }

	@Override
    public void setSecure( boolean secure ) {
        cookie.setSecure( secure );
    }

	@Override
    public int getExpiry() {
        return cookie.getMaxAge();
    }

	@Override
    public void setExpiry( int expiry ) {
        cookie.setMaxAge( expiry );
    }

	@Override
    public String getPath() {
        return cookie.getPath();
    }

	@Override
    public void setPath( String path ) {
        cookie.setPath( path );
    }

	@Override
    public String getDomain() {
        return cookie.getDomain();
    }

	@Override
    public void setDomain( String domain ) {
        cookie.setDomain( domain );
    }

	@Override
	public void setHttpOnly(boolean b) {
		cookie.setHttpOnly(b);
	}

	@Override
	public boolean isHttpOnly() {
		return cookie.isHttpOnly();
	}

	
}
