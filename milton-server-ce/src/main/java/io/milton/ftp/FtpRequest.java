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

package io.milton.ftp;

import io.milton.http.Request.Header;
import io.milton.http.Request.Method;
import io.milton.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fake Request object to be used for authorisation.
 *
 * @author brad
 */
public class FtpRequest extends AbstractRequest{
    private final Method method;
    private Auth auth;
    private final String url;

    public FtpRequest( Method method, Auth auth, String url ) {
        this.method = method;
        this.auth = auth;
        this.url = url;
    }
    

    @Override
    public String getRequestHeader( Header header ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Map<String, String> getHeaders() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public String getFromAddress() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Method getMethod() {
        return method;
    }

    public Auth getAuthorization() {
        return auth;
    }

    public void setAuthorization( Auth auth ) {
        this.auth = auth;
    }



    public String getAbsoluteUrl() {
        return url;
    }

    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void parseRequestParameters( Map<String, String> params, Map<String, FileItem> files ) throws RequestParseException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Cookie getCookie( String name ) {
        return null;
    }

    public List<Cookie> getCookies() {
        return new ArrayList<Cookie>();
    }

    public String getRemoteAddr() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

}
