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
package io.milton.http.http11;

import io.milton.http.ExistingEntityHandler;
import io.milton.http.DeleteHelper;
import io.milton.http.HandlerHelper;
import io.milton.resource.Resource;
import io.milton.resource.DeletableResource;
import io.milton.http.ResourceHandlerHelper;
import io.milton.http.DeleteHelperImpl;
import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.exceptions.BadRequestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.milton.http.Request.Method;
import io.milton.http.Response;
import io.milton.http.Response.Status;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;

public class DeleteHandler implements ExistingEntityHandler {

	private final Logger log = LoggerFactory.getLogger(DeleteHandler.class);
	private final Http11ResponseHandler responseHandler;
	private final ResourceHandlerHelper resourceHandlerHelper;
	private DeleteHelper deleteHelper;

	public DeleteHandler(Http11ResponseHandler responseHandler, ResourceHandlerHelper resourceHandlerHelper, HandlerHelper handlerHelper) {
		this.responseHandler = responseHandler;
		this.resourceHandlerHelper = resourceHandlerHelper;
		deleteHelper = new DeleteHelperImpl(handlerHelper);
	}

	@Override
	public String[] getMethods() {
		return new String[]{Method.DELETE.code};
	}

	@Override
	public boolean isCompatible(Resource handler) {
		return (handler instanceof DeletableResource);
	}

	@Override
	public void process(HttpManager manager, Request request, Response response) throws NotAuthorizedException, ConflictException, BadRequestException {
		String url = request.getAbsoluteUrl();
		if (url.contains("#")) {
			// See http://www.ettrema.com:8080/browse/MIL-88
			// Litmus test thinks this is unsafe
			throw new BadRequestException(null, "Can't delete a resource with a # in the url");
		}
		resourceHandlerHelper.process(manager, request, response, this);
	}

	@Override
	public void processResource(HttpManager manager, Request request, Response response, Resource r) throws NotAuthorizedException, ConflictException, BadRequestException {
		if (resourceHandlerHelper.isNotCompatible(r, request.getMethod()) ) {
			log.debug("resource not compatible. Resource class: " + r.getClass() + " handler: " + getClass());
			responseHandler.respondMethodNotImplemented(r, response, request);
			return;
		}

		resourceHandlerHelper.processResource(manager, request, response, r, this);
	}

	@Override
	public void processExistingResource(HttpManager manager, Request request, Response response, Resource resource) throws NotAuthorizedException, BadRequestException, ConflictException {
		log.debug("DELETE: " + request.getAbsoluteUrl());

		DeletableResource r = (DeletableResource) resource;

		if (deleteHelper.isLockedOut(request, r)) {
			log.info("Could not delete. Is locked");
			responseHandler.respondDeleteFailed(request, response, r, Status.SC_LOCKED);
			return;
		}

		deleteHelper.delete(r, manager.getEventManager());
		log.debug("deleted ok");
		responseHandler.respondNoContent(resource, response, request);

	}

	public DeleteHelper getDeleteHelper() {
		return deleteHelper;
	}

	public void setDeleteHelper(DeleteHelper deleteHelper) {
		this.deleteHelper = deleteHelper;
	}
}
