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

import io.milton.http.ExistingEntityHandler;
import io.milton.resource.Resource;
import io.milton.http.HttpManager;
import io.milton.common.Utils;
import io.milton.http.Response;
import io.milton.http.DeleteHelper;
import io.milton.resource.MoveableResource;
import io.milton.resource.DeletableResource;
import io.milton.http.DeleteHelperImpl;
import io.milton.http.HandlerHelper;
import io.milton.resource.CollectionResource;
import io.milton.http.ResourceHandlerHelper;
import io.milton.http.exceptions.ConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.event.MoveEvent;
import io.milton.http.Request;

public class MoveHandler implements ExistingEntityHandler {

	private Logger log = LoggerFactory.getLogger(MoveHandler.class);
	private final WebDavResponseHandler responseHandler;
	private final ResourceHandlerHelper resourceHandlerHelper;
	private final HandlerHelper handlerHelper;
	private final UserAgentHelper userAgentHelper;
	private DeleteHelper deleteHelper;	
	private boolean deleteExistingBeforeMove = true;

	/**
	 * Sets userAgentHelper to DefaultUserAgentHelper, which can be overridden
	 * by setting the property
	 *
	 * deleteHelper is set to DeleteHelperImpl
	 *
	 * @param responseHandler
	 * @param handlerHelper
	 * @param resourceHandlerHelper
	 */
	public MoveHandler(WebDavResponseHandler responseHandler, HandlerHelper handlerHelper, ResourceHandlerHelper resourceHandlerHelper, UserAgentHelper userAgentHelper) {
		this.userAgentHelper = userAgentHelper;
		this.responseHandler = responseHandler;
		this.resourceHandlerHelper = resourceHandlerHelper;
		this.handlerHelper = handlerHelper;
		this.deleteHelper = new DeleteHelperImpl(handlerHelper);
	}

	
	@Override
	public String[] getMethods() {
		return new String[]{Method.MOVE.code};
	}

	@Override
	public boolean isCompatible(Resource handler) {
		return (handler instanceof MoveableResource);
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
		MoveableResource r = (MoveableResource) resource;

		Dest dest = Utils.getDecodedDestination(request.getDestinationHeader());
		Resource rDest = manager.getResourceFactory().getResource(dest.host, dest.url);
		log.debug("process: moving from: " + r.getName() + " -> " + dest.url + " with name: " + dest.name);
		if (rDest == null) {
			log.debug("process: destination parent does not exist: " + dest);
			responseHandler.respondConflict(resource, response, request, "Destination parent does not exist: " + dest);
		} else if (!(rDest instanceof CollectionResource)) {
			log.debug("process: destination exists but is not a collection");
			responseHandler.respondConflict(resource, response, request, "Destination exists but is not a collection: " + dest);
		} else {
			boolean wasDeleted = false;
			CollectionResource colDest = (CollectionResource) rDest;
			// check if the dest exists
			Resource rExisting = colDest.child(dest.name);
			if (rExisting != null) {
				// check for overwrite header
				if (!canOverwrite(request)) {
					log.info("destination resource exists, and overwrite header is not set. dest name: " + dest.name + " dest folder: " + colDest.getName());
					responseHandler.respondPreconditionFailed(request, response, rExisting);
					return;
				} else {
					if (deleteExistingBeforeMove) {
						if (rExisting instanceof DeletableResource) {
							log.debug("deleting existing resource");
							DeletableResource drExisting = (DeletableResource) rExisting;
							if (deleteHelper.isLockedOut(request, drExisting)) {
								log.debug("destination resource exists but is locked");
								responseHandler.respondLocked(request, response, drExisting);
								return;
							}
							log.debug("deleting pre-existing destination resource");
							deleteHelper.delete(drExisting, manager.getEventManager());
							wasDeleted = true;
						} else {
							log.warn("destination exists, and overwrite header is set, but destination is not a DeletableResource");
							responseHandler.respondConflict(resource, response, request, "A resource exists at the destination, and it cannot be deleted");
							return;
						}
					}
				}
			}
			log.debug("process: moving resource to: " + rDest.getName());
			try {
				if( !handlerHelper.checkAuthorisation(manager, colDest, request, request.getMethod(), request.getAuthorization()) ) {
					responseHandler.respondUnauthorised( colDest, response, request );
					return ;
				}
				manager.getEventManager().fireEvent(new MoveEvent(resource, colDest, dest.name));
				r.moveTo(colDest, dest.name);
				// See http://www.ettrema.com:8080/browse/MIL-87
				if (wasDeleted) {
					responseHandler.respondNoContent(resource, response, request);
				} else {
					responseHandler.respondCreated(resource, response, request);
				}
			} catch (ConflictException ex) {
				log.warn("conflict", ex);
				responseHandler.respondConflict(resource, response, request, dest.toString());
			}
		}
		log.debug("process: finished");
	}

	private boolean canOverwrite(Request request) {
		Boolean ow = request.getOverwriteHeader();
		boolean bHasOverwriteHeader = (ow != null && request.getOverwriteHeader().booleanValue());
		if (bHasOverwriteHeader) {
			return true;
		} else {			
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

	public DeleteHelper getDeleteHelper() {
		return deleteHelper;
	}

	public void setDeleteHelper(DeleteHelper deleteHelper) {
		this.deleteHelper = deleteHelper;
	}
	
    public void setDeleteExistingBeforeMove(boolean deleteExistingBeforeCopy) {
        this.deleteExistingBeforeMove = deleteExistingBeforeCopy;
    }

    public boolean isDeleteExistingBeforeMove() {
        return deleteExistingBeforeMove;
    }	
}
