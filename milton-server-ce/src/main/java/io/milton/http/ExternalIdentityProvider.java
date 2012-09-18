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

package io.milton.http;

import io.milton.http.Request;
import io.milton.resource.Resource;
import io.milton.http.Response;

/**
 * Represents a method of authenticating users using remote sites. This is generally
 * when the user agent must be redirected to a login form on another site, so is not applicable
 * for webdav user agents which don't support html web page interaction.
 * 
 * Examples are SAML and OpenID
 *
 * @author brad
 */
public interface ExternalIdentityProvider {
	/**
	 * This will identify the provider when the user selects it (if selection is required)
	 * 
	 * @return 
	 */
	String getName();

	/**
	 * Begin the external authentication process. This will usually involve redirecting
	 * the user's browser to a remote site.
	 * 
	 * @param resource
	 * @param request
	 * @param response 
	 */
	void initiateExternalAuth(Resource resource, Request request, Response response);
}
