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

package io.milton.http.json;

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.http11.auth.DigestResponse;
import io.milton.resource.DigestResource;
import io.milton.resource.Resource;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class to contain common properties
 *
 * @author brad
 */
public abstract class JsonResource implements DigestResource {

    private static final Logger log = LoggerFactory.getLogger(JsonResource.class);
	
	public static String CONTENT_TYPE = "application/json; charset=utf-8";
	
    private final Resource wrappedResource;
    private final String name;
    private final Long maxAgeSecs;

    public abstract Method applicableMethod();

    public JsonResource(Resource wrappedResource, String name, Long maxAgeSecs) {
        this.wrappedResource = wrappedResource;
        this.name = name;
        this.maxAgeSecs = maxAgeSecs;
    }

    public Long getMaxAgeSeconds(Auth auth) {
        return maxAgeSecs;
    }

    public String getContentType(String accepts) {
		return CONTENT_TYPE;
        //String s = "application/x-javascript; charset=utf-8";
        //return s;
        //return "application/json";
    }

    public Long getContentLength() {
        return null;
    }

    @Override
    public String getUniqueId() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object authenticate(String user, String password) {
        if (log.isDebugEnabled()) {
            log.debug("authenticate: " + user);
        }
        Object o = wrappedResource.authenticate(user, password);
        if (log.isDebugEnabled()) {
            if (o == null) {
                log.debug("authentication failed on wrapped resource of type: " + wrappedResource.getClass());
            }
        }
        return o;
    }

    @Override
    public Object authenticate(DigestResponse digestRequest) {
        if (wrappedResource instanceof DigestResource) {
            return ((DigestResource) wrappedResource).authenticate(digestRequest);
        } else {
            return null;
        }
    }

    @Override
    public boolean isDigestAllowed() {
        return wrappedResource instanceof DigestResource;
    }

    @Override
    public boolean authorise(Request request, Method method, Auth auth) {
        boolean b = wrappedResource.authorise(request, applicableMethod(), auth);
        if (log.isDebugEnabled()) {
            if (!b) {
                log.trace("authorise failed on wrapped resource of type: " + wrappedResource.getClass());
            } else {
                log.trace("all ok");
            }
        }
        return b;
    }

    @Override
    public String getRealm() {
        return wrappedResource.getRealm();
    }

    @Override
    public Date getModifiedDate() {
        return null;
    }

    @Override
    public String checkRedirect(Request request) {
        return null;
    }

    public Resource getWrappedResource() {
        return wrappedResource;
    }
}
