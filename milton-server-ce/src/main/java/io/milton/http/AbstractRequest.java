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

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRequest implements Request {

	private Logger log = LoggerFactory.getLogger(AbstractRequest.class);
	public static final int INFINITY = 3; // To limit tree browsing a bit

	@Override
	public abstract String getRequestHeader(Request.Header header);
	private final Map<String, Object> attributes = new HashMap<String, Object>();

	@Override
	public Date getIfModifiedHeader() {
		String s = getRequestHeader(Request.Header.IF_MODIFIED);
		if (s == null || s.length() == 0) {
			return null;
		}

		try {
			return DateUtils.parseDate(s);
		} catch (DateUtils.DateParseException ex) {
			log.error("Unable to parse date: " + s, ex);
			return null;
		}
	}

	@Override
	public String getIfRangeHeader() {
		return getRequestHeader(Header.IF_RANGE);
	}

	@Override
	public String getExpectHeader() {
		return getRequestHeader(Request.Header.EXPECT);
	}

	@Override
	public String getAcceptHeader() {
		return getRequestHeader(Request.Header.ACCEPT);
	}

	@Override
	public String getAcceptLanguage() {
		return getRequestHeader(Request.Header.ACCEPT_LANGUAGE);
	}

	@Override
	public String getRefererHeader() {
		return getRequestHeader(Request.Header.REFERER);
	}

	@Override
	public String getContentTypeHeader() {
		return getRequestHeader(Request.Header.CONTENT_TYPE);
	}

	@Override
	public String getAcceptEncodingHeader() {
		return getRequestHeader(Request.Header.ACCEPT_ENCODING);
	}

	@Override
	public String getUserAgentHeader() {
		return getRequestHeader(Header.USER_AGENT);
	}

	@Override
	public int getDepthHeader() {
		String depthStr = getRequestHeader(Request.Header.DEPTH);
		if (depthStr == null) {
			return INFINITY;
		} else {
			if (depthStr.equals("0")) {
				return 0;
			} else if (depthStr.equals("1")) {
				return 1;
			} else if (depthStr.equals("infinity")) {
				return INFINITY;
			} else {
				log.warn("Unknown depth value: " + depthStr);
				return INFINITY;
			}
		}
	}

	@Override
	public String getHostHeader() {
		return getRequestHeader(Header.HOST);
	}

	@Override
	public String getDestinationHeader() {
		return getRequestHeader(Header.DESTINATION);
	}

	@Override
	public Long getContentLengthHeader() {
		String s = getRequestHeader(Header.CONTENT_LENGTH);
		if (s == null || s.length() == 0) {
			return null;
		}
		try {
			long l = Long.parseLong(s);
			return l;
		} catch (NumberFormatException ex) {
			log.warn("Couldnt parse content length header: " + s);
			return null;
		}
	}

	@Override
	public String getTimeoutHeader() {
		return getRequestHeader(Header.TIMEOUT);
	}

	@Override
	public String getIfHeader() {
		return getRequestHeader(Header.IF);
	}

	@Override
	public String getLockTokenHeader() {
		return getRequestHeader(Header.LOCK_TOKEN);
	}

	@Override
	public String getRangeHeader() {
		return getRequestHeader(Header.RANGE);
	}

	@Override
	public String getContentRangeHeader() {
		return getRequestHeader(Header.CONTENT_RANGE);
	}

	@Override
	public Boolean getOverwriteHeader() {
		String s = getRequestHeader(Header.OVERWRITE);
		if (s == null || s.length() == 0) {
			return null;
		}
		return "T".equals(s);
	}

	@Override
	public String getIfMatchHeader() {
		return getRequestHeader(Header.IF_MATCH);
	}

	@Override
	public String getIfNoneMatchHeader() {
		return getRequestHeader(Header.IF_NONE_MATCH);
	}

	@Override
	public String getOriginHeader() {
		return getRequestHeader(Header.ORIGIN);
	}

	
	
	@Override
	public String getAbsolutePath() {
		return stripToPath(getAbsoluteUrl());
	}

	public static String stripToPath(String url) {
		int i = url.indexOf("/", 8);
		if (i > 0) {
			url = url.substring(i);
		}
		i = url.indexOf("?");
		if (i > 0) {
			url = url.substring(0, i);
		}
		return url;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public Map<String, String> getParams() {
		return (Map<String, String>) attributes.get("_params");
	}

	@Override
	public Map<String, FileItem> getFiles() {
		return (Map<String, FileItem>) attributes.get("_files");
	}

	@Override
	public Locale getLocale() {
		String header = getAcceptLanguage();
		if( header == null) {
			return null;
		}
		for (String str : header.split(",")) {
			String[] arr = str.trim().replace("-", "_").split(";");

			//Parse the locale
			Locale locale;
			String[] l = arr[0].split("_");
			switch (l.length) {
				case 2:
					locale = new Locale(l[0], l[1]);
					break;
				case 3:
					locale = new Locale(l[0], l[1], l[2]);
					break;
				default:
					locale = new Locale(l[0]);
					break;
			}

			//Parse the q-value
//			Double q = 1.0D;
//			for (String s : arr) {
//				s = s.trim();
//				if (s.startsWith("q=")) {
//					q = Double.parseDouble(s.substring(2).trim());
//					break;
//				}
//			}
			if( locale != null ) {
				return locale;
			}
		}
		return null;
	}
}
