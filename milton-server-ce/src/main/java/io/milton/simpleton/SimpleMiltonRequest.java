/*
 *
 * Copyright 2014 McEvoy Software Ltd.
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

package io.milton.simpleton;

import io.milton.http.Request.Header;
import io.milton.http.Request.Method;
import io.milton.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.simpleframework.http.Address;
import org.simpleframework.http.Form;
import org.simpleframework.http.Part;
import org.simpleframework.http.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bradm (zfc1502)
 */
public class SimpleMiltonRequest extends AbstractRequest {

    private static final Logger log = LoggerFactory.getLogger(SimpleMiltonRequest.class);
    private final org.simpleframework.http.Request baseRequest;
    public final long started;
    private Auth auth;

    public SimpleMiltonRequest(Request baseRequest) {
        this.baseRequest = baseRequest;
        started = System.currentTimeMillis();
    }

    @Override
    public String getRequestHeader(Header header) {
        return baseRequest.getValue(header.code);
    }

    @Override
    public String getFromAddress() {
        Address add = baseRequest.getAddress();
        if (add == null) {
            return null;
        }
        return add.toString();
    }

    @Override
    public Method getMethod() {
        String s = baseRequest.getMethod().toUpperCase();
        try {
            return Method.valueOf(s);
        } catch (IllegalArgumentException e) {
            String ua = getUserAgentHeader();
            String ip = getRemoteAddr();
            throw new RuntimeException("No such method: " + s + " Requested by user-agent: " + ua + " from remote address: " + ip);
        }
    }

    @Override
    public Auth getAuthorization() {
        if (auth != null) {
            return auth;
        }
        String enc = getRequestHeader(Header.AUTHORIZATION);
        if (enc == null) {
            return null;
        }
        if (enc.length() == 0) {
            return null;
        }
        auth = new Auth(enc);
        return auth;
    }

	@Override
    public void setAuthorization(Auth auth) {
        this.auth = auth;
    }

    @Override
    public String getAbsoluteUrl() {
        String s = baseRequest.getTarget();
        // getTarget() java doc says it may contain full URI, but usually doesnt
        // note that a non-full uri will always start with a slash.
        if (s.startsWith("http")) {
            log.debug("target: " + s);
            return s;
        } else {
            String host = baseRequest.getValue("Host");
            Address a = baseRequest.getAddress();
            if (host == null) {
                host = a.getDomain();
            }

            if (baseRequest.isSecure()) {
                s = "https";
            } else {
                s = "http";
            }

            s = s + "://" + host;
            if (a.getPort() != 80 && a.getPort() > 0) {
                s = s + ":" + a.getPort();
            }
            s = s + baseRequest.getTarget();
//            s = s + a.getPath(); // note that this is unencoded, but milton expects absolute url to be encoded, eg raw
            return s;

        }
//        String s = baseRequest.getTarget();
//        s = "http://localhost:8088" + s;
//        return s;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            return baseRequest.getInputStream();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void parseRequestParameters(Map<String, String> params, Map<String, FileItem> files) throws RequestParseException {
        Map map = baseRequest.getQuery();
        params.putAll(map);
//        log.debug( "parseRequestParameters: query: " + params.size() );

        Form form;
        try {
            form = baseRequest.getForm();
        } catch (Exception ex) {
            throw new RequestParseException("", ex);
        }
        if (form == null) {
//            log.debug( "no form");
            return;
        }

        for (Entry<String, String> entry : form.entrySet()) {
            String nm = entry.getKey();
            String val = entry.getValue();
            params.put(nm, val);
        }
//        log.debug( "parseRequestParameters: form: " + params.size() );

        List<Part> list = form.getParts();
        for (Part part : list) {
            String name = part.getName();

            if (part.isFile()) {
                SimpleFileItem item = (SimpleFileItem) files.get(name);
                if (item == null) {
                    String filename = truncateFileName(getUserAgentHeader(), part.getFileName());;
                    item = new SimpleFileItem(name, part.getContentType().toString(), filename);
                    files.put(name, item);
                }
                item.addPart(part);
            }
        }
        for (FileItem item : files.values()) {
            SimpleFileItem sitem = (SimpleFileItem) item;
            sitem.finishedReadingRequest();
        }

    }

    /**
     * Used for parsing uploaded file names. MS web browsers tend to transmit the complete
     * path for an uploaded file, but we generally only want to know the last part of
     * the path.
     *
     * TODO: move this into milton
     *
     * @param s
     * @return
     */
    public static String truncateFileName(String agent, String s) {
        if (agent == null) {
            return s;
        } else {
            if (agent.contains("MSIE")) {
                if (s.contains("\\")) {
                    int pos = s.lastIndexOf("\\");
                    return s.substring(pos + 1);
                } else {
                    return s;
                }
            } else {
                return s;
            }
        }
    }

    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<String, String>();
        for (String s : baseRequest.getNames()) {
            String val = baseRequest.getValue(s);
            headers.put(s, val);
        }
        return headers;
    }

    public Cookie getCookie(String name) {
        for (org.simpleframework.http.Cookie c : baseRequest.getCookies()) {
            if (c.getName().equals(name)) {
                return new SimpletonCookie(c);
            }
        }
        return null;
    }

    public List<Cookie> getCookies() {
        ArrayList<Cookie> list = new ArrayList<Cookie>();
        for (org.simpleframework.http.Cookie c : baseRequest.getCookies()) {
            list.add(new SimpletonCookie(c));
        }
        return list;
    }

    public String getRemoteAddr() {
        InetSocketAddress add = baseRequest.getClientAddress();
        if (add == null) {
            return null;
        } else {
            return add.getHostName();
        }
    }
}
