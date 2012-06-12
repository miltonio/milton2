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

package io.milton.davproxy.adapter;

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.http11.auth.DigestResponse;
import io.milton.resource.DigestResource;
import io.milton.resource.Resource;
import java.util.Date;

/**
 *
 * @author brad
 */
public abstract class AbstractRemoteAdapter implements Resource, DigestResource {

    private final io.milton.httpclient.Resource resource;
    private final io.milton.http.SecurityManager securityManager;
    private final String hostName;

    public AbstractRemoteAdapter(io.milton.httpclient.Resource resource, io.milton.http.SecurityManager securityManager, String hostName) {
        this.resource = resource;
        this.securityManager = securityManager;
        this.hostName = hostName;
    }

    @Override
    public String getName() {
        return resource.name;
    }

    @Override
    public String getUniqueId() {
        return null;
    }

    @Override
    public Object authenticate(String user, String password) {
        return securityManager.authenticate(user, password);
    }

    @Override
    public boolean authorise(Request request, Request.Method method, Auth auth) {
        return securityManager.authorise(request, method, auth, this);
    }

    @Override
    public String getRealm() {
        return securityManager.getRealm(hostName);
    }

    @Override
    public Date getModifiedDate() {
        return resource.getModifiedDate();
    }

    @Override
    public String checkRedirect(Request request) {
        if (request.getMethod().equals(Method.GET)) {
            String url = request.getAbsolutePath();
            if (!url.endsWith("/")) {
                return url + "/";
            }
        }
        return null;
    }

    public io.milton.http.SecurityManager getSecurityManager() {
        return securityManager;
    }
    
    @Override
    public Object authenticate(DigestResponse digestRequest) {
        return securityManager.authenticate(digestRequest);
    }

    @Override
    public boolean isDigestAllowed() {
        return true;
    }

    public String getHostName() {
        return hostName;
    }
    
    
}
