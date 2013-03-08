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
package io.milton.httpclient;

import io.milton.common.Path;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import com.ettrema.cache.Cache;
import io.milton.common.LogUtils;
import io.milton.http.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mcevoyb
 */
public class Folder extends Resource {

    private static final Logger log = LoggerFactory.getLogger(Folder.class);
    final List<FolderListener> folderListeners = new ArrayList<FolderListener>();
    protected final Cache<Folder, List<Resource>> cache;

    /**
     * Special constructor for Host
     */
    Folder(Cache<Folder, List<Resource>> cache) {
        super();
        this.cache = cache;
        if (this.cache == null) {
            throw new IllegalArgumentException("cache cannot be null");
        }
    }

    public Folder(Folder parent, PropFindResponse resp, Cache<Folder, List<Resource>> cache) {
        super(parent, resp);
        this.cache = cache;
        if (this.cache == null) {
            throw new IllegalArgumentException("cache cannot be null");
        }
    }

    public Folder(Folder parent, String name, Cache<Folder, List<Resource>> cache) {
        super(parent, name);
        this.cache = cache;
        if (this.cache == null) {
            throw new IllegalArgumentException("cache cannot be null");
        }
    }

    public void addListener(FolderListener l) throws IOException, HttpException, NotAuthorizedException, BadRequestException {
        for (Resource r : this.children()) {
            l.onChildAdded(r.parent, r);
        }
        folderListeners.add(l);
    }

