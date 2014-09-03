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
package com.mycontacts.resource;

import com.mycontacts.model.ContactsDao;
import com.mycontacts.model.ContactsDao.User;
import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.http11.auth.DigestGenerator;
import io.milton.http.http11.auth.DigestResponse;
import io.milton.resource.CollectionResource;
import io.milton.resource.DigestResource;
import io.milton.resource.PropFindableResource;
import java.util.Date;

/**
 *
 *
 */
public abstract class AbstractResource implements DigestResource, PropFindableResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractResource.class);

    private AbstractResource parent;
    protected final ContactManager contactManager;
    
    public AbstractResource(ContactManager contactManager) {
        this.parent = null;
        this.contactManager = contactManager;
    }

    public AbstractResource(AbstractResource parent, ContactManager contactManager) {
        this.parent = parent;
        this.contactManager = contactManager;
    }
    
    
    @Override
    public Object authenticate(String userName, String requestedPassword) {
        User user = contactManager.getContactsDao().findUser(userName);
        if( user != null ) {
            if( user.getPassword().equals(requestedPassword)) {
                return getRoot().child(userName); // return the webdav resource for this object
            }
        }
        return null;
    }

    @Override
    public Object authenticate(DigestResponse digestRequest) {
        User user = contactManager.getContactsDao().findUser(digestRequest.getUser());
        if (user != null) {
            DigestGenerator gen = new DigestGenerator();
            String actual = gen.generateDigest(digestRequest, user.getPassword());
            if (actual.equals(digestRequest.getResponseDigest())) {
                return getRoot().child(digestRequest.getUser()); // return the webdav resource for this object
            } else {
                log.warn("that password is incorrect. Try '" + user.getPassword() + "'");
            }
        } else {
            log.warn("user not found: " + digestRequest.getUser() + " - try 'user'");
        }
        return null;
    }

    @Override
    public String getUniqueId() {
        return null;
    }

    @Override
    public String checkRedirect(Request request) {
        return null;
    }

    @Override
    public boolean authorise(Request request, Method method, Auth auth) {
        log.debug("authorise");
        return auth != null;
    }

    @Override
    public String getRealm() {
        return "testrealm@host.com";
    }

    @Override
    public Date getModifiedDate() {
        return null;
    }

    @Override
    public Date getCreateDate() {
        return null;
    }
       
    @Override
    public boolean isDigestAllowed() {
        return true;
    }
    
    public String getHref() {
        if( parent == null ) {
            return "/";
        } else {
            String href = parent.getHref() + getName();
            if( this instanceof CollectionResource) {
                href += "/";
            }
            return href;
        }
    }
    
    public RootResource getRoot() {
        if( this instanceof RootResource) {
            return (RootResource) this;
        } else {
            return parent.getRoot();
        }
    }
    
}
