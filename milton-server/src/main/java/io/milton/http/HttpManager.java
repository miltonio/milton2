/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.milton.http;

import io.milton.common.Utils;
import io.milton.resource.Resource;
import io.milton.http.entity.DefaultEntityTransport;
import io.milton.http.entity.EntityTransport;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.http11.Bufferable;
import io.milton.http.http11.CustomPostHandler;
import io.milton.http.http11.DefaultHttp11ResponseHandler;
import io.milton.http.http11.Http11ResponseHandler;
import io.milton.http.http11.auth.ExpiredNonceRemover;
import io.milton.http.http11.auth.Nonce;
import io.milton.http.http11.auth.NonceProvider;
import io.milton.http.http11.auth.SimpleMemoryNonceProvider;
import io.milton.http.webdav.DefaultWebDavResponseHandler;
import io.milton.http.webdav.WebDavResponseHandler;
import io.milton.property.PropertyAuthoriser;
import io.milton.property.PropertyHandler;
import io.milton.common.Stoppable;
import io.milton.event.EventManager;
import io.milton.event.EventManagerImpl;
import io.milton.event.RequestEvent;
import io.milton.event.ResponseEvent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Milton's main class. All the servlets and stuff is just fluff, this is where
 * stuff really starts to happen
 *
 *
 * @author brad
 */
public class HttpManager {

	private static final Logger log = LoggerFactory.getLogger(HttpManager.class);

	public static String decodeUrl(String s) {
		return Utils.decodePath(s);
	}
	private static final ThreadLocal<Request> tlRequest = new ThreadLocal<Request>();
	private static final ThreadLocal<Response> tlResponse = new ThreadLocal<Response>();

	public static Request request() {
		return tlRequest.get();
	}

	public static Response response() {
		return tlResponse.get();
	}
	private final ProtocolHandlers handlers;
	private Map<String, Handler> methodHandlers = new ConcurrentHashMap<String, Handler>();
	List<Filter> filters = new ArrayList<Filter>();
	List<EventListener> eventListeners = new ArrayList<EventListener>();
	protected final ResourceFactory resourceFactory;
	protected final Http11ResponseHandler responseHandler;
	private SessionAuthenticationHandler sessionAuthenticationHandler;
	private PropertyAuthoriser propertyPermissionService;
	private EventManager eventManager = new EventManagerImpl();
	private final List<Stoppable> shutdownHandlers = new CopyOnWriteArrayList<Stoppable>();
	private EntityTransport entityTransport;

	/**
	 * Creates the manager with a DefaultResponseHandler
	 *
	 * @param resourceFactory
	 */
	public HttpManager(ResourceFactory resourceFactory) {
		if (resourceFactory == null) {
			throw new NullPointerException("resourceFactory cannot be null");
		}
		Map<UUID, Nonce> nonces = new ConcurrentHashMap<UUID, Nonce>();
		int nonceValiditySeconds = 60 * 60 * 24;
		ExpiredNonceRemover expiredNonceRemover = new ExpiredNonceRemover(nonces, nonceValiditySeconds);
		NonceProvider nonceProvider = new SimpleMemoryNonceProvider(nonceValiditySeconds, expiredNonceRemover, nonces);
		AuthenticationService authenticationService = new AuthenticationService(nonceProvider);
		
		this.resourceFactory = resourceFactory;
		DefaultWebDavResponseHandler webdavResponseHandler = new DefaultWebDavResponseHandler(authenticationService);
		this.responseHandler = webdavResponseHandler;
		HandlerHelper handlerHelper = new HandlerHelper(authenticationService);
		this.handlers = new ProtocolHandlers(webdavResponseHandler, handlerHelper, resourceFactory);

		entityTransport = new DefaultEntityTransport(); // default implementation, can be overridden with setter
		
		initHandlers();

		shutdownHandlers.add(expiredNonceRemover);

		expiredNonceRemover.start();

	}

	public HttpManager(ResourceFactory resourceFactory, AuthenticationService authenticationService) {
		if (resourceFactory == null) {
			throw new NullPointerException("resourceFactory cannot be null");
		}
		this.resourceFactory = resourceFactory;
		DefaultWebDavResponseHandler webdavResponseHandler = new DefaultWebDavResponseHandler(authenticationService);
		this.responseHandler = webdavResponseHandler;
		HandlerHelper handlerHelper = new HandlerHelper(authenticationService);
		this.handlers = new ProtocolHandlers(webdavResponseHandler, handlerHelper, resourceFactory);

		entityTransport = new DefaultEntityTransport(); // default implementation, can be overridden with setter
		
		initHandlers();
	}

