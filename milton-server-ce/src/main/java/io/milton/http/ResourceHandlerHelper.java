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

import io.milton.common.Path;
import io.milton.resource.Resource;
import io.milton.http.AuthenticationService.AuthStatus;
import io.milton.http.Request.Method;
import io.milton.http.Response.Status;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.http.http11.Http11ResponseHandler;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class ResourceHandlerHelper {

	private final static Logger log = LoggerFactory.getLogger(ResourceHandlerHelper.class);
	/**
	 * request attribute name for the parameters map
	 */
	public static final String ATT_NAME_PARAMS = "_params";
	/**
	 * request attribute name for the files map
	 */
	public static final String ATT_NAME_FILES = "_files";
	private final HandlerHelper handlerHelper;
	private final Http11ResponseHandler responseHandler; // set after construction
	private final UrlAdapter urlAdapter;
	private final AuthenticationService authenticationService;

	public ResourceHandlerHelper(HandlerHelper handlerHelper, UrlAdapter urlAdapter, Http11ResponseHandler responseHandler, AuthenticationService authenticationService) {
		if (handlerHelper == null) {
			throw new IllegalArgumentException("handlerHelper may not be null");
		}
		this.responseHandler = responseHandler;
		this.urlAdapter = urlAdapter;
		this.handlerHelper = handlerHelper;
		this.authenticationService = authenticationService;
	}

	public void process(HttpManager manager, Request request, Response response, ResourceHandler handler) throws NotAuthorizedException, ConflictException, BadRequestException {
		// need a linked hash map to preserve ordering of params
		Map<String, String> params = new LinkedHashMap<String, String>();

		Map<String, FileItem> files = new HashMap<String, FileItem>();

		try {
			request.parseRequestParameters(params, files);
		} catch (RequestParseException ex) {
			log.warn("exception parsing request. probably interrupted upload", ex);
			return;
		}
		request.getAttributes().put(ATT_NAME_PARAMS, params);
		request.getAttributes().put(ATT_NAME_FILES, files);

		if (!handlerHelper.checkExpects(responseHandler, request, response)) {
			return;
		}
		String host = request.getHostHeader();
		String url = urlAdapter.getUrl(request);
		//log.debug( "find resource: path: " + url + " host: " + host );
		Resource r = manager.getResourceFactory().getResource(host, url);

		if (r == null) {
			// If the request is anonymous we might not want to send a 404 for a couple of reasons
			// 1. MS Office 2010 requires a challenge from a HEAD request prior to PUT to create a new file
			// 2. Potentially unsafe, because an anonymous user could determine the existence (though not content) of certain files
			
			// But dont check on OPTIONS, because some clients need unauthenticated OPTIONS (i think)
			if (!request.getMethod().equals(Method.OPTIONS)) {
				if (!authenticationService.authenticateDetailsPresent(request)) {
					// Find first existing parent, and test if it allows read access
					Resource parent = findClosestParent(manager, host, url);
					if (parent != null) {
						// If its a write operation then definitely challenge, otherwise test if HEAD is permitted
						if (request.getMethod().isWrite) {
							throw new NotAuthorizedException("Authentication is required for write access", parent);
						} else {
							boolean allowsHead = parent.authorise(request, Method.HEAD, null);
							if (!allowsHead) {
								throw new NotAuthorizedException("Authentication is required for read access", parent);
							}
						}
					}
				}
			}

			responseHandler.respondNotFound(response, request);
			return;
		}
		handler.processResource(manager, request, response, r);
	}

	public void processResource(HttpManager manager, Request request, Response response, Resource resource, ExistingEntityHandler handler) throws NotAuthorizedException, ConflictException, BadRequestException {
		processResource(manager, request, response, resource, handler, false, null, null);
	}

	public void processResource(HttpManager manager, Request request, Response response, Resource resource, ExistingEntityHandler handler, Map<String, String> params, Map<String, FileItem> files) throws NotAuthorizedException, ConflictException, BadRequestException {
		processResource(manager, request, response, resource, handler, false, params, files);
	}

	public void processResource(HttpManager manager, Request request, Response response, Resource resource, ExistingEntityHandler handler, boolean allowRedirect, Map<String, String> params, Map<String, FileItem> files) throws NotAuthorizedException, ConflictException, BadRequestException {
		log.trace("processResource");
		long t = System.currentTimeMillis();
		try {
			manager.onProcessResourceStart(request, response, resource);

			if (handlerHelper.isNotCompatible(resource, request.getMethod()) || !handler.isCompatible(resource)) {
				if (log.isInfoEnabled()) {
					log.info("resource not compatible. Resource class: " + resource.getClass() + " handler: " + handler.getClass());
				}
				responseHandler.respondMethodNotImplemented(resource, response, request);
				return;
			}

			boolean authorised = handlerHelper.checkAuthorisation(manager, resource, request);

			// redirect check must be after authorisation, because the check redirect
			// logic might depend on logged in user
			// but the actual redirection must be before we respond unathorised, because
			// in some cases we might want to pre-empt the unauthorised status and redirect
			// to a login page
			if (allowRedirect) {
				log.trace("check redirect");
				if (handlerHelper.doCheckRedirect(responseHandler, request, response, resource)) {
					return;
				}
			}

			if (!authorised) {
				if (log.isInfoEnabled()) {
					log.info("authorisation failed. respond with: " + responseHandler.getClass().getCanonicalName() + " resource: " + resource.getClass().getCanonicalName());
				}
				responseHandler.respondUnauthorised(resource, response, request);
				return;
			}


			// Do not lock on POST requests. It is up to the application to decide whether or not
			// a POST requires a lock
			if (request.getMethod().isWrite && request.getMethod() != Method.POST) {
				if (handlerHelper.isLockedOut(request, resource)) {
					response.setStatus(Status.SC_LOCKED); // replace with responsehandler method
					return;
				}
			}
			try {
				handler.processExistingResource(manager, request, response, resource);
			} catch (NotFoundException ex) {
				log.warn("Not found exception thrown from handler: " + handler.getClass(), ex);
				responseHandler.respondNotFound(response, request);
			}
		} finally {
			t = System.currentTimeMillis() - t;
			manager.onProcessResourceFinish(request, response, resource, t);
		}
	}

	public boolean isNotCompatible(Resource r, Method m) {
		return handlerHelper.isNotCompatible(r, m);
	}

	public boolean isLockedOut(Request inRequest, Resource inResource) {
		return handlerHelper.isLockedOut(inRequest, inResource);
	}

	public AuthStatus checkAuthentication(HttpManager manager, Resource resource, Request request) {
		return handlerHelper.checkAuthentication(manager, resource, request);
	}

	public UrlAdapter getUrlAdapter() {
		return urlAdapter;
	}

	public Http11ResponseHandler getResponseHandler() {
		return responseHandler;
	}

	private Resource findClosestParent(HttpManager manager, String host, String url) throws NotAuthorizedException, BadRequestException {
		Path p = Path.path(url);
		return findClosestParent(manager, host, p);

	}

	private Resource findClosestParent(HttpManager manager, String host, Path p) throws NotAuthorizedException, BadRequestException {
		p = p.getParent();
		if (p == null) {
			return null;
		}
		Resource parent = manager.getResourceFactory().getResource(host, p.toString());
		if (parent != null) {
			return parent;
		}
		return findClosestParent(manager, host, p);
	}
}
