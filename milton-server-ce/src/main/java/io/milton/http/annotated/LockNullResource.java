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
