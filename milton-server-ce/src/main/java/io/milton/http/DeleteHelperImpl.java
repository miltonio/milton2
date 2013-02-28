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

import io.milton.resource.DeletableCollectionResource;
import io.milton.resource.CollectionResource;
import io.milton.resource.DeletableResource;
import io.milton.resource.Resource;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.event.DeleteEvent;
import io.milton.event.EventManager;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of DeleteHelper
 *
 * It will delegate to the resource if it implements
 * DeletableCollectionResource, otherwise it will walk the collection if its a
 * CollectionResource, and finally will just call handlerHelper.isLockedOut
 * otherwise
 *
 */
public class DeleteHelperImpl implements DeleteHelper {

	private Logger log = LoggerFactory.getLogger(DeleteHelperImpl.class);
	private final HandlerHelper handlerHelper;

	public DeleteHelperImpl(HandlerHelper handlerHelper) {
		this.handlerHelper = handlerHelper;
	}

	@Override
	public boolean isLockedOut(Request req, Resource r) throws NotAuthorizedException, BadRequestException {
		if (r instanceof DeletableCollectionResource) {
			DeletableCollectionResource dcr = (DeletableCollectionResource) r;
			boolean locked = dcr.isLockedOutRecursive(req);
			if (locked && log.isInfoEnabled()) {
				log.info("isLocked, as reported by DeletableCollectionResource: " + dcr.getName());
			}
			return locked;
		} else if (r instanceof CollectionResource) {
			CollectionResource col = (CollectionResource) r;
			List<Resource> list = new ArrayList<Resource>();
			list.addAll(col.getChildren());
			for (Resource rChild : list) {
				if (rChild instanceof DeletableResource) {
					DeletableResource rChildDel = (DeletableResource) rChild;
					if (isLockedOut(req, rChildDel)) {
						if (log.isInfoEnabled()) {
							log.info("isLocked: " + rChild.getName() + " type:" + rChild.getClass());
						}
						return true;
					}
				} else {
					if (log.isInfoEnabled()) {
						log.info("a child resource is not deletable: " + rChild.getName() + " type: " + rChild.getClass());
					}
					return true;
				}
			}
			return false;

		} else {
			boolean locked = handlerHelper.isLockedOut(req, r);
			if (locked && log.isInfoEnabled()) {
				log.info("isLocked, as reported by handlerHelper on resource: " + r.getName());
			}
			return locked;

		}
	}

	@Override
	public void delete(DeletableResource r, EventManager eventManager) throws NotAuthorizedException, ConflictException, BadRequestException {
		if (r instanceof DeletableCollectionResource) {
			r.delete();
			if (eventManager != null) {
				eventManager.fireEvent(new DeleteEvent(r));
			}


		} else if (r instanceof CollectionResource) {
			CollectionResource col = (CollectionResource) r;
			List<Resource> list = new ArrayList<Resource>();
			list.addAll(col.getChildren());
			for (Resource rChild : list) {
				if (rChild == null) {
					log.warn("got a null item in list");
				} else {
					if (rChild instanceof DeletableResource) {
						DeletableResource rChildDel = (DeletableResource) rChild;
						delete(rChildDel, eventManager);
					} else {
						log.warn("Couldnt delete child resource: " + rChild.getName() + " of type; " + rChild.getClass().getName() + " because it does not implement: " + DeletableResource.class.getCanonicalName());
						throw new ConflictException(rChild);
					}
				}
			}
			r.delete();
			if (eventManager != null) {
				eventManager.fireEvent(new DeleteEvent(r));
			}

		} else {
			r.delete();
			if (eventManager != null) {
				eventManager.fireEvent(new DeleteEvent(r));
			}

		}
	}
}
