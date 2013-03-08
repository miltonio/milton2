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
