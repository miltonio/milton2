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

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.http11.auth.DigestResponse;
import io.milton.resource.DigestResource;
import io.milton.resource.Resource;
import java.util.Date;

/**
 *
 * @author brad
 */
public abstract class AbstractRemoteAdapter implements Resource, DigestResource {

    private final io.milton.httpclient.Resource resource;
    private final io.milton.http.SecurityManager securityManager;
    private final String hostName;

    public AbstractRemoteAdapter(io.milton.httpclient.Resource resource, io.milton.http.SecurityManager securityManager, String hostName) {
        this.resource = resource;
        this.securityManager = securityManager;
        this.hostName = hostName;
    }

    @Override
    public String getName() {
        return resource.name;
    }

    @Override
    public String getUniqueId() {
        return null;
    }

    @Override
    public Object authenticate(String user, String password) {
        return securityManager.authenticate(user, password);
    }

    @Override
    public boolean authorise(Request request, Request.Method method, Auth auth) {
        return securityManager.authorise(request, method, auth, this);
    }

    @Override
    public String getRealm() {
        return securityManager.getRealm(hostName);
    }

    @Override
    public Date getModifiedDate() {
        return resource.getModifiedDate();
    }

    @Override
    public String checkRedirect(Request request) {
        if (request.getMethod().equals(Method.GET)) {
            String url = request.getAbsolutePath();
            if (!url.endsWith("/")) {
                return url + "/";
            }
        }
        return null;
    }

    public io.milton.http.SecurityManager getSecurityManager() {
        return securityManager;
    }
    
    @Override
    public Object authenticate(DigestResponse digestRequest) {
        return securityManager.authenticate(digestRequest);
    }

    @Override
    public boolean isDigestAllowed() {
        return true;
    }

    public String getHostName() {
        return hostName;
    }
    
    
}
