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

package io.milton.http.http11;

import io.milton.http.Auth;
import io.milton.resource.GetableResource;
import io.milton.http.Response;

/**
 * Generates the cache-control header on the response
 *
 * @author brad
 */
public interface CacheControlHelper {
    /**
     *
     * @param resource
     * @param response
     * @param auth
     * @param notMod - true means we're sending a not modified response
     */
    void setCacheControl( final GetableResource resource, final Response response, Auth auth);
}
