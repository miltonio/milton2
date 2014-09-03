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
