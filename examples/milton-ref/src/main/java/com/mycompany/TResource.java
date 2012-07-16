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
package com.mycompany;

import io.milton.http.Auth;
import io.milton.http.LockInfo;
import io.milton.http.LockResult;
import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import io.milton.http.values.HrefList;
import io.milton.http.webdav.PropPatchHandler.Fields;
import io.milton.principal.Principal;
import io.milton.resource.AccessControlledResource.Priviledge;
import io.milton.resource.*;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public abstract class TResource extends AbstractResource implements GetableResource, PropFindableResource, DeletableResource, MoveableResource,
        CopyableResource, DigestResource, AccessControlledResource, LockableResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TResource.class);
    private LockToken currentLock;

    public TResource(TFolderResource parent, String name) {
        super(parent, name);
    }

    protected abstract Object clone(TFolderResource newParent, String newName);

    @Override
    public String getPrincipalURL() {
        TCalDavPrincipal user = getUser();
        if (user == null) {
            return null;
        } else {
            return user.getHref();
        }
    }

    @Override
    public HrefList getPrincipalCollectionHrefs() {
        return TResourceFactory.getPrincipalCollectionHrefs();
    }

    public String getHref() {
        if (parent == null) {
            return "";
        } else {
            String s = parent.getHref();
            if (!s.endsWith("/")) {
                s = s + "/";
            }
            s = s + name;
            if (this instanceof CollectionResource) {
                s = s + "/";
            }
            return s;
        }
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return (long) 10;
    }

    @Override
    public void moveTo(CollectionResource rDest, String name) {
        log.debug("moving..");
        TFolderResource d = (TFolderResource) rDest;
        this.parent.children.remove(this);
        this.parent = d;
        this.parent.children.add(this);
        this.name = name;
    }

    @Override
    public Date getCreateDate() {
        return createdDate;
    }

    @Override
    public void delete() {
        if (this.parent == null) {
            throw new RuntimeException("attempt to delete root");
        }

        if (this.parent.children == null) {
            throw new NullPointerException("children is null");
        }
        this.parent.children.remove(this);
    }

    @Override
    public void copyTo(CollectionResource toCollection, String destName) {
        System.out.println("COPY: " + parent.name + "/" + this.name + " --->>>" + toCollection.getName() + "/" + destName);
        TResource rClone;
        rClone = (TResource) this.clone((TFolderResource) toCollection, destName);
        rClone.name = destName;
    }

    public int compareTo(Resource o) {
        if (o instanceof TResource) {
            TResource res = (TResource) o;
            return this.getName().compareTo(res.getName());
        } else {
            return -1;
        }
    }

    /**
     * This is required for the PropPatchableResource interface, but should not
     * be implemented.
     *
     * Implement CustomPropertyResource or MultiNamespaceCustomPropertyResource
     * instead
     *
     * @param fields
     */
    public void setProperties(Fields fields) {
    }

    protected void print(PrintWriter printer, String s) {
        printer.print(s);
    }

    @Override
    public final LockResult lock(LockTimeout lockTimeout, LockInfo lockInfo) {
        log.trace("Lock : " + lockTimeout + " info : " + lockInfo + " on resource : " + getName() + " in : " + parent);
        LockToken token = new LockToken();
        token.info = lockInfo;
        token.timeout = LockTimeout.parseTimeout("30");
        token.tokenId = UUID.randomUUID().toString();
        currentLock = token;
        return LockResult.success(token);
    }

    @Override
    public final LockResult refreshLock(String tokenId) {
        log.trace("RefreshLock : " + tokenId + " on resource : " + getName() + " in : " + parent);
        //throw new UnsupportedOperationException("Not supported yet.");
        LockToken token = new LockToken();
        token.info = null;
        token.timeout = LockTimeout.parseTimeout("30");
        token.tokenId = currentLock.tokenId;
        currentLock = token;
        return LockResult.success(token);
    }

    @Override
    public void unlock(String arg0) {
        log.trace("UnLock : " + arg0 + " on resource : " + getName() + " in : " + parent);
        currentLock = null;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public final LockToken getCurrentLock() {
        log.trace("GetCurrentLock");
        return currentLock;
    }

    @Override
    public boolean isDigestAllowed() {
        return true;
    }

    @Override
    public Map<Principal, List<Priviledge>> getAccessControlList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Priviledge> getPriviledges(Auth auth) {
        List<Priviledge> priviledgesList = new ArrayList<Priviledge>();
        priviledgesList.add(Priviledge.READ);
        priviledgesList.add(Priviledge.READ_ACL);
        priviledgesList.add(Priviledge.READ_CURRENT_USER_PRIVILEDGE);
        priviledgesList.add(Priviledge.UNLOCK);
        priviledgesList.add(Priviledge.WRITE);
        priviledgesList.add(Priviledge.WRITE_ACL);

        return priviledgesList;
    }

    @Override
    public void setAccessControlList(Map<Principal, List<Priviledge>> privs) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
