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
package io.milton.http.annotated;

import io.milton.annotations.AccessControlList;
import io.milton.annotations.Authenticate;
import io.milton.annotations.ChildOf;
import io.milton.annotations.ChildrenOf;
import io.milton.annotations.ContentType;
import io.milton.annotations.Copy;
import io.milton.annotations.CreatedDate;
import io.milton.annotations.Delete;
import io.milton.annotations.Get;
import io.milton.annotations.MakeCollection;
import io.milton.annotations.MaxAge;
import io.milton.annotations.ModifiedDate;
import io.milton.annotations.Move;
import io.milton.annotations.Name;
import io.milton.annotations.PutChild;
import io.milton.annotations.Root;
import io.milton.annotations.UniqueId;
import io.milton.annotations.Users;
import io.milton.common.Path;
import io.milton.http.HttpManager;
import io.milton.http.LockInfo;
import io.milton.http.LockManager;
import io.milton.http.LockTimeout;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.ResourceFactory;
import io.milton.http.Response;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.template.ViewResolver;
import io.milton.http.webdav.DisplayNameFormatter;
import io.milton.resource.CollectionResource;
import io.milton.resource.PropFindableResource;
import io.milton.resource.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource factory which provides access to files in a file system.
 *
 * Using this with milton is equivalent to using the dav servlet in tomcat
 *
 */
public final class AnnotationResourceFactory implements ResourceFactory {

	private static final Logger log = LoggerFactory.getLogger(AnnotationResourceFactory.class);
	private io.milton.http.SecurityManager securityManager;
	private LockManager lockManager;
	private String contextPath;
	private Collection<Object> controllers;
	private ViewResolver viewResolver;
	/**
	 * Replace with a suitable cluster enabled Map for cluster support
	 */
	private Map<String, List<LockHolder>> mapOfTempResources = new ConcurrentHashMap<String, List<LockHolder>>();
	private Map<Class, AnnotationHandler> mapOfAnnotationHandlers = new HashMap<Class, AnnotationHandler>(); // keyed on annotation class
	private Map<Method, AnnotationHandler> mapOfAnnotationHandlersByMethod = new HashMap<Method, AnnotationHandler>(); // keyed on http method
	RootAnnotationHandler rootAnnotationHandler = new RootAnnotationHandler(this);
	GetAnnotationHandler getAnnotationHandler = new GetAnnotationHandler(this);
	ChildrenOfAnnotationHandler childrenOfAnnotationHandler = new ChildrenOfAnnotationHandler(this);
	ChildOfAnnotationHandler childOfAnnotationHandler = new ChildOfAnnotationHandler(this);
	NameAnnotationHandler nameAnnotationHandler = new NameAnnotationHandler(this);
	DisplayNameAnnotationHandler displayNameAnnotationHandler = new DisplayNameAnnotationHandler(this);
	MakeCollectionAnnotationHandler makCollectionAnnotationHandler = new MakeCollectionAnnotationHandler(this);
	MoveAnnotationHandler moveAnnotationHandler = new MoveAnnotationHandler(this);
	DeleteAnnotationHandler deleteAnnotationHandler = new DeleteAnnotationHandler(this);
	CopyAnnotationHandler copyAnnotationHandler = new CopyAnnotationHandler(this);
	PutChildAnnotationHandler putChildAnnotationHandler = new PutChildAnnotationHandler(this);
	UsersAnnotationHandler usersAnnotationHandler = new UsersAnnotationHandler(this);
	AuthenticateAnnotationHandler authenticateAnnotationHandler = new AuthenticateAnnotationHandler(this);
	AccessControlListAnnotationHandler accessControlListAnnotationHandler = new AccessControlListAnnotationHandler(this);
	CommonPropertyAnnotationHandler<Date> modifiedDateAnnotationHandler = new CommonPropertyAnnotationHandler<Date>(ModifiedDate.class, this);
	CommonPropertyAnnotationHandler<Date> createdDateAnnotationHandler = new CommonPropertyAnnotationHandler<Date>(CreatedDate.class, this);
	CommonPropertyAnnotationHandler<String> contentTypeAnnotationHandler = new CommonPropertyAnnotationHandler<String>(ContentType.class, this);
	CommonPropertyAnnotationHandler<Long> contentLengthAnnotationHandler = new CommonPropertyAnnotationHandler<Long>(ContentType.class, this);
	CommonPropertyAnnotationHandler<Long> maxAgeAnnotationHandler = new CommonPropertyAnnotationHandler<Long>(MaxAge.class, this);
	CommonPropertyAnnotationHandler<String> uniqueIdAnnotationHandler = new CommonPropertyAnnotationHandler<String>(UniqueId.class, this);

