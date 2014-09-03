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

	@Override
    public int getVersion() {
        return target.getVersion();
    }

	@Override
    public void setVersion(int version) {
        target.setVersion(version);
    }

	@Override
    public String getName() {
        return target.getName();
    }

	@Override
    public String getValue() {
        return target.getValue();
    }

	@Override
    public void setValue(String value) {
        target.setValue(value);
    }

	@Override
    public boolean getSecure() {
        return target.isSecure();
    }

	@Override
    public void setSecure(boolean secure) {
        target.setSecure(secure);
    }

	@Override
    public int getExpiry() {
        return target.getMaxAge();
    }

	@Override
    public void setExpiry(int expiry) {
        target.setMaxAge(expiry);
    }

	@Override
    public String getPath() {
        return target.getPath();
    }

	@Override
    public void setPath(String path) {
        target.setPath(path);
    }

	@Override
    public String getDomain() {
        return target.getDomain();
    }

	@Override
    public void setDomain(String domain) {
        target.setDomain(domain);
    }

	@Override
	public boolean isHttpOnly() {
		return false;
	}

	@Override
	public void setHttpOnly(boolean b) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
