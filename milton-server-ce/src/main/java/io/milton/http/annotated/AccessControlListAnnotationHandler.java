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

import io.milton.annotations.AccessControlList;
import io.milton.annotations.Post;
import io.milton.http.Auth;
import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.resource.AccessControlledResource;
import io.milton.resource.AccessControlledResource.Priviledge;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attempt to locate an Access Control List of the given resource for the
 * current user
 *
 * @author brad
 */
public class AccessControlListAnnotationHandler extends AbstractAnnotationHandler {

	private static final Logger log = LoggerFactory.getLogger(AccessControlListAnnotationHandler.class);

	public AccessControlListAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, AccessControlList.class);
	}

	public Set<AccessControlledResource.Priviledge> availablePrivs(AnnoPrincipalResource curUser, AnnoResource res, Request.Method method, Auth auth) {
		Set<AccessControlledResource.Priviledge> acl = new HashSet<Priviledge>();
		Object source = res.getSource();
		List<ControllerMethod> availMethods = getMethods(source.getClass());
		if (availMethods.isEmpty()) {
			return null;
		}
		try {
			for (ControllerMethod cm : availMethods) {
				Object[] args = outer.buildInvokeArgs(source, cm.method, curUser, res, method, auth);
				Object result = cm.method.invoke(cm.controller, args);
				if (result == null) {
					// ignore
				} else if (result instanceof Collection) {
					Collection col = (Collection) result;
					for (Object o : col) {
						if (o instanceof Priviledge) {
							Priviledge p = (Priviledge) o;
							acl.add(p);
						}
					}
				} else {
					if (result instanceof Priviledge) {
						Priviledge p = (Priviledge) result;
						acl.add(p);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return acl;
	}

	public Priviledge requiredPriv(AnnoResource res, Method httpMethod, Request request) {
		if (httpMethod.equals(Method.POST)) {
			Priviledge p = getRequiredPostPriviledge(request, res);
			if (p == null) {
				p = Priviledge.READ_CONTENT;
			}
			return p;
		} else if (httpMethod.isWrite) {
			return Priviledge.WRITE;
		} else {
			return Priviledge.READ;
		}
	}

	private Priviledge getRequiredPostPriviledge(Request request, AnnoResource res) {
		ControllerMethod cm = outer.postAnnotationHandler.getPostMethod(res, request, HttpManager.request().getParams());
		if (cm == null) {
			return null;
		} else {
			Post p = cm.method.getAnnotation(Post.class);
			if( p != null ) {
				return p.requiredPriviledge();
			} else {
				return null;
			}
		}

	}
}