	public AnnotationResourceFactory() {
		mapOfAnnotationHandlers.put(Root.class, rootAnnotationHandler);
		mapOfAnnotationHandlers.put(Get.class, getAnnotationHandler);
		mapOfAnnotationHandlers.put(ChildrenOf.class, childrenOfAnnotationHandler);
		mapOfAnnotationHandlers.put(ChildOf.class, childOfAnnotationHandler);
		mapOfAnnotationHandlers.put(Name.class, nameAnnotationHandler);
		mapOfAnnotationHandlers.put(DisplayNameAnnotationHandler.class, displayNameAnnotationHandler);
		mapOfAnnotationHandlers.put(MakeCollection.class, makCollectionAnnotationHandler);
		mapOfAnnotationHandlers.put(Move.class, moveAnnotationHandler);
		mapOfAnnotationHandlers.put(Delete.class, deleteAnnotationHandler);
		mapOfAnnotationHandlers.put(Copy.class, copyAnnotationHandler);
		mapOfAnnotationHandlers.put(PutChild.class, putChildAnnotationHandler);

		mapOfAnnotationHandlers.put(Users.class, usersAnnotationHandler);
		mapOfAnnotationHandlers.put(Authenticate.class, authenticateAnnotationHandler);
		mapOfAnnotationHandlers.put(AccessControlList.class, accessControlListAnnotationHandler);

		mapOfAnnotationHandlers.put(ModifiedDate.class, modifiedDateAnnotationHandler);
		mapOfAnnotationHandlers.put(CreatedDate.class, createdDateAnnotationHandler);
		mapOfAnnotationHandlers.put(ContentType.class, contentTypeAnnotationHandler);
		mapOfAnnotationHandlers.put(MaxAge.class, maxAgeAnnotationHandler);
		mapOfAnnotationHandlers.put(UniqueId.class, uniqueIdAnnotationHandler);

		for (AnnotationHandler ah : mapOfAnnotationHandlers.values()) {
			Method[] methods = ah.getSupportedMethods();
			if (methods != null) {
				for (Method m : methods) {
					mapOfAnnotationHandlersByMethod.put(m, ah);
				}
			}
		}
	}

	@Override
	public Resource getResource(String host, String url) throws NotAuthorizedException, BadRequestException {
		log.info("getResource: host: " + host + " - url:" + url);

		AnnoCollectionResource hostRoot = locateHostRoot(host, HttpManager.request());
		if (hostRoot == null) {
			log.warn("Could not find a root resource for host: " + host + " Using " + rootAnnotationHandler.controllerMethods.size() + " root methods");
			return null;
		}

		Resource r;
		url = stripContext(url);
		if (url.equals("/") || url.equals("")) {
			r = hostRoot;
		} else {
			Path path = Path.path(url);
			r = findFromRoot(hostRoot, path);
			if (r == null) {
				log.info("Resource not found: host=" + host + " path=" + path);
			} else {
				log.info("Found resource: " + r.getClass() + "  for path=" + path);
			}
		}
		return r;
	}

	public Resource findFromRoot(AnnoCollectionResource rootFolder, Path p) throws NotAuthorizedException, BadRequestException {
		CollectionResource col = rootFolder;
		Resource r = null;
		for (String s : p.getParts()) {
			if (col == null) {
				return null;
			}
			r = col.child(s);
			if (r == null) {
				return null;
			}
			if (r instanceof CollectionResource) {
				col = (CollectionResource) r;
			} else {
				col = null;
			}
		}
		return r;
	}

	public String getRealm(String host) {
		return securityManager.getRealm(host);
	}

	public void setSecurityManager(io.milton.http.SecurityManager securityManager) {
		if (securityManager != null) {
			log.debug("securityManager: " + securityManager.getClass());
		} else {
			log.warn("Setting null FsSecurityManager. This WILL cause null pointer exceptions");
		}
		this.securityManager = securityManager;
	}

	public io.milton.http.SecurityManager getSecurityManager() {
		return securityManager;
	}

	public void setMaxAgeSeconds(Long maxAgeSeconds) {
		maxAgeAnnotationHandler.setDefaultValue(maxAgeSeconds);
	}

