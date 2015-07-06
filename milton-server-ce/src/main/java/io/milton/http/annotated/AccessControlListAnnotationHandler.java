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

import io.milton.annotations.AccessControlList;
import io.milton.annotations.Post;
import io.milton.http.Auth;
import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.resource.AccessControlledResource;
import io.milton.resource.AccessControlledResource.Priviledge;
import java.util.Collection;
import java.util.EnumSet;
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

	/**
	 * Get priviledges for the current user
	 *
	 * @param curUser
	 * @param res
	 * @param auth
	 * @return
	 */
	public Set<AccessControlledResource.Priviledge> availablePrivs(Object curUser, AnnoResource res, Auth auth) {
		Set<Priviledge> privs = directPrivs(curUser, res, auth);
		if (privs != null) {
			return privs;
		}
		AnnoCollectionResource p = res.getParent();
		while (p != null) {
			privs = directPrivs(curUser, p, auth);
			if (privs != null) {
				return privs;
			}
			p = p.getParent();
		}
		// BM: this shouldnt be here. We do want to grant all privs at this point, because nothing
		// explicit was found, but only if there is a current user. And privs should use Priviledge.ALL
		// rather then allOf(..) because we want to use the more concise, unexpanded, priviledge set
		//privs = EnumSet.allOf(Priviledge.class);
		if (curUser != null) {
			log.info("No explicit AccessControl annotation found, so defaulting to full access for logged in user");
			privs = new HashSet<Priviledge>();
			privs.add(Priviledge.ALL);
		}
		return privs;
	}

	public Set<AccessControlledResource.Priviledge> directPrivs(Object curUser, AnnoResource res, Auth auth) {
		Set<AccessControlledResource.Priviledge> acl =  EnumSet.noneOf(AccessControlledResource.Priviledge.class);
		Object source = res.getSource();
		List<ControllerMethod> availMethods = getMethods(source.getClass());
		if (availMethods.isEmpty()) {
			log.warn("No ACL methods were found");
			return null;
		} else {
			try {
				for (ControllerMethod cm : availMethods) {
					addPrivsFromMethod(cm.method, cm.controller, acl, curUser, res, auth);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return acl;
	}

	private void addPrivsFromMethod(java.lang.reflect.Method method, Object target, Set<AccessControlledResource.Priviledge> acl, Object curUser, AnnoResource res, Auth auth) throws Exception {
		Object currentUserSource = null;
		if (curUser != null) {
			if( curUser instanceof AnnoResource) {
				AnnoResource ar = (AnnoResource) curUser;
				currentUserSource = ar.getSource();
			} else {
				currentUserSource = curUser;
			}
		}
		Object[] args = annoResourceFactory.buildInvokeArgsExt(res, currentUserSource, true, method, curUser, res, auth);

		Object result = method.invoke(target, args);
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

	public Priviledge requiredPriv(AnnoResource res, Method httpMethod, Request request) {
		if (httpMethod.equals(Method.POST)) {
			Priviledge p = getRequiredPostPriviledge(request, res);
			if (p == null) {
				p = Priviledge.READ_CONTENT;
			}
			return p;
		} else if( httpMethod == Method.ACL) {
			return Priviledge.READ_ACL;
		} else if( httpMethod == Method.UNLOCK) {
			return Priviledge.UNLOCK;
		} else if( httpMethod == Method.PROPFIND) {
			return Priviledge.READ_PROPERTIES;
		} else if (httpMethod.isWrite) {
			return Priviledge.WRITE_CONTENT;
		} else {
			return Priviledge.READ_CONTENT;
		}
	}

	private Priviledge getRequiredPostPriviledge(Request request, AnnoResource res) {
		ControllerMethod cm = annoResourceFactory.postAnnotationHandler.getPostMethod(res, request, HttpManager.request().getParams());
		if (cm == null) {
			return null;
		} else {
			Post p = cm.method.getAnnotation(Post.class);
			if (p != null) {
				return p.requiredPriviledge();
			} else {
				return null;
			}
		}

	}
}
