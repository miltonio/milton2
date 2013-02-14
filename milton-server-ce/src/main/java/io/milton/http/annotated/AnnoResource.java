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

import io.milton.http.Auth;
import io.milton.http.ConditionalCompatibleResource;
import io.milton.http.HttpManager;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.http.http11.auth.DigestResponse;
import io.milton.resource.CollectionResource;
import io.milton.resource.CopyableResource;
import io.milton.resource.DeletableResource;
import io.milton.resource.DigestResource;
import io.milton.resource.GetableResource;
import io.milton.resource.MoveableResource;
import io.milton.resource.PropFindableResource;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author brad
 */
public abstract class AnnoResource implements GetableResource, PropFindableResource, DeletableResource, CopyableResource, MoveableResource, ConditionalCompatibleResource, CommonResource, DigestResource {
	protected Object source;
	protected final AnnotationResourceFactory annoFactory;
	protected AnnoCollectionResource parent;

	public AnnoResource(final AnnotationResourceFactory outer, Object source, AnnoCollectionResource parent) {
		this.annoFactory = outer;
		this.source = source;
		this.parent = parent;
	}

	@Override
	public String getUniqueId() {
		return annoFactory.uniqueIdAnnotationHandler.execute(source);
	}

	@Override
	public String getName() {
		return annoFactory.nameAnnotationHandler.execute(source);
	}

	@Override
	public Object authenticate(String user, String password) {
		return annoFactory.getSecurityManager().authenticate(user, password);
	}

	@Override
	public Object authenticate(DigestResponse digestRequest) {
		return annoFactory.getSecurityManager().authenticate(digestRequest);
	}
		
	@Override
	public boolean authorise(Request request, Method method, Auth auth) {
		return annoFactory.getSecurityManager().authorise(request, method, auth, this);
	}

	@Override
	public String getRealm() {
		return annoFactory.getSecurityManager().getRealm(HttpManager.request().getHostHeader());
	}

	@Override
	public Date getModifiedDate() {
		return annoFactory.modifiedDateAnnotationHandler.execute(source);
	}

	@Override
	public String checkRedirect(Request request) throws NotAuthorizedException, BadRequestException {
		return null;
	}

	@Override
	public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
		annoFactory.deleteAnnotationHandler.execute(source);
	}

	@Override
	public boolean isCompatible(Method m) {
		if( Method.PROPFIND.equals(m)) {
			return true;
		}
		return annoFactory.isCompatible(source, m);
	}
    
	@Override
	public boolean is(String type) {
		return false; // TODO
	}

	@Override
	public Date getCreateDate() {
		return annoFactory.createdDateAnnotationHandler.execute(source);
	}

	@Override
	public void moveTo(CollectionResource rDest, String name) throws ConflictException, NotAuthorizedException, BadRequestException {
		annoFactory.moveAnnotationHandler.execute(source, rDest, name);
	}

	public Object getSource() {
		return source;
	}

	public AnnotationResourceFactory getAnnoFactory() {
		return annoFactory;
	}

	public AnnoCollectionResource getParent() {
		return parent;
	}

	@Override
	public void copyTo(CollectionResource toCollection, String name) throws NotAuthorizedException, BadRequestException, ConflictException {
		annoFactory.copyAnnotationHandler.execute(source, toCollection, name); 
	}	
	
	@Override
	public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
		annoFactory.getAnnotationHandler.execute(source, out, range, params, contentType);
	}

	@Override
	public Long getMaxAgeSeconds(Auth auth) {
		return annoFactory.maxAgeAnnotationHandler.execute(source);
	}

	@Override
	public String getContentType(String accepts) {
		return annoFactory.contentTypeAnnotationHandler.execute(source);
	}

	@Override
	public Long getContentLength() {
		return annoFactory.contentLengthAnnotationHandler.execute(source);
	}	

	@Override
	public boolean isDigestAllowed() {
		return annoFactory.getSecurityManager().isDigestAllowed();
	}
	
	
	
}
