/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
