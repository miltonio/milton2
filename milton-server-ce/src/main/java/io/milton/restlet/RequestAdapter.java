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

import io.milton.http.AbstractRequest;
import io.milton.http.Auth;
import io.milton.http.Cookie;
import io.milton.http.Request.Header;
import io.milton.http.Request.Method;
import io.milton.http.RequestParseException;
import org.apache.commons.fileupload.FileItem;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.engine.adapter.HttpRequest;
import org.restlet.util.Series;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RequestAdapter extends AbstractRequest {

    final protected HttpRequest target;
    protected Auth auth;

    RequestAdapter(HttpRequest target) {
        this.target = target;
    }

    public HttpRequest getTarget() {
        return target;
    }

    protected Series<org.restlet.engine.header.Header> getRawHeaders() {
        return getTarget().getHttpCall().getRequestHeaders();
    }

    @Override
    public String getRequestHeader(Header header) {
        return getRawHeaders().getValues(header.code);
    }

    @Override
    public Map<String, String> getHeaders() {
        return getRawHeaders().getValuesMap();
    }

    @Override
    public String getFromAddress() {
        return getTarget().getClientInfo().getAddress();
    }

    @Override
    public String getRemoteAddr() {
        return getTarget().getClientInfo().getAddress();
    }

    @Override
    public Method getMethod() {
        return Method.valueOf(getTarget().getMethod().getName());
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
        return getTarget().getOriginalRef().toString();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        // Milton doesn't check for null InputStream, but there are some places where it checks for -1
        // Don't call target.isEntityAvailable(), doesn't work for PUT, see its source...
        if (getTarget().getEntity() != null && getTarget().getEntity().isAvailable()) {
            InputStream stream = getTarget().getEntity().getStream();
            if (stream != null)
                return stream;
        }
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
        };
    }

    @Override
    public void parseRequestParameters(Map<String, String> params, Map<String, io.milton.http.FileItem> files) throws RequestParseException {

        // GET params
        if (getTarget().getOriginalRef().hasQuery()) {
            params.putAll(
                    getTarget().getOriginalRef().getQueryAsForm(true).getValuesMap()
            );
        }

        // Again, don't use Request#isEntityAvailable(), it restricts methods
        if (getTarget().getEntity() != null && getTarget().getEntity().isAvailable()) {
            if (MediaType.APPLICATION_WWW_FORM.equals(getTarget().getEntity().getMediaType(), true)) {
                params.putAll(
                        new Form(getTarget().getEntity(), true).getValuesMap()
                );
            } else if (MediaType.MULTIPART_FORM_DATA.equals(getTarget().getEntity().getMediaType(), true)) {
                throw new UnsupportedOperationException("Multipart file uploading not implemented");
                /* TODO: Two reasons why this is not done:
                    1. it doesn't seem to work, but because we have no tests, we don't know
                    2. someone has to clean up the temp files, so we have to use background threads or something
                try {
                    RestletFileUpload fileUpload = new RestletFileUpload(new DiskFileItemFactory());
                    List<FileItem> fileItems = fileUpload.parseRepresentation(getTarget().getEntity());
                    for (FileItem fileItem : fileItems) {
                        if (fileItem.isFormField()) {
                            params.put(fileItem.getFieldName(), fileItem.getString());
                        } else {
                            files.put(fileItem.getFieldName(), new FileItemWrapper(fileItem));
                        }
                    }
                } catch (FileUploadException ex) {
                    throw new RequestParseException("FileUploadException", ex);
                } catch (Throwable ex) {
                    throw new RequestParseException(ex.getMessage(), ex);
                }
                 */
            }
        }
    }

    @Override
    public Cookie getCookie(String name) {
        if (getTarget().getCookies().size() > 0) {
            for (org.restlet.data.Cookie cookie : getTarget().getCookies()) {
                if (cookie.getName().equals(name)) {
                    return new CookieAdapter(cookie);
                }
            }
        }
        return null;
    }

    @Override
    public List<Cookie> getCookies() {
        List<Cookie> cookies = new ArrayList<Cookie>();
        for (org.restlet.data.Cookie cookie : getTarget().getCookies()) {
            cookies.add(new CookieAdapter(cookie));
        }
        return cookies;
    }

    // Copied from milton-servlet..
    public static class FileItemWrapper implements io.milton.http.FileItem {

        final FileItem wrapped;

        final String name;

        /**
         * strip path information provided by IE
         *
         * @param s
         * @return
         */
        public static String fixIEFileName(String s) {
            if (s.contains("\\")) {
                int pos = s.lastIndexOf('\\');
                s = s.substring(pos + 1);
            }
            return s;
        }

        public FileItemWrapper(FileItem wrapped) {
            this.wrapped = wrapped;
            name = fixIEFileName(wrapped.getName());
        }

        public String getContentType() {
            return wrapped.getContentType();
        }

        public String getFieldName() {
            return wrapped.getFieldName();
        }

        public InputStream getInputStream() {
            try {
                return wrapped.getInputStream();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        public OutputStream getOutputStream() {
            try {
                return wrapped.getOutputStream();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        public String getName() {
            return name;
        }

        public long getSize() {
            return wrapped.getSize();
        }
    }
}
