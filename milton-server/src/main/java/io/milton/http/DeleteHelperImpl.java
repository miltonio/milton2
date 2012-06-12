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
 * It will delegate to the resource if it implements DeletableCollectionResource,
 * otherwise it will walk the collection if its a CollectionResource, and finally
 * will just call handlerHelper.isLockedOut otherwise
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
						if (eventManager != null) {
							eventManager.fireEvent(new DeleteEvent(rChildDel));
						}
						delete(rChildDel, eventManager);
					} else {
						log.warn("Couldnt delete child resource: " + rChild.getName() + " of type; " + rChild.getClass().getName() + " because it does not implement: " + DeletableResource.class.getCanonicalName());
						throw new ConflictException(rChild);
					}
				}
			}
			r.delete();

		} else {
			r.delete();
		}
	}
}
