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

import io.milton.http.Request.Method;
import io.milton.http.*;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.http.http11.auth.DigestResponse;
import io.milton.davproxy.content.FolderHtmlContentGenerator;
import io.milton.httpclient.Host;
import io.milton.resource.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author brad
 */
public class RootFolder implements CollectionResource, GetableResource, DigestResource, PropFindableResource {
    // This is the host name which this resource was resolved on. It is NOT the name
    // of any remote webdav host
    // This is the host name which this resource was resolved on. It is NOT the name
    // of any remote webdav host
    private final String hostName;
    /**
     * These are the root folders we will present to end users, which are the
     * remote webdav hosts being accessed.
     */
    /**
     * These are the root folders we will present to end users, which are the
     * remote webdav hosts being accessed.
     */
    private final Map<String, Host> roots;
    private final RemoteDavResourceFactory davResourceFactory;
    private final FolderHtmlContentGenerator contentGenerator;
    private final io.milton.http.SecurityManager securityManager;
    private final RemoteManager remoteManager;

    public RootFolder(String host, Map<String, Host> roots, final RemoteDavResourceFactory davResourceFactory, FolderHtmlContentGenerator contentGenerator, io.milton.http.SecurityManager securityManager, RemoteManager remoteManager) {
        this.davResourceFactory = davResourceFactory;
        this.contentGenerator = contentGenerator;
        this.securityManager = securityManager;
        this.remoteManager = remoteManager;
        this.hostName = host;
        this.roots = roots;
    }

    @Override
    public Resource child(String childName) {
        Host h = roots.get(childName);
        if (h == null) {
            return null;
        } else {
            return new MappedHostResourceAdapter(childName, h, securityManager, hostName, contentGenerator, remoteManager);
        }
    }

    @Override
    public List<? extends Resource> getChildren() {
        List<Resource> list = new ArrayList<Resource>();
        for (Entry<String, Host> root : roots.entrySet()) {
            MappedHostResourceAdapter mappedHost = new MappedHostResourceAdapter(root.getKey(), root.getValue(), securityManager, hostName, contentGenerator, remoteManager);
            list.add(mappedHost);
        }
        return list;
    }

    @Override
    public String getUniqueId() {
        return null;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public Object authenticate(String user, String password) {
        return securityManager.authenticate(user, password);
    }

    @Override
    public boolean authorise(Request request, Method method, Auth auth) {
        return securityManager.authorise(request, method, auth, this);
    }

    @Override
    public String getRealm() {
        return securityManager.getRealm(hostName);
    }

    @Override
    public Date getModifiedDate() {
        return null;
    }

    @Override
    public String checkRedirect(Request request) {
        return null;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        String uri = HttpManager.request().getAbsolutePath();
        contentGenerator.generateContent(this, out, uri);
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    @Override
    public String getContentType(String accepts) {
        return "text/html";
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public Object authenticate(DigestResponse digestRequest) {
        return securityManager.authenticate(digestRequest);
    }

    @Override
    public boolean isDigestAllowed() {
        return true;
    }

    @Override
    public Date getCreateDate() {
        return null;
    }
    
}
