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
package io.milton.http;

import io.milton.common.Utils;
import io.milton.resource.Resource;
import io.milton.http.entity.EntityTransport;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.http11.CustomPostHandler;
import io.milton.http.http11.Http11ResponseHandler;
import io.milton.http.webdav.WebDavResponseHandler;
import io.milton.common.Stoppable;
import io.milton.event.EventManager;
import io.milton.event.RequestEvent;
import io.milton.event.ResponseEvent;
import io.milton.http.Request.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

	private static final ThreadLocal<Request> tlRequest = new ThreadLocal<Request>();
	private static final ThreadLocal<Response> tlResponse = new ThreadLocal<Response>();
	private static final Map<Thread,RequestInfo> mapOfRequestsByThread = new ConcurrentHashMap<Thread, RequestInfo>();

	public static RequestInfo getRequestDataForThread(Thread th) {
		return mapOfRequestsByThread.get(th);
	}
	
	public static String decodeUrl(String s) {
		return Utils.decodePath(s);
	}

	public static Request request() {
		return tlRequest.get();
	}

	public static Response response() {
		return tlResponse.get();
	}
	private final ProtocolHandlers handlers;
	private final List<Filter> filters;
	private final List<EventListener> eventListeners = new ArrayList<EventListener>();
	private final ResourceFactory resourceFactory;
	private final Http11ResponseHandler responseHandler;
	private final EventManager eventManager;
	private final List<Stoppable> shutdownHandlers;
	private final EntityTransport entityTransport;
	private Map<String, Handler> methodHandlers;

	/**
	 * Instead of using this constructor directly, consider using the
	 * HttpManagerConfig builder class
	 *
	 * @param resourceFactory
	 * @param responseHandler
	 * @param handlers
	 * @param entityTransport
	 * @param filters
	 * @param eventManager
	 * @param shutdownHandlers
	 */
	public HttpManager(ResourceFactory resourceFactory, WebDavResponseHandler responseHandler, ProtocolHandlers handlers, EntityTransport entityTransport, List<Filter> filters, EventManager eventManager, List<Stoppable> shutdownHandlers) {
		this.responseHandler = responseHandler;
		this.handlers = handlers;
		this.resourceFactory = resourceFactory;
		this.entityTransport = entityTransport;
		this.filters = filters;
		this.eventManager = eventManager;
		this.shutdownHandlers = shutdownHandlers;
		initHandlers();
	}

	private void initHandlers() {
		this.methodHandlers = new ConcurrentHashMap<String, Handler>();
		for (HttpExtension ext : handlers) {
			for (Handler h : ext.getHandlers()) {
				for (String m : h.getMethods()) {
					this.methodHandlers.put(m, h);
				}
			}
		}
	}

	public void sendResponseEntity(Response response) throws Exception {
		entityTransport.sendResponseEntity(response);
	}

	public void closeResponse(Response response) {
		entityTransport.closeResponse(response);

	}

	public Handler getMethodHandler(Request.Method m) {
		return methodHandlers.get(m.code);
	}

	public ResourceFactory getResourceFactory() {
		return resourceFactory;
	}

	public void process(Request request, Response response) {
		if( request == null ) {
			throw new RuntimeException("request is null");					
		}
		if (log.isInfoEnabled()) {
			log.info(request.getMethod() + " :: " + request.getAbsoluteUrl() + " start");
		}

		try {
			setThreadAffinityData(request, response);
			try {
				fireRequestEvent(request);
			} catch (ConflictException ex) {
				responseHandler.respondConflict(null, response, request, null);
			} catch (BadRequestException ex) {
				responseHandler.respondBadRequest(null, response, request);
			} catch (NotAuthorizedException ex) {
				responseHandler.respondUnauthorised(null, response, request);
			}
			
			FilterChain chain = new FilterChain(this);
			long tm = System.currentTimeMillis();
			chain.process(request, response);
			try {
				tm = System.currentTimeMillis() - tm;
				log.info(request.getMethod() + " :: " + request.getAbsoluteUrl() + " finished " + tm + "ms");
				fireResponseEvent(request, response, tm);				
			} catch (ConflictException ex) {
				log.warn("exception thrown from event handler after response is complete", ex);
			} catch (BadRequestException ex) {
				log.warn("exception thrown from event handler after response is complete", ex);
			} catch (NotAuthorizedException ex) {
				log.warn("exception thrown from event handler after response is complete", ex);
			}
		} finally {
			clearThreadAffinity();
		}
	}

	private void clearThreadAffinity() {
		tlRequest.remove();
		tlResponse.remove();
		try {
			mapOfRequestsByThread.remove(Thread.currentThread());
		} catch (Throwable e) {
			log.info("Couldnt clear thread affinity request data");
		}
	}

	private void setThreadAffinityData(Request request, Response response) {
		tlRequest.set(request);
		tlResponse.set(response);
		try {
			RequestInfo info = new RequestInfo(request.getMethod(), request.getAbsoluteUrl(), new Date());
			mapOfRequestsByThread.put(Thread.currentThread(), info);
		} catch (Throwable e) {
			log.info("Couldnt set thread affinity request data");
		}
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

	public Collection<Handler> getAllHandlers() {
		return this.methodHandlers.values();
	}

	public Http11ResponseHandler getResponseHandler() {
		return responseHandler;
	}

	public ProtocolHandlers getHandlers() {
		return handlers;
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

	private void fireRequestEvent(Request request) throws ConflictException, BadRequestException, NotAuthorizedException {
		if (eventManager == null) {
			return;
		}
		eventManager.fireEvent(new RequestEvent(request));
	}

	private void fireResponseEvent(Request request, Response response, long duration) throws ConflictException, BadRequestException, NotAuthorizedException {
		if (eventManager == null) {
			return;
		}
		eventManager.fireEvent(new ResponseEvent(request, response, duration));

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

	public EntityTransport getEntityTransport() {
		return entityTransport;
	}
	
	public class RequestInfo {
		private final Method method;
		private final String url;
		private final Date started;

		public RequestInfo(Method method, String url, Date started) {
			this.method = method;
			this.url = url;
			this.started = started;
		}

		public Method getMethod() {
			return method;
		}

		public Date getStarted() {
			return started;
		}

		public String getUrl() {
			return url;
		}
		
		public long getDurationMillis() {
			return System.currentTimeMillis() - started.getTime();
		}
	}
}