    /**
     *
     * @param relativePath - encoded but relative path. Must not start with a
     * slash
     * @param params
     * @return
     * @throws HttpException
     */
    public String post(String relativePath, Map<String, String> params) throws HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        return host().doPost(encodedUrl() + relativePath, params);
    }

    @Override
    public File downloadTo(File destFolder, ProgressListener listener) throws FileNotFoundException, IOException, HttpException, NotAuthorizedException, BadRequestException {
        File thisDir = new File(destFolder, this.name);
        thisDir.mkdir();
        for (Resource r : this.children()) {
            r.downloadTo(thisDir, listener);
        }
        return thisDir;
    }

    /**
     * Empty the cached children for this folder
     *
     * @throws IOException
     */
    public void flush() throws IOException {
        cache.remove(this);
    }

    public boolean hasChildren() throws IOException, HttpException, NotAuthorizedException, BadRequestException {
        return !children().isEmpty();
    }

    public int numChildren() throws IOException, HttpException, NotAuthorizedException, BadRequestException {
        return children().size();
    }

    public List<? extends Resource> children() throws IOException, HttpException, NotAuthorizedException, BadRequestException {
        List<Resource> children = cache.get(this);
        if (children == null) {
            children = new ArrayList<Resource>();
            String thisHref = href();
            if (log.isTraceEnabled()) {
                log.trace("load children for: " + thisHref);
            }
            List<PropFindResponse> responses = host()._doPropFind(encodedUrl(), 1, null);
            if (responses != null) {
                for (PropFindResponse resp : responses) {
                    try {
                        Resource r = Resource.fromResponse(this, resp, cache);
                        if (!r.href().equals(thisHref)) {
                            children.add(r);
                        }
                        this.notifyOnChildAdded(r);
                    } catch (Exception e) {
                        log.error("couldnt process record", e);
                    }

                }
            } else {
                log.trace("null responses");
            }

            cache.put(this, children);
        }

        return children;
    }

    public Resource getChild(int num) throws IOException, HttpException, NotAuthorizedException, BadRequestException {
        int x = 0;
        for (Resource r : children()) {
            if (x++ == num) {
                return r;
            }
        }
        return null;
    }

    public void removeListener(FolderListener folderListener) {
        this.folderListeners.remove(folderListener);
    }

    @Override
    public String toString() {
        return href() + " (is a folder)";
    }

    public void upload(File f) throws IOException, HttpException, NotAuthorizedException, ConflictException, BadRequestException, FileNotFoundException, NotFoundException {
        upload(f, null);
    }

    /**
     *
     * @param f
     * @param listener
     * @param throttle - optional, can be used to slow down the transfer
     * @throws IOException
     */
    public void upload(File f, ProgressListener listener) throws IOException, HttpException, NotAuthorizedException, ConflictException, BadRequestException, FileNotFoundException, NotFoundException {
        if (f.isDirectory()) {
            uploadFolder(f, listener);
        } else {
            uploadFile(f, listener);
        }
    }

    public io.milton.httpclient.File uploadFile(File f) throws FileNotFoundException, IOException, HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        return uploadFile(f, null);
    }

    /**
     * Load a new file into this folder, and return a reference
     *
     * @param f
     * @param listener
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws HttpException
     */
    public io.milton.httpclient.File uploadFile(File f, ProgressListener listener) throws FileNotFoundException, IOException, HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        return uploadFile(f.getName(), f, listener);
    }

    /**
     * Upload a new file
     *
     * @param newName
     * @param f
     * @param listener
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws HttpException
     * @throws NotAuthorizedException
     * @throws ConflictException
     * @throws BadRequestException
     * @throws NotFoundException
     */
    public io.milton.httpclient.File uploadFile(String newName, File f, ProgressListener listener) throws FileNotFoundException, IOException, HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        Path newPath = path().child(newName);
        children(); // ensure children are loaded
        HttpResult result = host().doPut(newPath, f, null, listener);
        int resultCode = result.getStatusCode();
        LogUtils.trace(log, "uploadFile", newPath, " result", resultCode);
        Utils.processResultCode(resultCode, newPath.toString());
        String newEtag = result.getHeaders().get(Response.Header.ETAG.code);
        io.milton.httpclient.File child = new io.milton.httpclient.File(this, newName, null, f.length(), newEtag);
        flush();
        notifyOnChildAdded(child);
        return child;
    }

    protected void uploadFolder(File folder, ProgressListener listener) throws IOException, HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        if (folder.getName().startsWith(".")) {
            return;
        }
        Folder newFolder = createFolder(folder.getName());
        for (File f : folder.listFiles()) {
            newFolder.upload(f, listener);
        }
    }

    public io.milton.httpclient.File upload(String name, InputStream content, Integer contentLength, ProgressListener listener) throws IOException, HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        Long length = null;
        if (contentLength != null) {
            long l = contentLength;
            length = l;
        }
        return upload(name, content, length, listener);
    }

    /**
     * Upload a new file
     *
     * @param name
     * @param content
     * @param contentLength
     * @param listener
     * @return
     * @throws IOException
     * @throws HttpException
     * @throws NotAuthorizedException
     * @throws ConflictException
     * @throws BadRequestException
     * @throws NotFoundException
     */
    public io.milton.httpclient.File upload(String name, InputStream content, Long contentLength, ProgressListener listener) throws IOException, HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        String contentType = URLConnection.guessContentTypeFromName(name);
        return upload(name, content, contentLength, contentType, null, listener);
    }

    /**
     *
     * @param name
     * @param content
     * @param contentLength
     * @param contentType
     * @param etag - the expected etag of the file being overwritten, or null if
     * expecting to create new
     * @param listener
     * @return
     * @throws IOException
     * @throws HttpException
     * @throws NotAuthorizedException
     * @throws ConflictException
     * @throws BadRequestException
     * @throws NotFoundException
     */
    public io.milton.httpclient.File upload(String name, InputStream content, Long contentLength, String contentType, IfMatchCheck matchCheck, ProgressListener listener) throws IOException, HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        children(); // ensure children are loaded
        String newUri = encodedUrl() + io.milton.common.Utils.percentEncode(name);
        log.trace("upload: " + newUri);
        HttpResult result = host().doPut(newUri, content, contentLength, contentType, matchCheck, listener);
        int resultCode = result.getStatusCode();
        Utils.processResultCode(resultCode, newUri);
        String newEtag = result.getHeaders().get(Response.Header.ETAG.code);
        io.milton.httpclient.File child = new io.milton.httpclient.File(this, name, contentType, contentLength, newEtag);
        io.milton.httpclient.Resource oldChild = this.child(child.name);
        flush();
        notifyOnChildAdded(child);
        return child;
    }

    public Folder createFolder(String name) throws IOException, HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        children(); // ensure children are loaded
        String newUri = encodedUrl() + io.milton.common.Utils.percentEncode(name);
        try {
            try {
                host().doMkCol(newUri);
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
            flush();
            Folder child = (Folder) child(name);
            notifyOnChildAdded(child);
            return child;
        } catch (ConflictException e) {
            return handlerCreateFolderException(newUri, name);
        } catch (MethodNotAllowedException e) {
            return handlerCreateFolderException(newUri, name);
        }
    }

    private Folder handlerCreateFolderException(String newUri, String name) throws IOException, HttpException, NotAuthorizedException, BadRequestException {
        // folder probably exists, so flush children
        this.flush();
        Resource child = this.child(name);
        if (child instanceof Folder) {
            Folder fChild = (Folder) child;
            return fChild;
        } else {
            if (child == null) {
                log.error("Couldnt create remote collection");
            } else {
                log.error("Remote resource exists and is not a collection");
            }
            throw new GenericHttpException(405, newUri);
        }

    }

    public Resource child(String childName) throws IOException, HttpException, NotAuthorizedException, BadRequestException {
//        log.trace( "child: current children: " + children().size());
        for (Resource r : children()) {
            if (r.name.equals(childName)) {
                return r;
            }
        }
        return null;
    }

    void notifyOnChildAdded(Resource child) {
        List<FolderListener> l2 = new ArrayList<FolderListener>(folderListeners); // defensive copy in case the folderListeners is changed by the listeners
        for (FolderListener l : l2) {
            l.onChildAdded(this, child);
        }
        // the list of children in the cache for this folder is no longer valid, so flush it
        cache.remove(this);
    }

    void notifyOnChildRemoved(Resource child) {
        List<FolderListener> l2 = new ArrayList<FolderListener>(folderListeners);// defensive copy in case the folderListeners is changed by the listeners
        for (FolderListener l : l2) {
            l.onChildRemoved(this, child);
        }
        // the list of children in the cache for this folder is no longer valid, so flush it
        cache.remove(this);
    }

    @Override
    public String href() {
        return super.href() + "/";
    }

    @Override
    public String encodedUrl() {
        return parent.encodedUrl() + encodedName() + "/";
    }
}
