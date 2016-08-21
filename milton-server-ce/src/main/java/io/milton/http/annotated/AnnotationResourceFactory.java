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
import io.milton.annotations.AddressBooks;
import io.milton.annotations.Authenticate;
import io.milton.annotations.CTag;
import io.milton.annotations.CalendarColor;
import io.milton.annotations.CalendarDateRangeQuery;
import io.milton.annotations.CalendarInvitations;
import io.milton.annotations.CalendarInvitationsCTag;
import io.milton.annotations.CalendarOrder;
import io.milton.annotations.CalendarUserType;
import io.milton.annotations.Calendars;
import io.milton.annotations.ChildOf;
import io.milton.annotations.ChildrenOf;
import io.milton.annotations.ContactData;
import io.milton.annotations.ContentLength;
import io.milton.annotations.ContentType;
import io.milton.annotations.Copy;
import io.milton.annotations.CreatedDate;
import io.milton.annotations.Delete;
import io.milton.annotations.DirectoryGateway;
import io.milton.annotations.Email;
import io.milton.annotations.FreeBusyQuery;
import io.milton.annotations.Get;
import io.milton.annotations.ICalData;
import io.milton.annotations.MakeCollection;
import io.milton.annotations.MaxAge;
import io.milton.annotations.ModifiedDate;
import io.milton.annotations.Move;
import io.milton.annotations.Name;
import io.milton.annotations.Post;
import io.milton.annotations.PrincipalSearch;
import io.milton.annotations.PutChild;
import io.milton.annotations.Realm;
import io.milton.annotations.Root;
import io.milton.annotations.SupportedComponentSets;
import io.milton.annotations.UniqueId;
import io.milton.annotations.Users;
import io.milton.common.Path;
import io.milton.http.Auth;
import io.milton.http.AuthenticationService;
import io.milton.http.HttpManager;
import io.milton.http.LockInfo;
import io.milton.http.LockManager;
import io.milton.http.LockTimeout;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.ResourceFactory;
import io.milton.http.Response;
import io.milton.http.caldav.CalendarSearchService;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.template.ViewResolver;
import io.milton.http.values.SupportedCalendarComponentListsSet;
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
import java.util.EnumMap;
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
	private AuthenticationService authenticationService;
	private CalendarSearchService calendarSearchService;
	private boolean doEarlyAuth = true;
	private io.milton.http.SecurityManager securityManager;
	private LockManager lockManager;
	private String contextPath;
	private Collection<Object> controllers;
	private ViewResolver viewResolver;
	/**
	 * Replace with a suitable cluster enabled Map for cluster support
	 */
	private Map<String, List<LockHolder>> mapOfTempResources = new ConcurrentHashMap<String, List<LockHolder>>();
	private final Map<Class, AnnotationHandler> mapOfAnnotationHandlers = new HashMap<Class, AnnotationHandler>(); // keyed on annotation class
	private final Map<Method, AnnotationHandler> mapOfAnnotationHandlersByMethod = new EnumMap<Method, AnnotationHandler>(Method.class); // keyed on http method
	RootAnnotationHandler rootAnnotationHandler = new RootAnnotationHandler(this);
	GetAnnotationHandler getAnnotationHandler = new GetAnnotationHandler(this);
	PostAnnotationHandler postAnnotationHandler = new PostAnnotationHandler(this);
	ChildrenOfAnnotationHandler childrenOfAnnotationHandler = new ChildrenOfAnnotationHandler(this);
	ChildOfAnnotationHandler childOfAnnotationHandler = new ChildOfAnnotationHandler(this);
	DisplayNameAnnotationHandler displayNameAnnotationHandler = new DisplayNameAnnotationHandler(this);
	DisplayNameSetterAnnotationHandler displayNameSetterAnnotationHandler = new DisplayNameSetterAnnotationHandler(this);
	MakeCollectionAnnotationHandler makCollectionAnnotationHandler = new MakeCollectionAnnotationHandler(this);
	MakeCalendarAnnotationHandler makeCalendarAnnotationHandler = new MakeCalendarAnnotationHandler(this);
	MoveAnnotationHandler moveAnnotationHandler = new MoveAnnotationHandler(this);
	DeleteAnnotationHandler deleteAnnotationHandler = new DeleteAnnotationHandler(this);
	CopyAnnotationHandler copyAnnotationHandler = new CopyAnnotationHandler(this);
	PutChildAnnotationHandler putChildAnnotationHandler = new PutChildAnnotationHandler(this);
	UsersAnnotationHandler usersAnnotationHandler = new UsersAnnotationHandler(this);
	AuthenticateAnnotationHandler authenticateAnnotationHandler = new AuthenticateAnnotationHandler(this);
	AccessControlListAnnotationHandler accessControlListAnnotationHandler = new AccessControlListAnnotationHandler(this);
	CTagAnnotationHandler cTagAnnotationHandler = new CTagAnnotationHandler(this);
	ICalDataAnnotationHandler iCalDataAnnotationHandler = new ICalDataAnnotationHandler(this);
	CalendarsAnnotationHandler calendarsAnnotationHandler = new CalendarsAnnotationHandler(this);
	AddressBooksAnnotationHandler addressBooksAnnotationHandler = new AddressBooksAnnotationHandler(this);
	ContactDataAnnotationHandler contactDataAnnotationHandler = new ContactDataAnnotationHandler(this);
	PrincipalSearchAnnotationHandler principalSearchAnnotationHandler = new PrincipalSearchAnnotationHandler(this);
	CommonPropertyAnnotationHandler<String> nameAnnotationHandler = new CommonPropertyAnnotationHandler(Name.class, this, "name", "fileName");
	CommonPropertyAnnotationHandler<String> emailAnnotationHandler = new CommonPropertyAnnotationHandler(Email.class, this, "email");
	CommonPropertyAnnotationHandler<SupportedCalendarComponentListsSet> supportedComponentSets = new CommonPropertyAnnotationHandler(SupportedComponentSets.class, this, "supportedComponentSets");
	CommonPropertyAnnotationHandler<String> realmAnnotationHandler = new CommonPropertyAnnotationHandler(Realm.class, this, "realm");
	CommonPropertyAnnotationHandler<Date> modifiedDateAnnotationHandler = new CommonPropertyAnnotationHandler<Date>(ModifiedDate.class, this, "modifiedDate");
	CommonPropertyAnnotationHandler<Date> createdDateAnnotationHandler = new CommonPropertyAnnotationHandler<Date>(CreatedDate.class, this);
	ContentTypeAnnotationHandler contentTypeAnnotationHandler = new ContentTypeAnnotationHandler(this, "contentType");
	CommonPropertyAnnotationHandler<Long> contentLengthAnnotationHandler = new CommonPropertyAnnotationHandler<Long>(ContentLength.class, this, "contentLength");
	CommonPropertyAnnotationHandler<Long> maxAgeAnnotationHandler = new CommonPropertyAnnotationHandler<Long>(MaxAge.class, this, "maxAge");
	CommonPropertyAnnotationHandler<String> uniqueIdAnnotationHandler = new CommonPropertyAnnotationHandler<String>(UniqueId.class, this, "id");
	CommonPropertyAnnotationHandler<String> calendarColorAnnotationHandler = new CommonPropertyAnnotationHandler<String>(CalendarColor.class, this, "color");
	CommonPropertyAnnotationHandler<String> calendarOrderAnnotationHandler = new CommonPropertyAnnotationHandler<String>(CalendarOrder.class, this, "order");
	CommonPropertyAnnotationHandler<String> calendarUserTypeAnnotationHandler = new CommonPropertyAnnotationHandler<String>(CalendarUserType.class, this, "calendarUserType", "cuType");
	CalendarDateRangeQueryAnnotationHandler calendarDateRangeQueryAnnotationHandler = new CalendarDateRangeQueryAnnotationHandler(this);
	FreeBusyQueryAnnotationHandler freeBusyQueryAnnotationHandler = new FreeBusyQueryAnnotationHandler(this);
	CalendarInvitationsAnnotationHandler calendarInvitationsAnnotationHandler = new CalendarInvitationsAnnotationHandler(this);
	CalendarInvitationsCTagAnnotationHandler calendarInvitationsCTagAnnotationHandler = new CalendarInvitationsCTagAnnotationHandler(this);
	CommonPropertyAnnotationHandler<Boolean> directoryGatewayAnnotationHandler = new CommonPropertyAnnotationHandler(DirectoryGateway.class, this, "directoryGateway");

	public AnnotationResourceFactory() {

		supportedComponentSets.setDefaultValue(SupportedCalendarComponentListsSet.EVENTS_ONLY);

		mapOfAnnotationHandlers.put(Root.class, rootAnnotationHandler);
		mapOfAnnotationHandlers.put(Get.class, getAnnotationHandler);
		mapOfAnnotationHandlers.put(Post.class, postAnnotationHandler);
		mapOfAnnotationHandlers.put(ChildrenOf.class, childrenOfAnnotationHandler);
		mapOfAnnotationHandlers.put(ChildOf.class, childOfAnnotationHandler);
		mapOfAnnotationHandlers.put(Name.class, nameAnnotationHandler);
		mapOfAnnotationHandlers.put(DisplayNameAnnotationHandler.class, displayNameAnnotationHandler);
		mapOfAnnotationHandlers.put(DisplayNameSetterAnnotationHandler.class, displayNameSetterAnnotationHandler);		
		mapOfAnnotationHandlers.put(MakeCollection.class, makCollectionAnnotationHandler);
		mapOfAnnotationHandlers.put(Move.class, moveAnnotationHandler);
		mapOfAnnotationHandlers.put(Delete.class, deleteAnnotationHandler);
		mapOfAnnotationHandlers.put(Copy.class, copyAnnotationHandler);
		mapOfAnnotationHandlers.put(PutChild.class, putChildAnnotationHandler);

		mapOfAnnotationHandlers.put(Users.class, usersAnnotationHandler);
		mapOfAnnotationHandlers.put(Authenticate.class, authenticateAnnotationHandler);
		mapOfAnnotationHandlers.put(AccessControlList.class, accessControlListAnnotationHandler);
		mapOfAnnotationHandlers.put(AddressBooks.class, addressBooksAnnotationHandler);
		mapOfAnnotationHandlers.put(Calendars.class, calendarsAnnotationHandler);
		mapOfAnnotationHandlers.put(MakeCalendarAnnotationHandler.class, makeCalendarAnnotationHandler);

		mapOfAnnotationHandlers.put(ModifiedDate.class, modifiedDateAnnotationHandler);
		mapOfAnnotationHandlers.put(CreatedDate.class, createdDateAnnotationHandler);
		mapOfAnnotationHandlers.put(ContentType.class, contentTypeAnnotationHandler);
		mapOfAnnotationHandlers.put(MaxAge.class, maxAgeAnnotationHandler);
		mapOfAnnotationHandlers.put(ContentLength.class, contentLengthAnnotationHandler);
		mapOfAnnotationHandlers.put(UniqueId.class, uniqueIdAnnotationHandler);
		mapOfAnnotationHandlers.put(CTag.class, cTagAnnotationHandler);
		mapOfAnnotationHandlers.put(ICalData.class, iCalDataAnnotationHandler);
		mapOfAnnotationHandlers.put(CalendarColor.class, calendarColorAnnotationHandler);
		mapOfAnnotationHandlers.put(CalendarOrder.class, calendarOrderAnnotationHandler);
		mapOfAnnotationHandlers.put(CalendarUserType.class, calendarUserTypeAnnotationHandler);		
		mapOfAnnotationHandlers.put(ContactData.class, contactDataAnnotationHandler);
		mapOfAnnotationHandlers.put(PrincipalSearch.class, principalSearchAnnotationHandler);		

		mapOfAnnotationHandlers.put(CalendarDateRangeQuery.class, calendarDateRangeQueryAnnotationHandler);
		mapOfAnnotationHandlers.put(FreeBusyQuery.class, freeBusyQueryAnnotationHandler);
		mapOfAnnotationHandlers.put(CalendarInvitations.class, calendarInvitationsAnnotationHandler);
		mapOfAnnotationHandlers.put(CalendarInvitationsCTag.class, calendarInvitationsCTagAnnotationHandler);
		mapOfAnnotationHandlers.put(Email.class, emailAnnotationHandler);
		mapOfAnnotationHandlers.put(SupportedComponentSets.class, supportedComponentSets);

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
		if (log.isTraceEnabled()) {
			log.trace("getResource: host: " + host + " - url:" + url);
		}

		AnnoCollectionResource hostRoot = locateHostRoot(host, HttpManager.request());
		if (hostRoot == null) {
			if (rootAnnotationHandler.getControllerMethods().isEmpty()) {
				log.warn("No @Root methods were found, so i cant find a root resource. Note that controller methods are displayed on startup");
			} else {
				log.warn("Could not find a root resource for host: " + host + " Using " + rootAnnotationHandler.getControllerMethods().size() + " root methods");
			}
			return null;
		}

		Resource r;
		url = stripContext(url);
		if (url.equals("/") || url.isEmpty()) {
			r = hostRoot;
		} else {
			Path path = Path.path(url);
			r = findFromRoot(hostRoot, path);
			if (log.isTraceEnabled()) {
				if (r == null) {
					log.trace("Resource not found: host=" + host + " path=" + path);
				} else {
					if (r instanceof AnnoResource) {
						AnnoResource ar = (AnnoResource) r;
						log.trace("Found AnnoResource: " + r.getClass() + "  for path=" + path + "  with source: " + ar.getSource());
					} else {
						log.trace("Found resource: " + r.getClass() + "  for path=" + path);
					}
				}
			}
		}
		return r;
	}

	public Resource findFromRoot(AnnoCollectionResource rootFolder, Path p) throws NotAuthorizedException, BadRequestException {
		CollectionResource col = rootFolder;
		Resource r = null;
		for (String s : p.getParts()) {
			if (col == null) {
				if (log.isTraceEnabled()) {
					log.trace("findFromRoot: collection is null, can't look for child: " + s);
				}
				return null;
			}
			r = col.child(s);
			if (r == null) {
				if (log.isTraceEnabled()) {
					log.trace("findFromRoot: Couldnt find child: " + s + " of parent: " + col.getName() + " with type: " + col.getClass());
				}
				return null;
			} else {
				if (log.isTraceEnabled()) {
					if (r instanceof AnnoResource) {
						AnnoResource ar = (AnnoResource) r;
						log.trace("findFromRoot: found a child: " + r.getName() + " with source type: " + ar.getSource().getClass());
					} else {
						log.trace("findFromRoot: found a child: " + r.getName() + " of type: " + r.getClass());
					}
				}
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

	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	public CalendarSearchService getCalendarSearchService() {
		return calendarSearchService;
	}

	public void setCalendarSearchService(CalendarSearchService calendarSearchService) {
		this.calendarSearchService = calendarSearchService;
	}

	/**
	 * If true authentication will be attempted as soon as the root resource is
	 * located
	 *
	 * @return
	 */
	public boolean isDoEarlyAuth() {
		return doEarlyAuth;
	}

	public void setDoEarlyAuth(boolean doEarlyAuth) {
		this.doEarlyAuth = doEarlyAuth;
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

	/**
	 * Get a context path which is definitely valid as a path. Ie it always
	 * begins with a slash, and ends with a slash, and is a single slash if
	 * representing the root
	 *
	 * @return
	 */
	public String getValidContextPath() {
		String s = getContextPath();
		if (s == null || s.isEmpty()) {
			return "/";
		}
		if (!s.endsWith("/")) {
			s += "/";
		}
		return s;
	}

	public String stripContext(String url) {
		if (this.contextPath != null && contextPath.length() > 0 && !contextPath.equals("/")) {
			String c;
			if (!contextPath.startsWith("/")) {
				c = "/" + contextPath;
			} else {
				c = contextPath;
			}
			url = url.replaceFirst(c, "");
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
		log.info("setControllers: " + controllers.size() + " parsing controllers...");
		for (Object controller : controllers) {
			log.info("Parse controller: " + controller.getClass());
			for (AnnotationHandler ah : mapOfAnnotationHandlers.values()) {
				log.info(" - controller: " + controller.getClass() + " handler: " + ah.getAnnoClass());
				ah.parseController(controller);
			}
		}
		log.info("Controller parsing complete. Listing found methods..");
		for (AnnotationHandler ah : mapOfAnnotationHandlers.values()) {
			log.info("Annotation: " + ah.getAnnoClass());
			List<ControllerMethod> list = ah.getControllerMethods();
			if (list == null || list.isEmpty()) {
				log.info("  No methods found");
			} else {
				for (ControllerMethod cm : ah.getControllerMethods()) {
					log.info("  method: " + cm.method);
				}
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
		if (in(m, Method.REPORT, Method.LOCK, Method.UNLOCK, Method.HEAD, Method.OPTIONS, Method.PROPPATCH, Method.ACL)) {
			return true;
		}
		AnnotationHandler ah = mapOfAnnotationHandlersByMethod.get(m);
		if (ah != null) {
			boolean b = ah.isCompatible(source);
			if (log.isTraceEnabled()) {
				log.trace("isCompatible: " + source + " - " + m + " = " + b);
			}
			return b;
		}
		log.warn("No annotation handler is configured for http method: " + m);
		return false;
	}

	/**
	 *
	 * @param sourceRes
	 * @param m
	 * @param otherValues - any other values to be provided which can be mapped
	 * onto method arguments
	 * @return
	 * @throws Exception
	 */
	public Object[] buildInvokeArgs(AnnoResource sourceRes, java.lang.reflect.Method m, Object... otherValues) throws Exception {
		return buildInvokeArgsExt(sourceRes, null, false, m, otherValues);
	}

	/**
	 *
	 * @param sourceRes
	 * @param mandatorySecondArg - if present will be used as second arg. Used
	 * by AccessControlListAnnotationHandler to always provide user to second
	 * arg, even when null
	 * @param forceUseSecondArg
	 * @param m
	 * @param otherValues
	 * @return
	 * @throws Exception
	 */
	public Object[] buildInvokeArgsExt(AnnoResource sourceRes, Object mandatorySecondArg, boolean forceUseSecondArg, java.lang.reflect.Method m, Object... otherValues) throws Exception {
		if (log.isTraceEnabled()) {
			log.trace("buildInvokeArgsExt: source=" + sourceRes.getSource() + " on method: " + m);
		}
		Request request = HttpManager.request();
		Response response = HttpManager.response();
		Auth auth = request.getAuthorization();
		AnnoPrincipalResource principal = null;
		if (auth != null) {
			if (auth.getTag() instanceof AnnoPrincipalResource) {
				principal = (AnnoPrincipalResource) auth.getTag();
			}
		}

		Object[] args = new Object[m.getParameterTypes().length];
		List list = new ArrayList();

		list.add(sourceRes.getSource()); // First argument MUST be the source object!!!

		// put otherValues on. Note these are more specific then parents so must be added first
		for (Object s : otherValues) {
			list.add(s);
			if (s instanceof AnnoResource) {
				AnnoResource otherRes = (AnnoResource) s;
				list.add(otherRes.getSource());
			}
		}

		// put this resource's parents on the stack
		AnnoResource r = sourceRes.getParent();
		while (r != null) {
			list.add(r.getSource());
			list.add(r);
			r = r.getParent();
		}

		for (int i = 0; i < m.getParameterTypes().length; i++) {
			if (i == 1 && forceUseSecondArg) {
				args[i] = mandatorySecondArg; // hack for methods which can have a null 2nd arg. Without this any other matching object would be provided
			} else {
				if (isPrincipalArg(m, i)) {
					principal = checkAuthentication(sourceRes, principal);
					if (principal != null) {
						args[i] = principal.source;
					} else {
						log.warn("Null principal provided for method: " + m);
						args[i] = null;
					}
				} else {
					Class type = m.getParameterTypes()[i];
					Object argValue;
					try {
						argValue = findArgValue(type, request, response, list);
					} catch (UnresolvableParameterException e) {
						log.warn("Could not resolve parameter: " + i + "  in method: " + m.getName());
						argValue = null;
					}
					args[i] = argValue;
				}
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
				if (o != null && type.isAssignableFrom(o.getClass())) {
					otherAvailValues.remove(o); // remove it so that we dont use same value for next param of same type
					return o;
				}
			}
		}
		if (log.isInfoEnabled()) {
			if (log.isDebugEnabled()) {
				log.info("Unknown parameter type: " + type);
				log.debug("Available types are:");
				log.debug(" - " + Request.class);
				log.debug(" - " + Response.class);
				for (Object o : otherAvailValues) {
					if (o != null) {
						log.debug(" - " + o.getClass());
					} else {
						log.debug(" - null");
					}
				}
			} else {
				log.info("Unknown parameter type: " + type + " Enable DEBUG level logging to see available objects");
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
	 * @param m
	 * @return
	 */
	public AnnoResource instantiate(Object childSource, AnnoCollectionResource parent, java.lang.reflect.Method m) {
		if (authenticateAnnotationHandler.canAuthenticate(childSource)) {
			return new AnnoPrincipalResource(this, childSource, parent);
		}
		if (parent instanceof AnnoPrincipalResource) {
			// Check for a Calendars method which takes this source as first arg
			if (calendarsAnnotationHandler.isCompatible(childSource)) {
				AnnoPrincipalResource p = (AnnoPrincipalResource) parent;
				return new AnnoCalendarHomeResource(this, childSource, p, calendarSearchService);
			}
		}
		if (m.getAnnotation(Calendars.class) != null) {
			return new AnnoCalendarResource(this, childSource, parent);
		}
		if (parent instanceof AnnoCalendarResource) {
			if (childrenOfAnnotationHandler.isCompatible(childSource) || childOfAnnotationHandler.isCompatible(childSource)) {
				// This is an edge case, shouldnt really have collections inside calendars
				return new AnnoCollectionResource(this, childSource, parent);
			} else {
				return new AnnoEventResource(this, childSource, parent);
			}
		}
		if (m.getAnnotation(AddressBooks.class) != null) {
			return new AnnoAddressBookResource(this, childSource, parent);
		}
		if (parent instanceof AnnoAddressBookResource) {
			return new AnnoContactResource(this, childSource, parent);
		}

		if (childrenOfAnnotationHandler.isCompatible(childSource) || childOfAnnotationHandler.isCompatible(childSource)) {
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
	 * @param lockInfo
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
		List<LockHolder> toRemove = new ArrayList<LockHolder>();
		while (it.hasNext()) {
			LockHolder o = it.next();
			if (o.getName().equals(name)) {
				toRemove.add(o);
				//it.remove();
			}
		}
		list.removeAll(toRemove);
		parent.removeLockHolder(name);
	}

	public Map<String, List<LockHolder>> getMapOfTempResources() {
		return mapOfTempResources;
	}

	public void setMapOfTempResources(Map<String, List<LockHolder>> mapOfTempResources) {
		this.mapOfTempResources = mapOfTempResources;
	}

	private boolean in(Method m, Method... methods) {
		for (Method listMethod : methods) {
			if (m.equals(listMethod)) {
				return true;
			}
		}
		return false;
	}

	private boolean isPrincipalArg(java.lang.reflect.Method m, int i) {
		Annotation[] arr = m.getParameterAnnotations()[i];
		Class annoType = io.milton.annotations.Principal.class;
		for (Annotation a : arr) {
			if (a.annotationType().equals(annoType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Process the source object (which may be a Collection of source objects),
	 * and for each one instantiate an AnnoResource and append it to the result
	 * set
	 *
	 *
	 * @param result - to append to
	 * @param sources - single source object, or multiple source objects in a
	 * Collection
	 * @param parent - the parent collection of these resource(s)
	 * @param cm - the controller method they were found by
	 */
	public void createAndAppend(Collection<AnnoResource> result, Object sources, AnnoCollectionResource parent, ControllerMethod cm) {
		if (sources == null) {
			// ignore
		} else if (sources instanceof Collection) {
			Collection l = (Collection) sources;
			for (Object item : l) {
				result.add(instantiate(item, parent, cm.method));
			}
		} else if (sources.getClass().isArray()) {
			Object[] arr = (Object[]) sources;
			for (Object item : arr) {
				result.add(instantiate(item, parent, cm.method));
			}
		} else {
			result.add(instantiate(sources, parent, cm.method));
		}
	}

	private AnnoPrincipalResource checkAuthentication(AnnoResource res, AnnoPrincipalResource principal) throws NotAuthorizedException {
		if (principal != null) {
			return principal;
		}
		if (doEarlyAuth) {
			if (authenticationService != null) {
				Request request = HttpManager.request();

				// Note that authentication will usually result in a call to getResource to find the principal..
				AuthenticationService.AuthStatus authStatus = authenticationService.authenticate(res, request);
				if (authStatus == null) {
					log.trace("Authentication not attempted");
					throw new NotAuthorizedException(res);
				} else {
					if (authStatus.loginFailed) {
						log.warn("Early authentication failed");
						throw new NotAuthorizedException(res);
					} else {
						log.trace("Early authentication succeeded");
						Auth auth = authStatus.auth;
						if (auth != null) {
							if (auth.getTag() instanceof AnnoPrincipalResource) {
								principal = (AnnoPrincipalResource) auth.getTag();
							}
						}
						return principal;
					}
				}
			}
		}
		return null;
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

	public CalendarDateRangeQueryAnnotationHandler getCalendarDateRangeQueryAnnotationHandler() {
		return calendarDateRangeQueryAnnotationHandler;
	}

	public CalendarInvitationsAnnotationHandler getCalendarInvitationsAnnotationHandler() {
		return calendarInvitationsAnnotationHandler;
	}

	public CalendarInvitationsCTagAnnotationHandler getCalendarInvitationsCTagAnnotationHandler() {
		return calendarInvitationsCTagAnnotationHandler;
	}

	public FreeBusyQueryAnnotationHandler getFreeBusyQueryAnnotationHandler() {
		return freeBusyQueryAnnotationHandler;
	}

	public UsersAnnotationHandler getUsersAnnotationHandler() {
		return usersAnnotationHandler;
	}
	
	
}
