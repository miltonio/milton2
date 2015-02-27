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

    private String OAuth2Location;
    private String OAuth2ClientId;
    private String OAuth2RedirectURI;
    private String OAuth2ClientSecret;

    private String tokenLocation;
    private String userProfileLocation;
    private String OAuth2PermissionResponse;
    private OAuth2ProfileDetails oauthProfile;

    @Override
    public String getOAuth2ClientSecret() {
        return OAuth2ClientSecret;
    }

    public void setOAuth2ClientSecret(String OAuth2ClientSecret) {
        this.OAuth2ClientSecret = OAuth2ClientSecret;
    }

    @Override
    public String getOAuth2Location() {
        return OAuth2Location;
    }

    public void setOAuth2Location(String OAuth2Location) {
        this.OAuth2Location = OAuth2Location;
    }

    @Override
    public String getOAuth2ClientId() {
        return OAuth2ClientId;
    }

    public void setOAuth2ClientId(String OAuth2ClientId) {
        this.OAuth2ClientId = OAuth2ClientId;
    }

    @Override
    public String getOAuth2RedirectURI() {
        return OAuth2RedirectURI;
    }

    public void setOAuth2RedirectURI(String OAuth2RedirectURI) {
        this.OAuth2RedirectURI = OAuth2RedirectURI;
    }

//    public void setOAuth2Step(int OAuth2Step) {
//        this.OAuth2Step = OAuth2Step;
//    }

    public void setOAuth2PermissionResponse(String OAuth2PermissionResponse) {
        this.OAuth2PermissionResponse = OAuth2PermissionResponse;
    }

//    @Override
//    public int getOAuth2Step() {
//        return OAuth2Step;
//    }

    @Override
    public String getOAuth2PermissionResponse() {
        return OAuth2PermissionResponse;
    }

    @Override
    public boolean isOAuth2Authorized() {

        //TODO  
        //verify the authorization code which retrieved from the OAuth2.0 Server
        return false;
    }

    @Override
    public String getOAuth2TokenLocation() {
        return tokenLocation;
    }

    public void setOAuth2TokenLocation(String tokenLocation) {
        this.tokenLocation = tokenLocation;
    }

    public void setOAuth2UserProfileLocation(String userProfileLocation) {
        this.userProfileLocation = userProfileLocation;
    }

    @Override
    public String getOAuth2UserProfileLocation() {
        return this.userProfileLocation;
    }

    @Override
    public Object onAuthenticated(OAuth2ProfileDetails profile) {
        this.oauthProfile = profile;
        String profileId = getFirstOf(profile.getDetails(), "username", "user_id", "id");
        if( profileId != null ) {
            TCalDavPrincipal user = TResourceFactory.getUser(profileId);
            if( user == null ) {
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
    public Object getOAuth2TokenUser() {
        return this.oauthProfile;
    }

    private String getFirstOf(Map map, String ... names) {
        for( String s : names ) {
            Object o = map.get(s);
            if( o != null ) {
                return o.toString();
            }
        }
        return null;
    }
}
