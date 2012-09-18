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

package io.milton.http.fck;

import io.milton.resource.CollectionResource;
import io.milton.resource.DigestResource;
import io.milton.resource.PostableResource;
import io.milton.common.Path;
import io.milton.http.*;
import io.milton.http.http11.auth.DigestResponse;
import java.util.Date;

public abstract class FckCommon implements PostableResource, DigestResource {

    protected Path url;
    protected final CollectionResource wrappedResource;

    FckCommon( CollectionResource wrappedResource, Path url ) {
        this.wrappedResource = wrappedResource;
        this.url = url;
    }

    @Override
    public Long getMaxAgeSeconds( Auth auth ) {
        return null;
    }

    @Override
    public String getName() {
        return url.getName();
    }

    @Override
    public Object authenticate( String user, String password ) {
        return wrappedResource.authenticate( user, password );
    }

    @Override
    public Object authenticate( DigestResponse dr ) {
        if( wrappedResource instanceof DigestResource) {
            return ((DigestResource)wrappedResource).authenticate( dr );
        } else {
            return null;
        }
    }

    public boolean isDigestAllowed() {
        return wrappedResource instanceof DigestResource;
    }



    @Override
    public boolean authorise( Request request, Request.Method method, Auth auth ) {
        return auth != null;
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
    public Long getContentLength() {
        return null;
    }

    @Override
    public String checkRedirect( Request request ) {
        return null;
    }
}
