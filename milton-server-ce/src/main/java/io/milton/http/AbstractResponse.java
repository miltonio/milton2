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

package io.milton.http;

import io.milton.common.RangeUtils;
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
		String s = RangeUtils.toRangeString(start, finish, totalLength);
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
            setResponseHeader(Header.CACHE_CONTROL, "public, " + CacheControlResponse.MAX_AGE.code + "=" + delta);
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
	public String getAcceptRanges() {
		return getResponseHeader(Header.ACCEPT_RANGES);
	}

	@Override
	public void setAcceptRanges(String s) {
		setResponseHeader(Header.ACCEPT_RANGES , s);
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
                sb.append(", ");
            }
            sb.append(m);
        }
        setResponseHeader(Header.ALLOW, sb == null ? "" : sb.toString());
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
	public String getAccessControlAllowOrigin() {
		return getResponseHeader(Header.ACCESS_CONTROL_ALLOW_ORIGIN);
	}

	@Override
	public void setAccessControlAllowOrigin(String s) {
		setResponseHeader(Header.ACCESS_CONTROL_ALLOW_ORIGIN, s);
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
