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

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractResponse implements Response {

    private static final Logger log = LoggerFactory.getLogger(AbstractResponse.class);
    protected Long contentLength;
    protected Entity entity;

    public AbstractResponse() {
    }

    public void setResponseHeader(Response.Header header, String value) {
        //log.debug("setResponseHeader: " + header.code + " - " + value);
        setNonStandardHeader(header.code, value);
    }

    public String getResponseHeader(Response.Header header) {
        return getNonStandardHeader(header.code);
    }

	@Override
    public void setContentEncodingHeader(ContentEncoding encoding) {
        setResponseHeader(Response.Header.CONTENT_ENCODING, encoding.code);
    }

	@Override
    public Long getContentLength() {
        return contentLength;
    }

	@Override
    public void setDateHeader(Date date) {
        setAnyDateHeader(Header.DATE, date);
    }

    // must now set multiple headers, which is dependent on the http connector
//    public void setAuthenticateHeader(String realm) {
//        setResponseHeader(Header.WWW_AUTHENTICATE, "Basic realm=\"" + realm + "\"");
//    }
	@Override
    public void setContentRangeHeader(long start, long finish, Long totalLength) {
        String l = totalLength == null ? "*" : totalLength.toString();

        String s = null;
        if (finish > -1) {
            s = "bytes " + start + "-" + finish + "/" + l;
        } else {
            long wrotetill = totalLength.longValue() - 1;
            //The end position starts counting at zero. So subtract 1
            s = "bytes " + start + "-" + wrotetill + "/" + l;
        }


        setResponseHeader(Header.CONTENT_RANGE, s);
    }

	@Override
    public void setContentLengthHeader(Long totalLength) {
        String s = totalLength == null ? "" : totalLength.toString();
        setResponseHeader(Header.CONTENT_LENGTH, s);
        this.contentLength = totalLength;

    }

	@Override
    public void setContentTypeHeader(String type) {
        setResponseHeader(Header.CONTENT_TYPE, type);
    }

	@Override
    public String getContentTypeHeader() {
        return getResponseHeader(Header.CONTENT_TYPE);
    }

	@Override
    public void setCacheControlMaxAgeHeader(Long delta) {
        if (delta != null) {
            setResponseHeader(Header.CACHE_CONTROL, CacheControlResponse.MAX_AGE.code + "=" + delta);
        } else {
            setResponseHeader(Header.CACHE_CONTROL, CacheControlResponse.NO_CACHE.code);
        }
    }

	@Override
    public void setCacheControlPrivateMaxAgeHeader(Long delta) {
        if (delta != null) {
            setResponseHeader(Header.CACHE_CONTROL, CacheControlResponse.PRIVATE.code + " " + CacheControlResponse.MAX_AGE.code + "=" + delta);
        } else {
            setResponseHeader(Header.CACHE_CONTROL, CacheControlResponse.PRIVATE.code);
        }
    }

	@Override
    public void setExpiresHeader(Date expiresAt) {
        if (expiresAt == null) {
            setResponseHeader(Header.EXPIRES, null);
        } else {
            setAnyDateHeader(Header.EXPIRES, expiresAt);
        }
    }

	@Override
    public void setEtag(String uniqueId) {
        setResponseHeader(Header.ETAG, uniqueId);
    }

	@Override
    public void setLastModifiedHeader(Date date) {
        setAnyDateHeader(Header.LAST_MODIFIED, date);
    }

	@Override
    public void setCacheControlNoCacheHeader() {
        setResponseHeader(Header.CACHE_CONTROL, CacheControlResponse.NO_CACHE.code);
    }

	@Override
    public void setLocationHeader(String redirectUrl) {
        setResponseHeader(Header.LOCATION, redirectUrl);
    }

	@Override
    public void setAllowHeader(List<String> methodsAllowed) {
        if (methodsAllowed == null || methodsAllowed.isEmpty()) {
            return;
        }
        StringBuilder sb = null;
        for (String m : methodsAllowed) {
            if (sb == null) {
                sb = new StringBuilder();
            } else {
                sb.append(",");
            }
            sb.append(m);
        }
        setResponseHeader(Header.ALLOW, sb.toString());
    }

	@Override
    public void setLockTokenHeader(String s) {
        setResponseHeader(Header.LOCK_TOKEN, s);
    }

	@Override
    public void setDavHeader(String supportedLevels) {
        setResponseHeader(Header.DAV, supportedLevels);
    }

	@Override
    public void setVaryHeader(String value) {
        setResponseHeader(Header.VARY, value);
    }

    @Override
    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

	@Override
    public void sendRedirect(String url) {
        if (log.isTraceEnabled()) {
            log.trace("sendRedirect: " + url);
        }
        setStatus(Response.Status.SC_MOVED_TEMPORARILY);
        setLocationHeader(url);
    }

    protected void setAnyDateHeader(Header name, Date date) {
        if (date == null) {
            return;
        }
        String fmt = DateUtils.formatForHeader(date);
        setResponseHeader(name, fmt);

    }
}
