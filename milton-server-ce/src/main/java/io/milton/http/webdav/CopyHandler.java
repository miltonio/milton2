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
package io.milton.http.webdav;

import io.milton.http.HttpManager;
import io.milton.resource.Resource;
import io.milton.http.Response;
import io.milton.http.DeleteHelperImpl;
import io.milton.resource.CopyableResource;
import io.milton.resource.CollectionResource;
import io.milton.http.ExistingEntityHandler;
import io.milton.resource.DeletableResource;
import io.milton.http.HandlerHelper;
import io.milton.http.ResourceHandlerHelper;
import io.milton.common.Utils;
import io.milton.http.DeleteHelper;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CopyHandler implements ExistingEntityHandler {

	private Logger log = LoggerFactory.getLogger(CopyHandler.class);
	private final WebDavResponseHandler responseHandler;
	private final HandlerHelper handlerHelper;
	private final ResourceHandlerHelper resourceHandlerHelper;
	private final UserAgentHelper userAgentHelper;
	private DeleteHelper deleteHelper;
	private boolean deleteExistingBeforeCopy = true;

	public CopyHandler(WebDavResponseHandler responseHandler, HandlerHelper handlerHelper, ResourceHandlerHelper resourceHandlerHelper, UserAgentHelper userAgentHelper) {
		this.userAgentHelper = userAgentHelper;
		this.responseHandler = responseHandler;
		this.handlerHelper = handlerHelper;
		this.resourceHandlerHelper = resourceHandlerHelper;
		this.deleteHelper = new DeleteHelperImpl(handlerHelper);
	}

	@Override
	public String[] getMethods() {
		return new String[]{Method.COPY.code};
	}

	@Override
	public boolean isCompatible(Resource handler) {
		return (handler instanceof CopyableResource);
	}

	@Override
	public void processResource(HttpManager manager, Request request, Response response, Resource r) throws NotAuthorizedException, ConflictException, BadRequestException {
		resourceHandlerHelper.processResource(manager, request, response, r, this);
	}

	@Override
	public void process(HttpManager httpManager, Request request, Response response) throws ConflictException, NotAuthorizedException, BadRequestException {
		resourceHandlerHelper.process(httpManager, request, response, this);
	}

	@Override
	public void processExistingResource(HttpManager manager, Request request, Response response, Resource resource) throws NotAuthorizedException, BadRequestException, ConflictException {
		CopyableResource r = (CopyableResource) resource;
		Dest dest = Utils.getDecodedDestination(request.getDestinationHeader());
		Resource rDest = manager.getResourceFactory().getResource(dest.host, dest.url);
		log.debug("process: copying from: " + r.getName() + " -> " + dest.url + "/" + dest.name);

		if (rDest == null) {
			log.debug("process: destination parent does not exist: " + dest);
			responseHandler.respondConflict(resource, response, request, "Destination does not exist: " + dest);
		} else if (!(rDest instanceof CollectionResource)) {
			log.debug("process: destination exists but is not a collection");
			responseHandler.respondConflict(resource, response, request, "Destination exists but is not a collection: " + dest);
		} else {
			log.debug("process: copy resource to: " + rDest.getName());

			Resource fDest = manager.getResourceFactory().getResource(dest.host, dest.url + "/" + dest.name);
			if (handlerHelper.isLockedOut(request, fDest)) {
				responseHandler.respondLocked(request, response, resource);
				return;
			} else {
				boolean wasDeleted = false;
				CollectionResource colDest = (CollectionResource) rDest;
				Resource rExisting = colDest.child(dest.name);
				if (rExisting != null) {
					if (!canOverwrite(request)) {
						// Exists, and overwrite = F, disallow - http://www.webdav.org/specs/rfc4918.html#rfc.section.9.8.4
						log.info("destination resource exists, and overwrite header is not set. dest name: " + dest.name + " dest folder: " + colDest.getName());
						responseHandler.respondPreconditionFailed(request, response, resource);
						return;
					} else {
						// Overwrite is absent or T, so continue
						if (deleteHelper.isLockedOut(request, rExisting)) {
							log.info("destination resource exists, and overwrite header IS set, but destination is locked. dest name: " + dest.name + " dest folder: " + colDest.getName());
							responseHandler.respondPreconditionFailed(request, response, resource);
							return;
						} else {
							if (deleteExistingBeforeCopy) {
								if (rExisting instanceof DeletableResource) {
									log.debug("copy destination exists and is deletable, delete it..");
									DeletableResource dr = (DeletableResource) rExisting;
									
									// Check the user can delete
									if (!handlerHelper.checkAuthorisation(manager, dr, request, Method.DELETE, request.getAuthorization())) {
										responseHandler.respondUnauthorised(colDest, response, request);
										return;
									}

									deleteHelper.delete(dr, manager.getEventManager());
									wasDeleted = true;
								} else {
									log.warn("copy destination exists and is a collection so must be deleted, but does not implement: " + DeletableResource.class);
									responseHandler.respondConflict(rExisting, response, request, dest.toString());
									return;
								}
							}
						}
					}
				}
				
				// The initial authorisation check is on the resource identified by the request URL. Now we need to check
				// the resource identified in the dest header
				if (!handlerHelper.checkAuthorisation(manager, colDest, request, request.getMethod(), request.getAuthorization())) {
					responseHandler.respondUnauthorised(colDest, response, request);
					return;
				}
				r.copyTo(colDest, dest.name);

				// See http://www.ettrema.com:8080/browse/MIL-87
				if (wasDeleted) {
					responseHandler.respondNoContent(resource, response, request);
				} else {
					responseHandler.respondCreated(resource, response, request);
				}

			}
		}
	}

	public void setDeleteExistingBeforeCopy(boolean deleteExistingBeforeCopy) {
		this.deleteExistingBeforeCopy = deleteExistingBeforeCopy;
	}

	public boolean isDeleteExistingBeforeCopy() {
		return deleteExistingBeforeCopy;
	}

	private boolean canOverwrite(Request request) {
		Boolean ow = request.getOverwriteHeader();
		boolean bHasOverwriteHeader = (ow != null && request.getOverwriteHeader().booleanValue());
		if (bHasOverwriteHeader) {
			return true;
		} else {
			String us = request.getUserAgentHeader();
			if (userAgentHelper.isMacFinder(request)) {
				log.debug("no overwrite header, but user agent is Finder so permit overwrite");
				return true;
			} else {
				return false;
			}
		}
	}

	public UserAgentHelper getUserAgentHelper() {
		return userAgentHelper;
	}
}
