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

package io.milton.http;

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



    public int getVersion() {
        return cookie.getVersion();
    }

    public void setVersion( int version ) {
        cookie.setVersion( version );
    }

    public String getName() {
        return cookie.getName();
    }

    public String getValue() {
        return cookie.getValue();
    }

    public void setValue( String value ) {
        cookie.setValue( value );
    }

    public boolean getSecure() {
        return cookie.getSecure();
    }

    public void setSecure( boolean secure ) {
        cookie.setSecure( secure );
    }

    public int getExpiry() {
        return cookie.getMaxAge();
    }

    public void setExpiry( int expiry ) {
        cookie.setMaxAge( expiry );
    }

    public String getPath() {
        return cookie.getPath();
    }

    public void setPath( String path ) {
        cookie.setPath( path );
    }

    public String getDomain() {
        return cookie.getDomain();
    }

    public void setDomain( String domain ) {
        cookie.setDomain( domain );
    }

}