	public Long getMaxAgeSeconds() {
		return maxAgeAnnotationHandler.getDefaultValue();
	}

	public LockManager getLockManager() {
		return lockManager;
	}

	public void setLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public String getContextPath() {
		return contextPath;
	}

	private String stripContext(String url) {
		if (this.contextPath != null && contextPath.length() > 0) {
			url = url.replaceFirst('/' + contextPath, "");
			//log.debug("stripped context: " + url);
			return url;
		} else {
			return url;
		}
	}

	public Collection<Object> getControllers() {
		return controllers;
	}

	public void setControllers(Collection<Object> controllers) {
		this.controllers = Collections.unmodifiableCollection(controllers);
		for (Object controller : controllers) {
			for (AnnotationHandler ah : mapOfAnnotationHandlers.values()) {
				ah.parseController(controller);
			}
		}
	}

	public ViewResolver getViewResolver() {
		return viewResolver;
	}

	public void setViewResolver(ViewResolver viewResolver) {
		this.viewResolver = viewResolver;
	}

	private AnnoCollectionResource locateHostRoot(String host, Request request) {
		AnnoCollectionResource rootRes;
		if (request != null) {
			// attempt to find one cached in the request
			rootRes = (AnnoCollectionResource) request.getAttributes().get("RootRes_" + host);
			if (rootRes != null) {
				return rootRes;
			}
		}
		Object root = rootAnnotationHandler.execute(host);
		if (root == null) {
			return null;
		}
		rootRes = new AnnoCollectionResource(this, root, null);
		if (request != null) {
			request.getAttributes().put("RootRes_" + host, rootRes);
		}
		return rootRes;
	}

	public boolean isCompatible(Object source, Method m) {
		AnnotationHandler ah = mapOfAnnotationHandlersByMethod.get(m);
		if (ah != null) {
			boolean b = ah.isCompatible(source);
			log.info("isCompatible: " + source + " - " + m + " = " + b);
			return b;
		}
		System.out.println("NO HANDLER for: " + m);
		return false;
	}

	/**
	 *
	 * @param source
	 * @param m
	 * @param otherValues - any other values to be provided which can be mapped
	 * onto method arguments
	 * @return
	 * @throws Exception
	 */
	public Object[] buildInvokeArgs(Object source, java.lang.reflect.Method m, Object... otherValues) throws Exception {
		Request request = HttpManager.request();
		Response response = HttpManager.response();
		Object[] args = new Object[m.getParameterTypes().length];
		args[0] = source; // first arg is required to be source
		List list = new ArrayList();
		for (Object s : otherValues) {
			list.add(s);
		}
		for (int i = 1; i < m.getParameterTypes().length; i++) {
			Class type = m.getParameterTypes()[i];
			try {
				args[i] = findArgValue(type, request, response, list);
			} catch (UnresolvableParameterException e) {
				throw new Exception("Couldnt find parameter " + type + " for method: " + m);
			}
		}
		return args;
	}

	public java.lang.reflect.Method findMethodForAnno(Class sourceClass, Class annoClass) {
		for (java.lang.reflect.Method m : sourceClass.getMethods()) {
			Annotation a = m.getAnnotation(annoClass);
			if (a != null) {
				return m;
			}
		}
		return null;
	}

	private Object findArgValue(Class type, Request request, Response response, List otherAvailValues) throws Exception {
		if (type == Request.class) {
			return request;
		} else if (type == Response.class) {
			return response;
		} else if (type == byte[].class) {
			InputStream in = (InputStream) findArgValue(InputStream.class, request, response, otherAvailValues);
			return toBytes(in);
		} else {
			for (Object o : otherAvailValues) {
				//System.out.println("is assignable: " + o + " == " + type + " = " + o.getClass().isAssignableFrom(type) );				 
				if (o != null && type.isAssignableFrom(o.getClass())) {
					otherAvailValues.remove(o); // remove it so that we dont use same value for next param of same type
					return o;
				}
			}
		}
		log.error("Unknown parameter type: " + type);
		log.error("Available types are:");
		log.error(" - " + Request.class);
		log.error(" - " + Response.class);
		for (Object o : otherAvailValues) {
			if (o != null) {
				log.error(" - " + o.getClass());
			} else {
				log.error(" - null");
			}
		}

		throw new UnresolvableParameterException("Couldnt find parameter of type: " + type);
	}

