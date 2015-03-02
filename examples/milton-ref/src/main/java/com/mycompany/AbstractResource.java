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
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.http11.auth.DigestGenerator;
import io.milton.http.http11.auth.DigestResponse;
import io.milton.resource.DigestResource;
import io.milton.resource.OAuth2Provider;
import io.milton.resource.OAuth2Resource;
import io.milton.resource.ReportableResource;
import io.milton.resource.Resource;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * BM: added reportable so that all these resource classes work with REPORT
 *
 * @author alex
 */
public class AbstractResource implements Resource, ReportableResource, DigestResource, OAuth2Resource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractResource.class);
    protected UUID id;
    protected String name;
    protected Date modDate;
    protected Date createdDate;
    protected TFolderResource parent;

    public AbstractResource(TFolderResource parent, String name) {
        id = UUID.randomUUID();
        this.parent = parent;
        this.name = name;
        modDate = new Date();
        createdDate = new Date();
        if (parent != null) {
            checkAndRemove(parent, name);
            parent.children.add(this);
        }
    }

    TCalDavPrincipal getUser() {
        TFolderResource p = parent;
        while (p != null) {
            if (p instanceof TCalDavPrincipal) {
                return (TCalDavPrincipal) p;
            } else {
                p = p.parent;
            }
        }
        return null;
    }

    @Override
    public Object authenticate(String user, String requestedPassword) {
        TCalDavPrincipal p = TResourceFactory.findUser(user);
        if (p != null) {
            if (p.getPassword().equals(requestedPassword)) {
                return p;
            } else {
                log.warn("that password is incorrect. Try:" + p.getPassword());
            }
        } else {
            log.warn("user not found: " + user + " - try 'userA'");
        }
        return null;

    }

    @Override
    public Object authenticate(DigestResponse digestRequest) {
        TCalDavPrincipal p = TResourceFactory.findUser(digestRequest.getUser());
        if (p != null) {
            DigestGenerator gen = new DigestGenerator();
            String actual = gen.generateDigest(digestRequest, p.getPassword());
            if (actual.equals(digestRequest.getResponseDigest())) {
                return p;
            } else {
                log.warn("that password is incorrect. Try 'password'");
            }
        } else {
            log.warn("user not found: " + digestRequest.getUser() + " - try 'userA'");
        }
        return null;
    }

    @Override
    public Object authenticate(OAuth2ProfileDetails profile) {
        String profileId = getFirstOf(profile.getDetails(), "username", "user_id", "id");
        if (profileId != null) {
            TCalDavPrincipal user = TResourceFactory.getUser(profileId);
            if (user == null) {
                log.warn("Registering new user " + profileId);
                user = TResourceFactory.addUser(TResourceFactory.principals, profileId, null, name, "Anyorg", "");
            }
            return user;
        } else {
            log.warn("Could not get a userid from the response");
            return null;
        }
    }

    @Override
    public String getUniqueId() {
        return this.id.toString();
    }

    @Override
    public String checkRedirect(Request request) {
        return null;
    }

    @Override
    public String getName() {
        return name;
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
        return modDate;
    }

    private void checkAndRemove(TFolderResource parent, String name) {
        TResource r = (TResource) parent.child(name);
        if (r != null) {
            parent.children.remove(r);
        }
    }

    @Override
    public boolean isDigestAllowed() {
        return true;
    }

    @Override
    public Map<String, OAuth2Provider> getOAuth2Providers() {
        return TResourceFactory.mapOfOauthProviders;
    }

    private String getFirstOf(Map map, String... names) {
        for (String s : names) {
            Object o = map.get(s);
            if (o != null) {
                return o.toString();
            }
        }
        return null;
    }
}