	public HttpManager(ResourceFactory resourceFactory, WebDavResponseHandler responseHandler, AuthenticationService authenticationService) {
		if (resourceFactory == null) {
			throw new NullPointerException("resourceFactory cannot be null");
		}
		this.resourceFactory = resourceFactory;
		this.responseHandler = responseHandler;
		HandlerHelper handlerHelper = new HandlerHelper(authenticationService);
		this.handlers = new ProtocolHandlers(responseHandler, handlerHelper, resourceFactory);

		entityTransport = new DefaultEntityTransport(); // default implementation, can be overridden with setter
		
		initHandlers();
	}

	public HttpManager(ResourceFactory resourceFactory, WebDavResponseHandler responseHandler, ProtocolHandlers handlers) {
		if (resourceFactory == null) {
			throw new NullPointerException("resourceFactory cannot be null");
		}
		this.resourceFactory = resourceFactory;
		this.responseHandler = responseHandler;
		this.handlers = handlers;

		entityTransport = new DefaultEntityTransport(); // default implementation, can be overridden with setter
		
		initHandlers();
	}

	public void sendResponseEntity(Response response) throws Exception {
		entityTransport.sendResponseEntity(response);
	}

	public void closeResponse(Response response) {
		entityTransport.closeResponse(response);

	}

	private void initHandlers() {
		for (HttpExtension ext : handlers) {
			for (Handler h : ext.getHandlers()) {
				for (String m : h.getMethods()) {
					this.methodHandlers.put(m, h);
				}
			}
		}
		// The standard filter must always be there, its what invokes the main milton processing
		filters.add(createStandardFilter());
	}

	public Handler getMethodHandler(Request.Method m) {
		return methodHandlers.get(m.code);
	}

	public ResourceFactory getResourceFactory() {
		return resourceFactory;
	}

	public SessionAuthenticationHandler getSessionAuthenticationHandler() {
		return sessionAuthenticationHandler;
	}

	public void setSessionAuthenticationHandler(SessionAuthenticationHandler sessionAuthenticationHandler) {
		this.sessionAuthenticationHandler = sessionAuthenticationHandler;
	}

	/**
	 * @deprecated - use an AuthenticationHandler instead
	 *
	 * @param request
	 * @return - if no SessionAuthenticationHandler has been set returns null.
	 * Otherwise, calls getSessionAuthentication on it and returns the result
	 *
	 *
	 */
	@Deprecated
	public Auth getSessionAuthentication(Request request) {
		if (this.sessionAuthenticationHandler == null) {
			return null;
		}
		return this.sessionAuthenticationHandler.getSessionAuthentication(request);
	}

	public void process(Request request, Response response) {
		if (log.isInfoEnabled()) {
			log.info(request.getMethod() + " :: " + request.getAbsoluteUrl() + " - " + request.getAbsoluteUrl());
		}
		tlRequest.set(request);
		tlResponse.set(response);
		try {
			fireRequestEvent(request);
		} catch (ConflictException ex) {
			responseHandler.respondConflict(null, response, request, null);
		} catch (BadRequestException ex) {
			responseHandler.respondBadRequest(null, response, request);
		} catch (NotAuthorizedException ex) {
			responseHandler.respondUnauthorised(null, response, request);
		}
		try {
			FilterChain chain = new FilterChain(this);
			chain.process(request, response);
			try {
				fireResponseEvent(request, response);
			} catch (ConflictException ex) {
				log.warn("exception thrown from event handler after response is complete", ex);
			} catch (BadRequestException ex) {
				log.warn("exception thrown from event handler after response is complete", ex);
			} catch (NotAuthorizedException ex) {
				log.warn("exception thrown from event handler after response is complete", ex);
			}
		} finally {
			tlRequest.remove();
			tlResponse.remove();
		}
	}

	protected Filter createStandardFilter() {
		return new StandardFilter();
	}

	public void addFilter(int pos, Filter filter) {
		filters.add(pos, filter);
	}

	public void addEventListener(EventListener l) {
		eventListeners.add(l);
	}

	public void removeEventListener(EventListener l) {
		eventListeners.remove(l);
	}

