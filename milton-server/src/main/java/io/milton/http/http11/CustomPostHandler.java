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

import io.milton.http.Request;
import io.milton.resource.Resource;
import io.milton.http.Response;

/**
 * Used for when we want to delegate POST handling to something other then the
 * usual processForm method.
 *
 * For example, this can be for handling POST requests to scheduling resources
 * with a content type of text/calendar, in which case we should perform
 * specific scheduling logic instead of artbitrary operations which
 * are usually implemented on POST requests
 *
 * @author brad
 */
public interface CustomPostHandler {
    boolean supports(Resource resource, Request request);

    void process(Resource resource, Request request, Response response);
}
