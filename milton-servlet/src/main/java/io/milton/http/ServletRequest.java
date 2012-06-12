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
import io.milton.http.upload.MonitoredDiskFileItemFactory;
import io.milton.http.upload.UploadListener;
import java.util.ArrayList;
import java.util.EnumMap;
import javax.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletRequest extends AbstractRequest {

    private static final Logger log = LoggerFactory.getLogger(ServletRequest.class);
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
            log.trace("using cached auth object");
            return auth;
        }
        String enc = getRequestHeader(Request.Header.AUTHORIZATION);
        if (enc == null) {
            return null;
        }
        if (enc.length() == 0) {
            log.trace("authorization header is not-null, but is empty");
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
                    String val = request.getParameter(nm);
                    params.put(nm, val);
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
                    return new ServletCookie(c);
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
                list.add(new ServletCookie(c));

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
}
