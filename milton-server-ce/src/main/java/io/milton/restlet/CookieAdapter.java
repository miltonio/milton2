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

package io.milton.restlet;

import io.milton.http.Cookie;
import org.restlet.data.CookieSetting;

public class CookieAdapter implements Cookie {

    final protected CookieSetting target;

    public CookieAdapter(String name, String value) {
        target = new CookieSetting(name, value);
    }

    public CookieAdapter(Cookie cookie) {
        target = new CookieSetting(
           cookie.getVersion(),
           cookie.getName(),
           cookie.getValue(),
           cookie.getPath(),
           cookie.getDomain(),
           null,
           cookie.getExpiry(),
           cookie.getSecure(),
           false
        );
    }

    public CookieAdapter(org.restlet.data.Cookie target) {
        this.target = new CookieSetting(
           target.getVersion(),
           target.getName(),
           target.getValue(),
           target.getPath(),
           target.getDomain()
        );
    }

    public CookieSetting getTarget() {
        return target;
    }

    public int getVersion() {
        return target.getVersion();
    }

    public void setVersion(int version) {
        target.setVersion(version);
    }

    public String getName() {
        return target.getName();
    }

    public String getValue() {
        return target.getValue();
    }

    public void setValue(String value) {
        target.setValue(value);
    }

    public boolean getSecure() {
        return target.isSecure();
    }

    public void setSecure(boolean secure) {
        target.setSecure(secure);
    }

    public int getExpiry() {
        return target.getMaxAge();
    }

    public void setExpiry(int expiry) {
        target.setMaxAge(expiry);
    }

    public String getPath() {
        return target.getPath();
    }

    public void setPath(String path) {
        target.setPath(path);
    }

    public String getDomain() {
        return target.getDomain();
    }

    public void setDomain(String domain) {
        target.setDomain(domain);
    }

}
