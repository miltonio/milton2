/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.milton.http;

import java.util.Date;

/**
 *
 * @author brad
 */
public class BeanCookie implements Cookie {

    /**
     * Formats the cookie in a suitable format for a SetCookie response
     *
     * @return
     */
    public static String toHeader(Cookie c) {
        return toHeader(c, System.currentTimeMillis());
    }
    public static String toHeader(Cookie c, long nowMs) {
        StringBuilder sb = new StringBuilder();
        sb.append(c.getName()).append("=").append(c.getValue());
        if (c.getDomain() != null && c.getDomain().length() > 0) {
            sb.append("; Domain=").append(c.getDomain());
        }
        if (c.getPath() != null && c.getPath().length() > 0) {
            sb.append("; Path=").append(c.getPath());
        }

        if (c.getExpiry() > 0) {
            long expiryMs = nowMs + (c.getExpiry() * 1000);
            Date date = new Date(expiryMs);
            String sDate = DateUtils.formatForCookieExpiry(date);
            sb.append("; Expires=").append(sDate);
        }
        if (c.getSecure()) {
            sb.append("; Secure");
        }
        if (c.isHttpOnly()) {
            sb.append("; HttpOnly");
        }
        return sb.toString();
    }
    private int version;
    private final String name;
    private String value;
    private boolean secure;
    private int expiry;
    private String domain;
    private String path;
    private boolean httpOnly;

    public BeanCookie(String name) {
        this.name = name;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    public boolean isSecure() {
        return secure;
    }

    @Override
    public boolean getSecure() {
        return secure;
    }

    @Override
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Override
    public int getExpiry() {
        return expiry;
    }

    @Override
    public void setExpiry(int expiry) {
        this.expiry = expiry;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    @Override
    public boolean isHttpOnly() {
        return httpOnly;
    }
}
