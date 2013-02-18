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
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.DeletableResource;
import io.milton.resource.ReplaceableResource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 *
 * @author brad
 */
public class LockNullResource implements CommonResource, DeletableResource, ReplaceableResource {

	private final AnnotationResourceFactory annoFactory;
	private final AnnoCollectionResource parent;
	private final LockHolder lockHolder;

	public LockNullResource(AnnotationResourceFactory annoFactory, AnnoCollectionResource parent, LockHolder lockHolder) {
		this.annoFactory = annoFactory;
		this.parent = parent;
		this.lockHolder = lockHolder;
	}

	@Override
	public boolean is(String type) {
		return false;
	}

	@Override
	public CommonResource getParent() {
		return parent;
	}

	@Override
	public String getUniqueId() {
		return lockHolder.getId().toString();
	}

	@Override
	public String getName() {
		return lockHolder.getName();
	}

	@Override
	public Object authenticate(String user, String password) {
		return parent.authenticate(user, password);
	}

	@Override
	public boolean authorise(Request request, Method method, Auth auth) {
		return parent.authorise(request, method, auth);
	}

	@Override
	public String getRealm() {
		return parent.getRealm();
	}

	@Override
	public Date getModifiedDate() {
		return lockHolder.getCreatedDate();
	}

	@Override
	public String checkRedirect(Request request) throws NotAuthorizedException, BadRequestException {
		return null;
	}

	@Override
	public void delete() {
		annoFactory.removeLockHolder(parent, lockHolder.getName());
	}

	@Override
	public void replaceContent(InputStream in, Long length) throws BadRequestException, ConflictException, NotAuthorizedException {
		delete();
		try {
			parent.createNew(lockHolder.getName(), in, length, null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
