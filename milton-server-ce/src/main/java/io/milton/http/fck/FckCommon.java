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
