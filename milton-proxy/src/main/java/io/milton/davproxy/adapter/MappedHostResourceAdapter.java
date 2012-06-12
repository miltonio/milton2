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

import io.milton.http.*;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.davproxy.content.FolderHtmlContentGenerator;
import io.milton.httpclient.File;
import io.milton.httpclient.Folder;
import io.milton.httpclient.Host;
import io.milton.httpclient.HttpException;
import io.milton.httpclient.Resource;
import io.milton.resource.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Represents a remote DAV host which has been mapped onto the DAV proxy
 *
 * @author brad
 */
public class MappedHostResourceAdapter extends AbstractRemoteAdapter implements IFolderAdapter, CollectionResource, MakeCollectionableResource, PutableResource, GetableResource, PropFindableResource, DigestResource {

    private final io.milton.httpclient.Host remoteHost;
    private final FolderHtmlContentGenerator contentGenerator;
    private final RemoteManager remoteManager;
    private final String name;
    private final String hostName;

    public MappedHostResourceAdapter(String name, Host host, io.milton.http.SecurityManager securityManager, String hostName, FolderHtmlContentGenerator contentGenerator, RemoteManager remoteManager) {
        super(host, securityManager, hostName);
        this.contentGenerator = contentGenerator;
        this.remoteManager = remoteManager;
        this.remoteHost = host;
        this.name = name;
        this.hostName = hostName;
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException, BadRequestException {
        Folder newRemoteFolder;
        try {
            newRemoteFolder = remoteHost.createFolder(newName);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (NotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        }
        return new FolderResourceAdapter(newRemoteFolder, getSecurityManager(), newName, contentGenerator, remoteManager);
    }

    @Override
    public io.milton.resource.Resource child(String childName) throws NotAuthorizedException, BadRequestException {
        for( io.milton.resource.Resource r : getChildren() ) {
            if( r.getName().equals(childName)) {
                return r;
            }
        }
        return null;
    }

    @Override
    public List<? extends io.milton.resource.Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        try {
            return remoteManager.getChildren(hostName, remoteHost);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public io.milton.resource.Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        try {
            File newFile = remoteHost.upload(newName, inputStream, length, null);
            return new FileResourceAdapter(newFile, getSecurityManager(), hostName, remoteManager);
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        } catch (NotFoundException ex) {
            throw new RuntimeException(ex);
        }
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
    public Date getCreateDate() {
        return null;
    }

    public Folder getRemoteFolder() {
        return this.remoteHost;
    }
}

