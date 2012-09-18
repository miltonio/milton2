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

package io.milton.http.webdav;

import io.milton.http.HrefStatus;
import io.milton.http.Request;
import io.milton.resource.Resource;
import io.milton.http.Response;
import io.milton.http.http11.Http11ResponseHandler;
import io.milton.http.quota.StorageChecker.StorageErrorReason;
import java.util.List;

/**
 *
 * @author brad
 */
public interface WebDavResponseHandler extends Http11ResponseHandler{
    void responseMultiStatus(Resource resource, Response response, Request request, List<HrefStatus> statii);

    /**
     * Generate the response for a PROPFIND or a PROPPATCH
     *
     * @param propFindResponses
     * @param response
     * @param request
     * @param r - the resource
     */
    void respondPropFind( List<PropFindResponse> propFindResponses, Response response, Request request, Resource r );

    void respondInsufficientStorage( Request request, Response response, StorageErrorReason storageErrorReason );

    void respondLocked( Request request, Response response, Resource existingResource );

    /**
     * Generate a 412 response, 
     * 
     * @param request
     * @param response
     * @param resource
     */
    void respondPreconditionFailed( Request request, Response response, Resource resource );
}
