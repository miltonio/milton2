/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.milton.http.annotated;

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.MakeCollectionableResource;
import io.milton.resource.PutableResource;
import io.milton.resource.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 *
 * @author brad
 */
public class AnnoCollectionResource extends AnnoResource implements CollectionResource, PutableResource, MakeCollectionableResource {
	
	private ResourceList children;

	public AnnoCollectionResource(final AnnotationResourceFactory outer, Object source, AnnoCollectionResource parent) {
		super(outer, source, parent);
	}

	@Override
	public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
		// TODO: have specialised annotation for locating single children
		for (Resource r : getChildren()) {
			if (r.getName().equals(childName)) {
				return r;
			}
		}
		return null;
	}

	@Override
	public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
		if( children == null ) {
			children = new ResourceList();
			Set set = annoFactory.childrenOfAnnotationHandler.execute(source);
			for( Object childSource : set ) {
				AnnoResource r;
				if( annoFactory.childrenOfAnnotationHandler.isCompatible(childSource)) {
					r = new AnnoCollectionResource(annoFactory, childSource, this);
				} else {
					r = new AnnoFileResource(annoFactory, childSource, this);
				}
				children.add(r);
			}
		}
		return children;		
	}

	@Override
	public CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException, BadRequestException {
		Object newlyCreatedSource = annoFactory.makCollectionAnnotationHandler.execute(source, newName);		
		AnnoCollectionResource r = new AnnoCollectionResource(annoFactory, newlyCreatedSource, this);
		if( children != null ) {
			children.add(r);
		}
		return r;
	}

	@Override
	public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
		Object newChildSource = annoFactory.putChildAnnotationHandler.execute(source, newName, inputStream, length, contentType);
		AnnoCollectionResource newRes = new AnnoCollectionResource(annoFactory, newChildSource, this);
		if( children != null ) {
			CommonResource oldRes = children.get(newName); 
			if( oldRes != null ) {
				children.remove(oldRes);
			}
			children.add(newRes);
		}
		return newRes;
	}
	
}
