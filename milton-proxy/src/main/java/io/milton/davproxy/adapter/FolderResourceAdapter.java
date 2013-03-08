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

import io.milton.http.*;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.davproxy.content.FolderHtmlContentGenerator;
import io.milton.httpclient.File;
import io.milton.httpclient.Folder;
import io.milton.httpclient.HttpException;
import io.milton.resource.CollectionResource;
import io.milton.resource.FolderResource;
import io.milton.resource.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author brad
 */
public class FolderResourceAdapter extends AbstractRemoteAdapter implements IFolderAdapter, FolderResource {

    private final io.milton.httpclient.Folder folder;

    private final FolderHtmlContentGenerator contentGenerator;
    
    private final RemoteManager remoteManager;
    
    public FolderResourceAdapter(Folder folder, io.milton.http.SecurityManager securityManager, String hostName, FolderHtmlContentGenerator contentGenerator, RemoteManager remoteManager) {
        super(folder, securityManager, hostName);
        this.folder = folder;
        this.contentGenerator = contentGenerator;
        this.remoteManager = remoteManager;
    }

    @Override
    public CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException, BadRequestException {
        Folder newRemoteFolder;
        try {
            newRemoteFolder = folder.createFolder(newName);
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
    public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
        for( Resource r : getChildren() ) {
            if( r.getName().equals(childName)) {
                return r;
            }
        }
        return null;
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        try {
            return remoteManager.getChildren(getHostName(), folder);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        }
    }


    @Override
    public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        try {
            File newFile = folder.upload(newName, inputStream, length, null);
            return new FileResourceAdapter(newFile, getSecurityManager(), getHostName(), remoteManager);
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        } catch (NotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }


    @Override
    public void copyTo(CollectionResource toCollection, String destName) throws NotAuthorizedException, BadRequestException, ConflictException {
        IFolderAdapter destFolderAdapter = (IFolderAdapter) toCollection;
        Folder destRemoteFolder = destFolderAdapter.getRemoteFolder();
        remoteManager.copyTo(folder, destName, destRemoteFolder);
    }

    @Override
    public void moveTo(CollectionResource toCollection, String destName) throws ConflictException, NotAuthorizedException, BadRequestException {
        IFolderAdapter destFolderAdapter = (IFolderAdapter) toCollection;
        Folder destRemoteFolder = destFolderAdapter.getRemoteFolder();
        remoteManager.moveTo(folder, destName, destRemoteFolder);
    }
    
    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
        String uri = HttpManager.request().getAbsolutePath();
        contentGenerator.generateContent(this, out, uri);
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    @Override
    public String getContentType(String accepts) {
        return null;
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public Date getCreateDate() {
        return folder.getCreatedDate();
    }
    
    @Override
    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
        try {
            folder.delete();
        } catch (NotFoundException ex) {
            return; // ok, not there to delete
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Folder getRemoteFolder() {
        return folder;
    }
    
    
}
