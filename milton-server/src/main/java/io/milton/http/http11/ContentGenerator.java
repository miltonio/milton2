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

import io.milton.http.AuthenticationService;
import io.milton.http.Request;
import io.milton.resource.Resource;
import io.milton.http.Response;
import io.milton.http.Response.Status;

/**
 * Used to generate error pages from ResponseHandlers.
 * 
 * Can be customised to produce custom pages, such as by including JSP's etc
 *
 * @author brad
 */
public interface ContentGenerator {
	/**
	 * Generate an error page for the given status
	 * 
	 * @param request
	 * @param response
	 * @param status 
	 */
	void generate(Resource resource, Request request, Response response, Status status);
	
	/**
	 * Generate content for a login page, generally when unauthorised
	 * 
	 * @param request
	 * @param response
	 * @param authenticationService 
	 */
	void generateLogin(Resource resource, Request request, Response response, AuthenticationService authenticationService);
}
