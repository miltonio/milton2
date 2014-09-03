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

import io.milton.resource.Resource;
import io.milton.common.Path;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used the decorator pattern to add support for .wellknown paths to a custom
 * resource factory.
 * 
 * By integrating this into your application it will "overlay" some additional
 * synthetic resources (ie url's) on your application
 * 
 * This is required for iCal5+ support for caldav
 * 
 * http://tools.ietf.org/html/draft-daboo-srv-caldav-10#page-5
 * http://tools.ietf.org/html/rfc5785
 * http://hueniverse.com/2010/04/rfc-5785-defining-well-known-uris/
 *
 * @author brad
 */
public class WellKnownResourceFactory implements ResourceFactory {

	public static final String URI_PREFIX = "/.well-known";
	
	private final ResourceFactory wrapped;
	
	private List<WellKnownHandler> wellKnownHandlers;
	
	private Map<String,WellKnownHandler> mapOfWellKnownHandlers = new HashMap<String, WellKnownHandler>();

	public WellKnownResourceFactory(ResourceFactory wrapped) {
		this.wrapped = wrapped;
	}
	
	public void setWellKnownHandlers(List<WellKnownHandler> wellKnownHandlers) {
		this.wellKnownHandlers = wellKnownHandlers;
		mapOfWellKnownHandlers.clear();
		for( WellKnownHandler h : wellKnownHandlers ) {
			addHandler(h);
		}		
	}

	public List<WellKnownHandler> getWellKnownHandlers() {
		return wellKnownHandlers;
	}
	
	
//		
//	public WellKnownResourceFactory(ResourceFactory wrapped) {
//		this.wrapped = wrapped;
//	}	
	
	@Override
	public Resource getResource(String host, String sPath) throws NotAuthorizedException, BadRequestException {
		if( sPath.startsWith(URI_PREFIX)) {
			Path path = Path.path(sPath);
			path = path.getStripFirst();
			WellKnownHandler wellKnown = mapOfWellKnownHandlers.get(path.getFirst());
			if( wellKnown != null ) {
				Resource hostRes = wrapped.getResource(host, "/");
				if( hostRes != null ) {
					return wellKnown.locateWellKnownResource(hostRes);
				}
			}
		}
		return wrapped.getResource(host, sPath);
	}
	
	public final void addHandler(WellKnownHandler handler) {
		mapOfWellKnownHandlers.put(handler.getWellKnownName(), handler);
	}
	
	/**
	 * Locates a resource for a .well-known/XXX path
	 */
	public interface WellKnownHandler {
		/**
		 * Identifies the part of the path following .well-known which will map to this handler
		 * 
		 * @return 
		 */
		String getWellKnownName();
		
		/**
		 * Find a resource which will handle the well-known request. This should
		 * generally redirect to the appropriate location. It should not perform
		 * the role of the service being looked up.
		 */
		Resource locateWellKnownResource(Resource hostRes);
	}
}