	public void onProcessResourceFinish(Request request, Response response, Resource resource, long duration) {
		for (EventListener l : eventListeners) {
			l.onProcessResourceFinish(request, response, resource, duration);
		}
	}

	public void onProcessResourceStart(Request request, Response response, Resource resource) {
		for (EventListener l : eventListeners) {
			l.onProcessResourceStart(request, response, resource);
		}
	}

	public void onPost(Request request, Response response, Resource resource, Map<String, String> params, Map<String, FileItem> files) {
		for (EventListener l : eventListeners) {
			l.onPost(request, response, resource, params, files);
		}
	}

	public void onGet(Request request, Response response, Resource resource, Map<String, String> params) {
		for (EventListener l : eventListeners) {
			l.onGet(request, response, resource, params);
		}
	}

	public List<Filter> getFilters() {
		ArrayList<Filter> col = new ArrayList<Filter>(filters);
		return col;
	}

	public void setFilters(List<Filter> filters) {
		this.filters = filters;
		filters.add(createStandardFilter());
	}

	public void setEventListeners(List<EventListener> eventListeners) {
		this.eventListeners = eventListeners;
	}

	public Collection<Handler> getAllHandlers() {
		return this.methodHandlers.values();
	}

	public Http11ResponseHandler getResponseHandler() {
		return responseHandler;
	}

	public ProtocolHandlers getHandlers() {
		return handlers;
	}

	public PropertyAuthoriser getPropertyPermissionService() {
		return propertyPermissionService;
	}

	public void setPropertyPermissionService(PropertyAuthoriser propertyPermissionService) {
		log.trace("setPropertyPermissionService: " + propertyPermissionService.getClass().getCanonicalName());
		this.propertyPermissionService = propertyPermissionService;
		for (Handler h : methodHandlers.values()) {
			if (h instanceof PropertyHandler) {
				PropertyHandler ph = (PropertyHandler) h;
				log.trace("set propertyPermissionService on: " + ph.getClass().getCanonicalName());
				ph.setPermissionService(propertyPermissionService);
			}
		}
	}

	public boolean isEnableExpectContinue() {
		return handlers.isEnableExpectContinue();
	}

	public void setEnableExpectContinue(boolean enableExpectContinue) {
		handlers.setEnableExpectContinue(enableExpectContinue);
	}

	public EventManager getEventManager() {
		return eventManager;
	}

	public void setEventManager(EventManager eventManager) {
		this.eventManager = eventManager;
	}

	private void fireRequestEvent(Request request) throws ConflictException, BadRequestException, NotAuthorizedException {
		if (eventManager == null) {
			return;
		}
		eventManager.fireEvent(new RequestEvent(request));
	}

	private void fireResponseEvent(Request request, Response response) throws ConflictException, BadRequestException, NotAuthorizedException {
		if (eventManager == null) {
			return;
		}
		eventManager.fireEvent(new ResponseEvent(request, response));

	}

	public List<CustomPostHandler> getCustomPostHandlers() {
		List<CustomPostHandler> list = new ArrayList<CustomPostHandler>();
		for (HttpExtension p : this.handlers) {
			if (p.getCustomPostHandlers() != null) {
				for (CustomPostHandler h : p.getCustomPostHandlers()) {
					list.add(h);
				}
			}
		}
		return list;
	}

	public final void shutdown() {
		for (Stoppable stoppable : shutdownHandlers) {
			try {
				stoppable.stop();
			} catch (Throwable e) {
				log.warn("Exception stopping: " + stoppable.getClass(), e);
			}
		}
	}

	public DefaultHttp11ResponseHandler.BUFFERING getBuffering() {
		if (this.responseHandler instanceof Bufferable) {
			Bufferable hrh = (Bufferable) responseHandler;
			return hrh.getBuffering();
		} else {
			return null; // unknown
		}
	}

	public void setBuffering(DefaultHttp11ResponseHandler.BUFFERING buffering) {
		if (this.responseHandler instanceof Bufferable) {
			Bufferable hrh = (Bufferable) responseHandler;
			hrh.setBuffering(buffering);
		} else {
			throw new RuntimeException("Can't set buffering on unsupported Http11ResponseHandler: " + responseHandler.getClass());
		}
	}

	public EntityTransport getEntityTransport() {
		return entityTransport;
	}

	public void setEntityTransport(EntityTransport entityTransport) {
		this.entityTransport = entityTransport;
	}		
}
