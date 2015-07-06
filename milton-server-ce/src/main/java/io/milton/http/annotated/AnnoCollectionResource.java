/*
 *
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.milton.http.annotated;

import io.milton.common.Path;
import io.milton.http.LockInfo;
import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import io.milton.http.Request;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.resource.CollectionResource;
import io.milton.resource.DeletableCollectionResource;
import io.milton.resource.ExtMakeCalendarResource;
import io.milton.resource.LockingCollectionResource;
import io.milton.resource.MakeCollectionableResource;
import io.milton.resource.PutableResource;
import io.milton.resource.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import org.slf4j.LoggerFactory;

/**
 * Placeholder object to represent a node in an annotations hierachy acting as a
 * collection
 *
 * A source object (ie your pojo) is considered a collection if it can have
 * children , ie if there exists at least one @ChildOf or @ChildrenOf method
 * which has that object as its source type. Note this is keyed on the class.
 *
 * This class includes methods suitable for use in page templating logic for
 * navigating through the hierarchy.
 *
 * @author brad
 */
public class AnnoCollectionResource extends AnnoResource implements CollectionResource, PutableResource, MakeCollectionableResource, LockingCollectionResource, DeletableCollectionResource, ExtMakeCalendarResource {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(AnnoCollectionResource.class);

	/**
	 * lazy loaded list of all children of this collection
	 */
	private ResourceList children;
	/**
	 * this holds child items located single prior to the children list being
	 * populated
	 */
	private ResourceList singlyLoadedChildItems;

	public AnnoCollectionResource(final AnnotationResourceFactory outer, Object source, AnnoCollectionResource parent) {
		super(outer, source, parent);
	}

	@Override
	public Resource child(String childName) throws NotAuthorizedException, BadRequestException {

		// attempt to locate singly, ie without loading entire list of children
		// first check if it has already been loaded singly
		if (singlyLoadedChildItems != null && singlyLoadedChildItems.hasChild(childName)) {
			return singlyLoadedChildItems.get(childName);
		}

		// if children list has already been loaded then look for child in there
		if (children != null) {
			for (Resource r : children) {
				if (r.getName().equals(childName)) {
					return r;
				}
			}
		}

		// try to load singly using ChildOf annotation, if present
		// childTriValue can be null, the child source object, or a special value indicating no search
		Object childTriValue = null;
		try {
			childTriValue = annoFactory.childOfAnnotationHandler.execute(this, childName);
		} catch (NotFoundException ex) {
			log.warn("Failed to lookup child", ex);
		}
		if (childTriValue == null) {
			//return null; // definitely not found
			// well, actually. ChildOf can just apply to a certain sort of child, so if its not found
			// that doesnt mean that there might not be some othe child
			// so we can't assume in any circumstance that a null means not found. Must always fall through to ChildrenOf
		} else if (childTriValue.equals(ChildOfAnnotationHandler.NOT_ATTEMPTED)) {
			// there is no ChildOf method, so fall through to iterating over all children
		} else {
			// got one!
			AnnoResource r = (AnnoResource) childTriValue;
			if (singlyLoadedChildItems == null) {
				singlyLoadedChildItems = new ResourceList();
			}
			singlyLoadedChildItems.add(r);
			return r;
		}

		// Previously we checked in children list if it was loaded to avoid double-instantiating
		// childOf objects. Since we've got here without finding a child, we can load the
		// list of children and iterate over it.
		// We can end up iterating over the list twice, but thats because there
		// is no guarantee that getChildren is the same as children
		for (Resource r : getChildren(true)) {
			if (r.getName().equals(childName)) {
				return r;
			}
		}

		return null;
	}

