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

package io.milton.servlet;

import io.milton.common.ContentTypeUtils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import io.milton.http.Auth;
import io.milton.http.LockToken;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.resource.GetableResource;
import io.milton.resource.Resource;


/**
 * Used to provide access to static files via Milton
 * 
 * For a full implementation of webdav on a filesystem use the milton-filesysten
 * project
 * 
 * @author brad
 */
public class StaticResource implements GetableResource {
    
    private final File file;
    private String contentType;
    
    public StaticResource(File file, String url, String contentType) {
        if( file.isDirectory() ) throw new IllegalArgumentException("Static resource must be a file, this is a directory: " + file.getAbsolutePath());
        this.file = file;
        this.contentType = contentType;
    }

	@Override
    public String getUniqueId() {
        return file.hashCode() + "";
    }
    
    public int compareTo(Resource res) {
        return this.getName().compareTo(res.getName());
    }    
    
	@Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bin = new BufferedInputStream(fis);
        final byte[] buffer = new byte[ 1024 ];
        int n = 0;
        while( -1 != (n = bin.read( buffer )) ) {
            out.write( buffer, 0, n );
        }        
    }

	@Override
    public String getName() {
        return file.getName();
    }

	@Override
    public Object authenticate(String user, String password) {
        return "ok";
    }

	@Override
    public boolean authorise(Request request, Request.Method method, Auth auth) {
        return true;
    }

    @Override
    public String getRealm() {
        return "ettrema";   //TODO
    }

	@Override
    public Date getModifiedDate() {        
        Date dt = new Date(file.lastModified());
//        log.debug("static resource modified: " + dt);
        return dt;
    }

	@Override
    public Long getContentLength() {
        return file.length();
    }

	@Override
    public String getContentType(String preferredList) {
		return ContentTypeUtils.findAcceptableContentTypeForName(getName(), contentType);
    }

    @Override
    public String checkRedirect(Request request) {
        return null;
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        Long ll = 315360000l; // immutable
        return ll;
    }

	public LockToken getLockToken()
	{
		return null;
	}

}
