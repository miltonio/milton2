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

import io.milton.common.JsonResult;
import io.milton.http.AclUtils;
import io.milton.http.Auth;
import io.milton.http.ConditionalCompatibleResource;
import io.milton.http.FileItem;
import io.milton.http.HttpManager;
import io.milton.http.LockInfo;
import io.milton.http.LockResult;
import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.LockedException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.http.exceptions.PreConditionFailedException;
import io.milton.http.http11.auth.DigestResponse;
import io.milton.resource.AccessControlledResource;
import io.milton.resource.CollectionResource;
import io.milton.resource.CopyableResource;
import io.milton.resource.DeletableResource;
import io.milton.resource.DigestResource;
import io.milton.resource.GetableResource;
import io.milton.resource.LockableResource;
import io.milton.resource.MoveableResource;
import io.milton.resource.PostableResource;
import io.milton.resource.PropFindableResource;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public abstract class AnnoResource implements GetableResource, PropFindableResource, DeletableResource, CopyableResource, MoveableResource, LockableResource, ConditionalCompatibleResource, CommonResource, DigestResource, PostableResource {

	private static final Logger log = LoggerFactory.getLogger(AnnoResource.class);
	protected Object source;
	protected final AnnotationResourceFactory annoFactory;
	protected AnnoCollectionResource parent;
	protected JsonResult jsonResult;
	protected String nameOverride;

	public AnnoResource(final AnnotationResourceFactory outer, Object source, AnnoCollectionResource parent) {
		if (source == null) {
			throw new RuntimeException("Source object is required");
		}
		this.annoFactory = outer;
		this.source = source;
		this.parent = parent;
	}

	@Override
	public String processForm(Map<String, String> parameters, Map<String, FileItem> files) throws BadRequestException, NotAuthorizedException, ConflictException {
		Request request = HttpManager.request();
		Object result = annoFactory.postAnnotationHandler.execute(this, request, parameters);
		if (result instanceof String) {
			String redirect = (String) result;
			return redirect;
		} else if (result instanceof JsonResult) {
			jsonResult = (JsonResult) result;
		}
		return null;
	}

	@Override
	public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
		if (jsonResult == null) {
			annoFactory.getAnnotationHandler.execute(this, out, range, params, contentType);
		} else {
			JsonWriter jsonWriter = new JsonWriter();
			jsonWriter.write(this, out);
		}
	}

	@Override
	public String getUniqueId() {
		return annoFactory.uniqueIdAnnotationHandler.execute(source);
	}

	@Override
	public String getName() {
		if (nameOverride != null) {
			return nameOverride;
		}
		return annoFactory.nameAnnotationHandler.execute(source);
	}

	@Override
	public Object authenticate(String user, String password) {
		AnnoPrincipalResource userRes = annoFactory.usersAnnotationHandler.findUser(getRoot(), user);
		if (userRes != null) {
			Boolean b = annoFactory.authenticateAnnotationHandler.authenticate(userRes, password);
			if (b != null) {
				return userRes;
			}
		}
		return annoFactory.getSecurityManager().authenticate(user, password);
	}

	@Override
	public Object authenticate(DigestResponse digestRequest) {
		AnnoPrincipalResource userRes = annoFactory.usersAnnotationHandler.findUser(getRoot(), digestRequest.getUser());
		if (userRes != null) {
			Boolean b = annoFactory.authenticateAnnotationHandler.authenticate(userRes, digestRequest);
			if (b != null) {
				return userRes;
			}
		}
		return annoFactory.getSecurityManager().authenticate(digestRequest);
	}

	@Override
	public boolean authorise(Request request, Method method, Auth auth) {
		Object oUser = auth.getTag();
		AnnoPrincipalResource p = null;
		if (oUser instanceof AnnoPrincipalResource) {
			p = (AnnoPrincipalResource) oUser;
		}
		// only check ACL if current user is null (ie guest) or the current user is an AnnoPrincipal
		if (oUser == null || p != null) {
			Set<AccessControlledResource.Priviledge> acl = annoFactory.accessControlListAnnotationHandler.availablePrivs(p, this, method, auth);
			if (acl != null) {
				AccessControlledResource.Priviledge requiredPriv = annoFactory.accessControlListAnnotationHandler.requiredPriv(this, method, request);
				boolean allows;
				if (requiredPriv == null) {
					allows = true;
				} else {
					allows = AclUtils.containsPriviledge(requiredPriv, acl);
					if (!allows) {
						if (p != null) {
							log.info("Authorisation declined for user: " + p.getName());
						} else {
							log.info("Authorisation declined for anonymous access");
						}
						log.info("Required priviledge: " + requiredPriv + " was not found in assigned priviledge list of size: " + acl.size());
					}
				}
				return allows;
			} else {
				// null ACL means do not apply ACL
			}
		}
		// if we get here it means ACL was not applied, so we check default SM
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
		if (Method.PROPFIND.equals(m)) {
			return true;
		}
		return annoFactory.isCompatible(source, m);
	}

	@Override
	public boolean is(String type) {

		if (matchesType(source.getClass(), type)) {
			return true;
		}
		for (Class c : source.getClass().getClasses()) {
			if (matchesType(c, type)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Date getCreateDate() {
		return annoFactory.createdDateAnnotationHandler.execute(source);
	}

	@Override
	public void moveTo(CollectionResource rDest, String name) throws ConflictException, NotAuthorizedException, BadRequestException {
		nameOverride = null; // reset any explicitly set name (eg for creating new resources)
		annoFactory.moveAnnotationHandler.execute(source, rDest, name);
	}

	public Object getSource() {
		return source;
	}

	public AnnotationResourceFactory getAnnoFactory() {
		return annoFactory;
	}

	@Override
	public AnnoCollectionResource getParent() {
		return parent;
	}

	@Override
	public void copyTo(CollectionResource toCollection, String name) throws NotAuthorizedException, BadRequestException, ConflictException {
		annoFactory.copyAnnotationHandler.execute(source, toCollection, name);
	}

	@Override
	public Long getMaxAgeSeconds(Auth auth) {
		return annoFactory.maxAgeAnnotationHandler.execute(source);
	}

	@Override
	public String getContentType(String accepts) {
		if (accepts != null && accepts.contains("application/json")) {
			return "application/json";
		}
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

	public ResourceList getAsList() {
		ResourceList l = new ResourceList();
		l.add(this);
		return l;
	}

	private boolean matchesType(Class c, String type) {
		String name = c.getCanonicalName();
		int pos = name.lastIndexOf(".");
		name = name.substring(pos);
		if (name.equalsIgnoreCase(type)) {
			return true;
		}
		return false;
	}

	public String getHref() {
		if (parent == null) {
			return "/";
		} else {
			String s = parent.getHref() + getName();
			if (this instanceof CollectionResource) {
				s += "/";
			}
			return s;
		}
	}

	public AnnoCollectionResource getRoot() {
		return parent.getRoot();
	}

	public String getLink() {
		return "<a href=\"" + getHref() + "\">" + getName() + "</a>";
	}

	public String getDisplayName() {
		return annoFactory.displayNameAnnotationHandler.execute(this);
	}

	@Override
	public LockResult lock(LockTimeout timeout, LockInfo lockInfo) throws NotAuthorizedException, PreConditionFailedException, LockedException {
		return annoFactory.getLockManager().lock(timeout, lockInfo, this);
	}

	@Override
	public LockResult refreshLock(String token) throws NotAuthorizedException, PreConditionFailedException {
		return annoFactory.getLockManager().refresh(token, this);
	}

	@Override
	public void unlock(String tokenId) throws NotAuthorizedException, PreConditionFailedException {
		annoFactory.getLockManager().unlock(tokenId, this);
	}

	@Override
	public LockToken getCurrentLock() {
		return annoFactory.getLockManager().getCurrentToken(this);
	}

	public String getNameOverride() {
		return nameOverride;
	}

	public void setNameOverride(String nameOverride) {
		this.nameOverride = nameOverride;
	}
}