	@Override
	public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
		return getResourceList();
	}

	public List<? extends Resource> getChildren(boolean isChildLookup) throws NotAuthorizedException, BadRequestException {
		return findChildren(isChildLookup);
	}

	public ResourceList getResourceList() throws NotAuthorizedException, BadRequestException {
		return findChildren(false);
	}

	public ResourceList getSubFolders() throws NotAuthorizedException, BadRequestException {
		return getResourceList().getDirs();
	}

	public ResourceList getFiles() throws NotAuthorizedException, BadRequestException {
		return getResourceList().getFiles();
	}

	protected ResourceList findChildren(boolean isChildLookup) throws NotAuthorizedException, BadRequestException {
		if (children == null) {
			initChildren(isChildLookup);
		}
		return children;
	}

	/**
	 * Called when the children list is first accessed
	 *
	 * This will create the children ResourceList and populate it with known
	 * child resources
	 *
	 * @param isChildLookup - indicates this is being called in the context of a
	 * call to load a single child by name
	 *
	 * @throws NotAuthorizedException
	 * @throws BadRequestException
	 */
	protected void initChildren(boolean isChildLookup) throws NotAuthorizedException, BadRequestException {
		children = new ResourceList();
		Set<AnnoResource> set;
		try {
			set = annoFactory.childrenOfAnnotationHandler.execute(this, isChildLookup);
		} catch (NotAuthorizedException e) {
			throw e;
		} catch (NotFoundException ex) {
			throw new RuntimeException(ex);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		for (AnnoResource r : set) {
			children.add(r);
		}

		// Now add any temp lock resources
		for (LockHolder holder : annoFactory.getTempResourcesForParent(this)) {
			CommonResource cr = annoFactory.instantiate(holder, this);
			children.add(cr);
		}

		// if there are singly loaded items we must replace their dopple-ganger in children
		// because there might already be references to those resource objects elsewhere in code
		// and having two objects representing the same resource causes unpredictable chaos!!!
		if (singlyLoadedChildItems != null) {
			for (CommonResource r : singlyLoadedChildItems) {
				children.remove(r.getName());
				children.add(r);
			}
		}
	}

	public Map<String, CommonResource> getChildrenMap() throws NotAuthorizedException, BadRequestException {
		return getResourceList().getMap();
	}

	public Map<String, ResourceList> getChildrenOfType() throws NotAuthorizedException, BadRequestException {
		return getResourceList().getOfType();
	}

	@Override
	public CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException, BadRequestException {
		Object newlyCreatedSource = annoFactory.makCollectionAnnotationHandler.execute(this, newName);
		AnnoCollectionResource r = new AnnoCollectionResource(annoFactory, newlyCreatedSource, this);
		if (children != null) {
			children.add(r);
		}
		return r;
	}

	@Override
	public CollectionResource createCalendar(String newName, Map<QName, String> fieldsToSet) throws NotAuthorizedException, ConflictException, BadRequestException {
		Object newlyCreatedSource = annoFactory.makeCalendarAnnotationHandler.execute(this, newName, fieldsToSet);
		AnnoCollectionResource r = new AnnoCalendarResource(annoFactory, newlyCreatedSource, this);
		if (children != null) {
			children.add(r);
		}
		return r;
	}

	@Override
	public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
		Object newChildSource = annoFactory.putChildAnnotationHandler.execute(this, newName, inputStream, length, contentType);
		AnnoCollectionResource newRes = new AnnoCollectionResource(annoFactory, newChildSource, this);
		if (children != null) {
			CommonResource oldRes = children.get(newName);
			if (oldRes != null) {
				children.remove(oldRes);
			}
			children.add(newRes);
		}
		return newRes;
	}

	@Override
	public AnnoCollectionResource getRoot() {
		if (parent == null) {
			return this;
		} else {
			return parent.getRoot();
		}
	}

	@Override
	public LockToken createAndLock(String name, LockTimeout timeout, LockInfo lockInfo) throws NotAuthorizedException {
		LockHolder r = annoFactory.createLockHolder(this, name, timeout, lockInfo);
		if (children != null) {
			CommonResource cr = annoFactory.instantiate(r, parent);
			children.add(cr);
		}
		return new LockToken(r.getId().toString(), lockInfo, timeout);
	}

	void removeLockHolder(String name) {
		if (children != null) {
			Iterator<CommonResource> it = children.iterator();
			while (it.hasNext()) {
				Resource r = it.next();
				if (r instanceof LockNullResource && r.getName().equals(name)) {
					it.remove();
				}
			}
		}
	}

	@Override
	public boolean isLockedOutRecursive(Request request) {
		return false; // TODO
	}

	public Resource find(String s) throws NotAuthorizedException, BadRequestException {
		Path p = Path.path(s);
		return findPath(p);
	}

	/**
	 * Locate a resource from the given path evaluated relative to this
	 * resource.
	 *
	 * Supports ".." and "." segments, any other strings are considered file
	 * names
	 *
	 * @param p
	 * @return
	 * @throws NotAuthorizedException
	 * @throws BadRequestException
	 */
	public Resource findPath(Path p) throws NotAuthorizedException, BadRequestException {
		Resource r = this;
		for (String segment : p.getParts()) {
			if (segment.equals("..")) {
				if (r instanceof AnnoResource) {
					AnnoResource ar = (AnnoResource) r;
					r = ar.getParent();
				} else {
					log.warn("Couldnt get parent of resource type: " + r.getClass());
					return null;
				}
			} else if (segment.equals(".")) {
				// do nothing
			} else {
				if (r instanceof CollectionResource) {
					CollectionResource col = (CollectionResource) r;
					r = col.child(segment);
				} else {
					log.warn("Couldnt find child: " + segment + " of parent: " + r.getName() + " because the parent is not actually a collection");
					return null;
				}
			}
		}
		return r;
	}
}
