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
