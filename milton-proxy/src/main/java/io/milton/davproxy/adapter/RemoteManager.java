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

import io.milton.http.SecurityManager;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.davproxy.content.FolderHtmlContentGenerator;
import io.milton.httpclient.Folder;
import io.milton.httpclient.HttpException;
import io.milton.resource.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class RemoteManager {

    private final io.milton.http.SecurityManager securityManager;
    private final FolderHtmlContentGenerator contentGenerator;

    public RemoteManager(SecurityManager securityManager, FolderHtmlContentGenerator contentGenerator) {
        this.securityManager = securityManager;
        this.contentGenerator = contentGenerator;
    }

    public List<? extends io.milton.resource.Resource> getChildren(String hostName, io.milton.httpclient.Folder folder) throws IOException, HttpException, NotAuthorizedException, BadRequestException {

        List<io.milton.resource.Resource> list = new ArrayList<Resource>();
        for (io.milton.httpclient.Resource r : folder.children()) {
            list.add(adapt(hostName, r));
        }
        return list;
    }

    public io.milton.resource.Resource adapt(String hostName, io.milton.httpclient.Resource remote) {
        if (remote instanceof io.milton.httpclient.Folder) {
            io.milton.httpclient.Folder f = (io.milton.httpclient.Folder) remote;
            return new FolderResourceAdapter(f, securityManager, hostName, contentGenerator, this);
        } else {
            io.milton.httpclient.File f = (io.milton.httpclient.File) remote;
            return new FileResourceAdapter(f, securityManager, hostName, this);
        }
    }

    public void copyTo(io.milton.httpclient.Resource remoteResource, String destName, Folder destRemoteFolder) throws RuntimeException, ConflictException, BadRequestException, NotAuthorizedException {
        try {
            if (destName.equals(remoteResource.name)) { // this is the normal case, copy with no rename                
                remoteResource.copyTo(destRemoteFolder);
            } else {    // its possible to request a copy with a new name
                remoteResource.copyTo(destRemoteFolder, destName);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        } catch (NotFoundException ex) {
            throw new BadRequestException("Remote resource does not exist", ex);
        }
    }

    public void moveTo(io.milton.httpclient.Resource remoteResource, String destName, Folder destRemoteFolder) throws NotAuthorizedException, ConflictException, BadRequestException {
        try {
            if (destName.equals(remoteResource.name)) { // this is the normal case, move with no rename                
                remoteResource.moveTo(destRemoteFolder);
            } else {    // its possible to request a copy with a new name
                remoteResource.moveTo(destRemoteFolder, destName);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        } catch (NotFoundException ex) {
            throw new BadRequestException("Remote resource does not exist", ex);
        }
    }
}
