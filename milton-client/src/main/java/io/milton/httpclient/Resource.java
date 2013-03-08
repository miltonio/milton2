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
import com.ettrema.cache.Cache;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mcevoyb
 */
public abstract class Resource {

    private static final Logger log = LoggerFactory.getLogger(Resource.class);

    static Resource fromResponse(Folder parent, PropFindResponse resp, Cache<Folder, List<Resource>> cache) {
        if (resp.isCollection()) {
            return new Folder(parent, resp, cache);
        } else {
            return new io.milton.httpclient.File(parent, resp);
        }
    }

    /**
     * does percentage decoding on a path portion of a url
     *
     * E.g. /foo > /foo /with%20space -> /with space
     *
     * @param href
     */
    public static String decodePath(String href) {
        // For IPv6
        href = href.replace("[", "%5B").replace("]", "%5D");

        // Seems that some client apps send spaces.. maybe..
        href = href.replace(" ", "%20");
        // ok, this is milton's bad. Older versions don't encode curly braces
        href = href.replace("{", "%7B").replace("}", "%7D");
        try {
            if (href.startsWith("/")) {
                URI uri = new URI("http://anything.com" + href);
                return uri.getPath();
            } else {
                URI uri = new URI("http://anything.com/" + href);
                String s = uri.getPath();
                return s.substring(1);
            }
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }
    public Folder parent;
    public String name;
    public String displayName;
    private Date modifiedDate;
    private Date createdDate;
    final List<ResourceListener> listeners = new ArrayList<ResourceListener>();
    private String lockOwner;
    private String lockToken;

    public abstract java.io.File downloadTo(java.io.File destFolder, ProgressListener listener) throws FileNotFoundException, IOException, HttpException, Utils.CancelledException, NotAuthorizedException, BadRequestException;
    private static long count = 0;

    public static long getCount() {
        return count;
    }

    public abstract String encodedUrl();

    /**
     * Special constructor for Host
     */
    Resource() {
        this.parent = null;
        this.name = "";
        this.displayName = "";
        this.createdDate = null;
        this.modifiedDate = null;
        count++;
    }

    public Resource(Folder parent, PropFindResponse resp) {
        count++;
        if (parent == null) {
            throw new NullPointerException("parent is null");
        }
        this.parent = parent;
        name = Resource.decodePath(resp.getName());
        displayName = resp.getDisplayName();
        createdDate = resp.getCreatedDate();
        modifiedDate = resp.getModifiedDate();
        if (resp.getLock() != null) {
            lockToken = resp.getLock().getToken();
            lockOwner = resp.getLock().getOwner();
        } else {
            lockToken = null;
            lockOwner = null;
        }
    }

    public Resource(Folder parent, String name, String displayName, String href, Date modifiedDate, Date createdDate) {
        count++;
        if (parent == null) {
            throw new NullPointerException("parent");
        }
        this.parent = parent;
        this.name = name;
        this.displayName = displayName;
        this.modifiedDate = modifiedDate;
        this.createdDate = createdDate;
    }

    public Resource(Folder parent, String name) {
        count++;
        if (parent == null) {
            throw new NullPointerException("parent");
        }
        this.parent = parent;
        this.name = name;
        this.displayName = name;
        this.modifiedDate = null;
        this.createdDate = null;
    }

    @Override
    protected void finalize() throws Throwable {
        count--;
        super.finalize();
    }

    public void addListener(ResourceListener l) {
        listeners.add(l);
    }

    public String post(Map<String, String> params) throws HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        return host().doPost(encodedUrl(), params);
    }

    public void lock() throws HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
      lock(-1);
    }

    public void lock(int timeout) throws HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        if (lockToken != null) {
            log.warn("already locked: " + href() + " token: " + lockToken);
        }
        try {
            lockToken = host().doLock(encodedUrl(), timeout);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    public int unlock() throws HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        if (lockToken == null) {
            throw new IllegalStateException("Can't unlock, is not currently locked (no lock token) - " + href());
        }
        try {
            return host().doUnLock(encodedUrl(), lockToken);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void copyTo(Folder folder) throws IOException, HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        copyTo(folder, name);
    }

    public void copyTo(Folder folder, String destName) throws IOException, HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        try {
            host().doCopy(encodedUrl(), folder.encodedUrl() + io.milton.common.Utils.percentEncode(destName));
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
        folder.flush();
    }

    public void rename(String newName) throws IOException, HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        String dest = "";
        if (parent != null) {
            dest = parent.encodedUrl();
        }
        dest = dest + io.milton.common.Utils.percentEncode(newName);
        int res;
        try {
            res = host().doMove(encodedUrl(), dest);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
        if (res == 201) {
            this.name = newName;
        }
    }

    public void moveTo(Folder folder) throws IOException, HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        moveTo(folder, name);
    }

    public void moveTo(Folder folder, String destName) throws IOException, HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        log.info("Move: " + this.href() + " to " + folder.href());
        int res;
        try {
            res = host().doMove(encodedUrl(), folder.href() + io.milton.common.Utils.percentEncode(destName));
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
        if (res == 201) {
            this.parent.flush();
            folder.flush();
        }
    }

    public void removeListener(ResourceListener l) {
        listeners.remove(l);
    }

    @Override
    public String toString() {
        return href() + "(" + displayName + ")";
    }

    public void delete() throws IOException, HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        host().doDelete(encodedUrl());
        notifyOnDelete();
    }

    void notifyOnDelete() {
        if (this.parent != null) {
            this.parent.notifyOnChildRemoved(this);
        }
        List<ResourceListener> l2 = new ArrayList<ResourceListener>(listeners);
        for (ResourceListener l : l2) {
            l.onDeleted(this);
        }
    }

    public Host host() {
        Host h = parent.host();
        if (h == null) {
            throw new NullPointerException("no host");
        }
        return h;
    }

    public String encodedName() {
        return io.milton.common.Utils.percentEncode(name);
    }

    /**
     * Returns the UN-encoded url
     *
     * @return
     */
    public String href() {
        if (parent == null) {
            return name;
            //return encodedName();
        } else {
            //return parent.href() + encodedName();
            return parent.href() + name;
        }
    }

    public Path path() {
        if (parent == null) {
            return Path.root;
            //return encodedName();
        } else {
            //return parent.href() + encodedName();
            return parent.path().child(name);
        }
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public String getLockToken() {
        return lockToken;
    }

    public String getLockOwner() {
        return lockOwner;
    }
}
