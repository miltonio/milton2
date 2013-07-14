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

package io.milton.http.caldav;

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.http11.auth.DigestResponse;
import io.milton.principal.CalDavPrincipal;
import io.milton.resource.CollectionResource;
import io.milton.resource.DigestResource;
import io.milton.resource.ReportableResource;
import java.util.Date;

/**
 * Common sub-interface for inbox and outbox scheduling resources
 *
 * @author brad
 */
public abstract class BaseSchedulingResource implements CollectionResource, ReportableResource, DigestResource {
    protected final CalDavPrincipal principal;
    protected final SchedulingResourceFactory schedulingResourceFactory;
    protected final CalendarSearchService calendarSearchService;

    public BaseSchedulingResource(CalDavPrincipal principal, SchedulingResourceFactory schedulingResourceFactory) {
        this.principal = principal;
        this.schedulingResourceFactory = schedulingResourceFactory;
        this.calendarSearchService = schedulingResourceFactory.getCalendarSearchService();
    }


    @Override
    public String getUniqueId() {
        return null;
    }

    @Override
    public Object authenticate(String user, String password) {
        return principal.authenticate(user, password);
    } 

    @Override
    public boolean authorise(Request request, Request.Method method, Auth auth) {
        return principal.authorise(request, method, auth);
    }

    @Override
    public String getRealm() {
        return principal.getRealm();
    }

    @Override
    public Date getModifiedDate() {
        return null;
    }

    @Override
    public String checkRedirect(Request request) throws NotAuthorizedException, BadRequestException {
        return null;
    }

    @Override
    public Object authenticate(DigestResponse digestRequest) {
        if( principal instanceof DigestResource) {
            DigestResource dr = (DigestResource) principal;
            return dr.authenticate(digestRequest);
        }
        return null;
    }

    @Override
    public boolean isDigestAllowed() {
        if( principal instanceof DigestResource) {
            DigestResource dr = (DigestResource) principal;
            return dr.isDigestAllowed();
        }
        return false;
    }
    
    
}