	private byte[] toBytes(InputStream inputStream) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		IOUtils.copy(inputStream, bout);
		return bout.toByteArray();
	}

	/**
	 * Create a Resource to wrap a source pojo object.
	 *
	 * @param childSource
	 * @param parent
	 * @param method - the method which located the source object. Will be
	 * inspected for annotations
	 * @return
	 */
	public AnnoResource instantiate(Object childSource, AnnoCollectionResource parent, java.lang.reflect.Method m) {
		if (m.getAnnotation(Users.class) != null) { // TODO: instead of Users annotation, just look for @Authenticate anno for source class
			return new AnnoPrincipalResource(this, childSource, parent);
		}
		if (childrenOfAnnotationHandler.isCompatible(childSource)) {
			return new AnnoCollectionResource(this, childSource, parent);
		} else {
			return new AnnoFileResource(this, childSource, parent);
		}
	}

	public CommonResource instantiate(LockHolder r, AnnoCollectionResource parent) {
		return new LockNullResource(this, parent, r);
	}

	/**
	 * Create an in-memory resource with the given timeout. The resource will
	 * not be persisted, but may be distributed among the cluster if configured
	 * as such.
	 *
	 * The resource may be flushed from the in-memory list after the given
	 * timeout if not-null
	 *
	 * The type of the object returned is not intended to have any significance
	 * and does not contain any meaningful content
	 *
	 * @param parentCollection
	 * @param name
	 * @param timeout - optional. The resource will be removed after this
	 * timeout expires
	 * @return - a temporary (not persistent) resource of an indeterminate type
	 */
	public LockHolder createLockHolder(AnnoCollectionResource parentCollection, String name, LockTimeout timeout, LockInfo lockInfo) {
		String parentKey = parentCollection.getUniqueId();
		if (parentKey == null) {
			throw new RuntimeException("Cant create temp resource because parent's uniqueID is null. Please add the @UniqueID for class: " + parentCollection.getSource().getClass());
		}
		LockHolder r = new LockHolder(UUID.randomUUID());
		r.setParentCollectionId(parentKey);
		r.setName(name);
		r.setLockTimeout(timeout);
		r.setLockInfo(lockInfo);
		synchronized (this) {
			List<LockHolder> list = mapOfTempResources.get(parentKey);
			if (list == null) {
				list = new CopyOnWriteArrayList<LockHolder>();
				mapOfTempResources.put(parentKey, list);
			}
			list.add(r);
		}
		return r;
	}

	/**
	 * Null-safe method to get the list of lock holders for the given parent.
	 * These are resources created by a LOCK-null request, where resources are
	 * locked prior to being created. The lock-null resource is replaced
	 * following a PUT to that resource
	 *
	 * @param parent
	 * @return
	 */
	public List<LockHolder> getTempResourcesForParent(AnnoCollectionResource parent) {
		String parentKey = parent.getUniqueId();
		if (parentKey == null) {
			return Collections.EMPTY_LIST;
		}
		return getTempResourcesForParent(parentKey);
	}

	public List<LockHolder> getTempResourcesForParent(String parentKey) {
		List<LockHolder> list = mapOfTempResources.get(parentKey);
		if (list == null) {
			return Collections.EMPTY_LIST;
		} else {
			return list;
		}
	}

	/**
	 * Removes the LockHolder from memory and also from the parent which
	 * contains it (if loaded)
	 *
	 * @param parent
	 * @param name
	 */
	public void removeLockHolder(AnnoCollectionResource parent, String name) {
		List<LockHolder> list = getTempResourcesForParent(parent);
		Iterator<LockHolder> it = list.iterator();
		while (it.hasNext()) {
			if (it.next().getName().equals(name)) {
				it.remove();
			}
		}
		parent.removeLockHolder(name);
	}

	public Map<String, List<LockHolder>> getMapOfTempResources() {
		return mapOfTempResources;
	}

	public void setMapOfTempResources(Map<String, List<LockHolder>> mapOfTempResources) {
		this.mapOfTempResources = mapOfTempResources;
	}

	public class AnnotationsDisplayNameFormatter implements DisplayNameFormatter {

		private final DisplayNameFormatter wrapped;

		public AnnotationsDisplayNameFormatter(DisplayNameFormatter wrapper) {
			this.wrapped = wrapper;
		}

		@Override
		public String formatDisplayName(PropFindableResource res) {
			if (res instanceof AnnoResource) {
				AnnoResource r = (AnnoResource) res;
				return r.getDisplayName();
			}
			return wrapped.formatDisplayName(res);
		}
	}
}
