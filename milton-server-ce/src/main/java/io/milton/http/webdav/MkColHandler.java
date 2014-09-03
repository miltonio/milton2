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

import io.milton.http.Handler;
import io.milton.resource.CollectionResource;
import io.milton.resource.Resource;
import io.milton.http.HandlerHelper;
import io.milton.http.HttpManager;
import io.milton.resource.MakeCollectionableResource;
import io.milton.common.Path;
import io.milton.http.Request.Method;
import io.milton.http.Response.Status;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.event.NewFolderEvent;
import io.milton.http.Request;
import io.milton.http.Response;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MkColHandler implements Handler {

	private static final Logger log = LoggerFactory.getLogger(MkColHandler.class);
	private final WebDavResponseHandler responseHandler;
	private final HandlerHelper handlerHelper;
	private CollectionResourceCreator collectionResourceCreator = new DefaultCollectionResourceCreator();

	public MkColHandler(WebDavResponseHandler responseHandler, HandlerHelper handlerHelper) {
		this.responseHandler = responseHandler;
		this.handlerHelper = handlerHelper;
	}

	@Override
	public String[] getMethods() {
		return new String[]{Method.MKCOL.code};
	}

	@Override
	public boolean isCompatible(Resource r) {
		return (r instanceof MakeCollectionableResource);
	}

	@Override
	public void process(HttpManager manager, Request request, Response response) throws ConflictException, NotAuthorizedException, BadRequestException {
		try {
			process(manager, request, response, collectionResourceCreator);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void process(HttpManager manager, Request request, Response response, CollectionResourceCreator creator) throws ConflictException, NotAuthorizedException, BadRequestException, IOException {
		if (!handlerHelper.checkExpects(responseHandler, request, response)) {
			return;
		}
		String host = request.getHostHeader();
		String finalurl = HttpManager.decodeUrl(request.getAbsolutePath());
		String name;
		if (log.isDebugEnabled()) {
			log.debug("process request: host: " + host + " url: " + finalurl);
		}

		Path finalpath = Path.path(finalurl); //this is the parent collection it goes in
		name = finalpath.getName();
		Path parent = finalpath.getParent();
		String parenturl = parent.toString();

		Resource parentcol = manager.getResourceFactory().getResource(host, parenturl);
		if (parentcol != null) {
			log.debug("process: resource: " + parentcol.getClass().getName());

			if (handlerHelper.isNotCompatible(parentcol, request.getMethod())) {
				log.debug("resource not compatible. Resource class: " + parentcol.getClass() + " handler: " + getClass());
				responseHandler.respondMethodNotImplemented(parentcol, response, request);
				return;
			}

			if (handlerHelper.isLockedOut(request, parentcol)) {
				log.warn("isLockedOut");
				response.setStatus(Status.SC_LOCKED);
				return;
			}
			Resource dest = manager.getResourceFactory().getResource(host, finalpath.toString());

			if (dest != null) {
				if (handlerHelper.isLockedOut(request, dest)) {
					log.info("destination exists and is locked");
					responseHandler.respondLocked(request, response, dest);
				} else {
					log.info("destination exists and is not locked");
					responseHandler.respondMethodNotAllowed(dest, response, request);
				}
				return;
			}
			if (handlerHelper.missingLock(request, parentcol)) {
				// ensure the dest is not locked
				if (handlerHelper.isLocked(dest)) {
					log.info("precondition failed");
					//response.setStatus(Status.SC_PRECONDITION_FAILED); //notowner_modify wants this code here
					responseHandler.respondPreconditionFailed(request, response, dest);
					return;
				}
			}

			if (parentcol instanceof CollectionResource) {
				CollectionResource col = (CollectionResource) parentcol;
				if (log.isTraceEnabled()) {
					log.trace("process mkcol on parent: " + parentcol.getClass() + " with creator: " + creator.getClass());
				}
				processMakeCol(manager, request, response, col, name, creator);
			} else {
				log.warn("parent collection is no a CollectionResource: " + parentcol.getName());
				responseHandler.respondConflict(parentcol, response, request, "not a collection");
			}

		} else {
			log.warn("parent does not exist: host=" + host + " path=" + parenturl);
			manager.getResponseHandler().respondConflict(parentcol, response, request, name);
		}
	}

	private void processMakeCol(HttpManager manager, Request request, Response response, CollectionResource resource, String newName, CollectionResourceCreator creator) throws ConflictException, NotAuthorizedException, BadRequestException, IOException {
		if (!handlerHelper.checkAuthorisation(manager, resource, request)) {
			log.info("not authorised");
			responseHandler.respondUnauthorised(resource, response, request);
			return;
		}

		handlerHelper.checkExpects(responseHandler, request, response);

		if (!isCompatible(resource)) {
			log.info("not compatible");
			responseHandler.respondMethodNotImplemented(resource, response, request);
			return;
		}

		MakeCollectionableResource existingCol = (MakeCollectionableResource) resource;
//		try {
//			//For litmus test and RFC support
//			if (request.getInputStream().read() > -1) //This should be empty
//			{
//				log.info("unsupported media type1");
//				response.setStatus(Response.Status.SC_UNSUPPORTED_MEDIA_TYPE);
//				return;
//			}
//		} catch (Exception ex) {
//			//Per RFC2518 MKCOL request-content is undefined and it is therefore MANDATORY to return 415 if it exists.
//			if (request.getContentLengthHeader() > 0) {
//				log.info("unsupported media type2");
//				response.setStatus(Response.Status.SC_UNSUPPORTED_MEDIA_TYPE);
//				return;
//			}
//		}

		Resource existingChild = existingCol.child(newName);
		if (existingChild != null) {
			log.warn("found already existing item: " + newName + " of type: " + existingChild.getClass() + " with actual name: " + existingChild.getName());
			//throw new ConflictException( existingChild );
			// See http://www.ettrema.com:8080/browse/MIL-86
			// 405 (Method Not Allowed) - MKCOL can only be executed on a deleted/non-existent resource.
			responseHandler.respondMethodNotAllowed(existingChild, response, request);
			return;
		}
		CollectionResource made = creator.createResource(existingCol, newName, request);
		if (made == null) {
			log.warn("createCollection returned null. In resource class: " + existingCol.getClass());
			response.setStatus(Response.Status.SC_METHOD_NOT_ALLOWED);
		} else {
			log.info("created item ok: " + made.getClass());
			manager.getEventManager().fireEvent(new NewFolderEvent(resource));
			response.setStatus(Response.Status.SC_CREATED);
		}
	}

	public CollectionResourceCreator getCollectionResourceCreator() {
		return collectionResourceCreator;
	}

	public void setCollectionResourceCreator(CollectionResourceCreator collectionResourceCreator) {
		this.collectionResourceCreator = collectionResourceCreator;
	}

	public interface CollectionResourceCreator {

		CollectionResource createResource(MakeCollectionableResource existingCol, String newName, Request request) throws ConflictException, NotAuthorizedException, BadRequestException, IOException;
	}

	private class DefaultCollectionResourceCreator implements CollectionResourceCreator {

		@Override
		public CollectionResource createResource(MakeCollectionableResource existingCol, String newName, Request request) throws ConflictException, NotAuthorizedException, BadRequestException, IOException {
			CollectionResource made = existingCol.createCollection(newName);
			return made;
		}
	}

}
