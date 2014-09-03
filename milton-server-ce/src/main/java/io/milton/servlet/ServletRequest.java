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

import io.milton.http.AbstractRequest;
import io.milton.http.Auth;
import io.milton.http.BeanCookie;
import io.milton.http.Cookie;
import io.milton.http.Request;
import io.milton.http.RequestParseException;
import io.milton.http.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import io.milton.http.Response.ContentType;
import io.milton.servlet.upload.MonitoredDiskFileItemFactory;
import io.milton.servlet.upload.UploadListener;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Locale;
import javax.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletRequest extends AbstractRequest {

    private static final Logger log = LoggerFactory.getLogger(ServletRequest.class);
	
	public static BeanCookie toBeanCookie(javax.servlet.http.Cookie c) {
		BeanCookie bc = new BeanCookie(c.getName());
		bc.setDomain(c.getDomain());
		bc.setExpiry(c.getMaxAge());
		bc.setHttpOnly(true); // http only by default
		bc.setPath(c.getPath());
		bc.setSecure(c.getSecure());
		bc.setValue(c.getValue());
		bc.setVersion(c.getVersion());
		return bc;
	}	
	
    private final HttpServletRequest request;
    private final ServletContext servletContext;
    private final Request.Method method;
    private final String url;
    private Auth auth;
    private static final Map<ContentType, String> contentTypes = new EnumMap<ContentType, String>(ContentType.class);
    private static final Map<String, ContentType> typeContents = new HashMap<String, ContentType>();

    static {
        contentTypes.put(ContentType.HTTP, Response.HTTP);
        contentTypes.put(ContentType.MULTIPART, Response.MULTIPART);
        contentTypes.put(ContentType.XML, Response.XML);
        for (ContentType key : contentTypes.keySet()) {
            typeContents.put(contentTypes.get(key), key);
        }
    }
    private static ThreadLocal<HttpServletRequest> tlRequest = new ThreadLocal<HttpServletRequest>();
    private static ThreadLocal<ServletContext> tlServletContext = new ThreadLocal<ServletContext>();

    public static HttpServletRequest getRequest() {
        return tlRequest.get();
    }

    public static ServletContext getTLServletContext() {
        return tlServletContext.get();
    }

    static void clearThreadLocals() {
        tlRequest.remove();
        tlServletContext.remove();
    }

    public ServletRequest(HttpServletRequest r, ServletContext servletContext) {
        this.request = r;
        this.servletContext = servletContext;
        String sMethod = r.getMethod();
        method = Request.Method.valueOf(sMethod);
        String s = r.getRequestURL().toString(); //MiltonUtils.stripContext(r);
        url = s;
        tlRequest.set(r);
        tlServletContext.set(servletContext);
		
		if( log.isTraceEnabled()) {
			log.trace("Dumping headers ---- " + r.getMethod() + " " + r.getRequestURL() + " -----");
			log.trace("Request class: " + r.getClass());
			log.trace("Response class: " + r.getClass());
			Enumeration names = r.getHeaderNames();
			while( names.hasMoreElements() ) {
				String name = (String) names.nextElement();
				String value = r.getHeader(name);
				log.trace("  " + name + "=" + value);
			}
			log.trace("-------------------------------------------");
		}
    }

    public HttpSession getSession() {
        return request.getSession();
    }

    @Override
    public String getFromAddress() {
        return request.getRemoteHost();
    }

    @Override
    public String getRequestHeader(Request.Header header) {
        return request.getHeader(header.code);
    }

    @Override
    public Request.Method getMethod() {
        return method;
    }

    @Override
    public String getAbsoluteUrl() {
        return url;
    }

    @Override
    public Auth getAuthorization() {
        if (auth != null) {
            return auth;
        }
        String enc = getRequestHeader(Request.Header.AUTHORIZATION);
        if (enc == null) {
			log.trace("getAuthorization: No http credentials in request headers");
            return null;
        }
        if (enc.length() == 0) {
            log.trace("getAuthorization: No http credentials in request headers; authorization header is not-null, but is empty");
            return null;
        }
        auth = new Auth(enc);
        if (log.isTraceEnabled()) {
            log.trace("creating new auth object {}", auth.getScheme());
        }
        return auth;
    }

    @Override
    public void setAuthorization(Auth auth) {
        this.auth = auth;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return request.getInputStream();
    }

    @Override
    public void parseRequestParameters(Map<String, String> params, Map<String, io.milton.http.FileItem> files) throws RequestParseException {
        try {
            if (isMultiPart()) {
                log.trace("parseRequestParameters: isMultiPart");
                UploadListener listener = new UploadListener();
                MonitoredDiskFileItemFactory factory = new MonitoredDiskFileItemFactory(listener);
                ServletFileUpload upload = new ServletFileUpload(factory);
                List items = upload.parseRequest(request);

                parseQueryString(params);

                for (Object o : items) {
                    FileItem item = (FileItem) o;
                    if (item.isFormField()) {
                        params.put(item.getFieldName(), item.getString());
                    } else {
                        // See http://jira.ettrema.com:8080/browse/MIL-118 - ServletRequest#parseRequestParameters overwrites multiple file uploads when using input type="file" multiple="multiple"                        
                        String itemKey = item.getFieldName();
                        if (files.containsKey(itemKey)) {
                            int count = 1;
                            while (files.containsKey(itemKey + count)) {
                                count++;
                            }
                            itemKey = itemKey + count;
                        }
                        files.put(itemKey, new FileItemWrapper(item));
                    }
                }
            } else {
                for (Enumeration en = request.getParameterNames(); en.hasMoreElements();) {
                    String nm = (String) en.nextElement();
					String[] vals = request.getParameterValues(nm);
					if( vals.length == 1) {
						params.put(nm, vals[0]);
					} else {
						StringBuilder sb = new StringBuilder();
						for( String s : vals) {
							sb.append(s).append(",");
						}
						if( sb.length() > 0 ) {
							sb.deleteCharAt(sb.length()-1); // remove last comma
						}
						params.put(nm, sb.toString());
					}					
                }
            }
        } catch (FileUploadException ex) {
            throw new RequestParseException("FileUploadException", ex);
        } catch (Throwable ex) {
            throw new RequestParseException(ex.getMessage(), ex);
        }
    }

    private void parseQueryString(Map<String, String> map) {
        String qs = request.getQueryString();
        parseQueryString(map, qs);
    }

    public static void parseQueryString(Map<String, String> map, String qs) {
        if (qs == null) {
            return;
        }
        String[] nvs = qs.split("&");
        for (String nv : nvs) {
            String[] parts = nv.split("=");
            String key = parts[0];
            String val = null;
            if (parts.length > 1) {
                val = parts[1];
            }
            if (val != null) {
                try {
                    val = URLDecoder.decode(val, "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException(ex);
                }
            }
            map.put(key, val);
        }
    }

    protected Response.ContentType getRequestContentType() {
        String s = request.getContentType();
        log.trace("request content type", s);
        if (s == null) {
            return null;
        }
        if (s.contains(Response.MULTIPART)) {
            return ContentType.MULTIPART;
        }
        return typeContents.get(s);
    }

    protected boolean isMultiPart() {
        ContentType ct = getRequestContentType();
        log.trace("content type:", ct);
        return (ContentType.MULTIPART.equals(ct));
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> map = new HashMap<String, String>();
        Enumeration num = request.getHeaderNames();
        while (num.hasMoreElements()) {
            String name = (String) num.nextElement();
            String val = request.getHeader(name);
            map.put(name, val);
        }
        return map;
    }

    @Override
    public Cookie getCookie(String name) {
        if (request.getCookies() != null) {
            for (javax.servlet.http.Cookie c : request.getCookies()) {
                if (c.getName().equals(name)) {
                    return toBeanCookie(c);
                }
            }
        }
        return null;
    }

    @Override
    public List<Cookie> getCookies() {
        ArrayList<Cookie> list = new ArrayList<Cookie>();
        if (request.getCookies() != null) {
            for (javax.servlet.http.Cookie c : request.getCookies()) {
                list.add(toBeanCookie(c));

            }
        }
        return list;
    }
	
    @Override
    public String getRemoteAddr() {
        return request.getRemoteAddr();
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

	@Override
	public Locale getLocale() {
		return request.getLocale();
	}
	
	
}
