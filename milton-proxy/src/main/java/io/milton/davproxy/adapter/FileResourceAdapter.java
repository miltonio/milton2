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

import io.milton.resource.ReplaceableResource;
import io.milton.http.*;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.httpclient.File;
import io.milton.httpclient.Folder;
import io.milton.httpclient.HttpException;
import io.milton.httpclient.Utils.CancelledException;
import io.milton.resource.CollectionResource;
import io.milton.resource.FileResource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

/**
 * Wraps a milton-client File object to adapt it for use as a milton server
 * resource
 *
 * @author brad
 */
public class FileResourceAdapter extends AbstractRemoteAdapter implements FileResource, ReplaceableResource {

    private final io.milton.httpclient.File file;
    
    private final RemoteManager remoteManager;

    public FileResourceAdapter(File file, io.milton.http.SecurityManager securityManager, String hostName, RemoteManager remoteManager) {
        super(file, securityManager, hostName);
        this.remoteManager = remoteManager;
        this.file = file;
    }

    @Override
    public void copyTo(CollectionResource toCollection, String destName) throws NotAuthorizedException, BadRequestException, ConflictException {
        IFolderAdapter destFolderAdapter = (IFolderAdapter) toCollection;
        Folder destRemoteFolder = destFolderAdapter.getRemoteFolder();
        remoteManager.copyTo(file, destName, destRemoteFolder);
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
        try {
            file.download(out, null);
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        } catch (CancelledException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    @Override
    public String getContentType(String accepts) {
        return file.contentType;
    }

    @Override
    public Long getContentLength() {
        return file.contentLength;
    }

    @Override
    public void moveTo(CollectionResource toCollection, String destName) throws ConflictException, NotAuthorizedException, BadRequestException {
        IFolderAdapter destFolderAdapter = (IFolderAdapter) toCollection;
        Folder destRemoteFolder = destFolderAdapter.getRemoteFolder();
        remoteManager.moveTo(file, destName, destRemoteFolder);
    }

    @Override
    public String processForm(Map<String, String> parameters, Map<String, FileItem> files) throws BadRequestException, NotAuthorizedException, ConflictException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Date getCreateDate() {
        return file.getCreatedDate();
    }

    @Override
    public void replaceContent(InputStream in, Long length) throws BadRequestException, ConflictException, NotAuthorizedException {
        try {
            file.setContent(in, length, null);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        } catch (NotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
        try {
            file.delete();
        } catch (NotFoundException ex) {
            // ok, not there to delete
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        }
    }
}
