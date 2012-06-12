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

package io.milton.http.json;

import io.milton.common.LogUtils;
import io.milton.common.Path;
import io.milton.event.EventManager;
import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.webdav.PropFindPropertyBuilder;
import io.milton.http.webdav.PropPatchSetter;
import io.milton.property.PropertyAuthoriser;
import io.milton.property.PropertySource;
import io.milton.resource.*;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class JsonResourceFactory implements ResourceFactory {

	private static final Logger log = LoggerFactory.getLogger(JsonResourceFactory.class);
	private final ResourceFactory wrapped;
	private final JsonPropFindHandler propFindHandler;
	private final JsonPropPatchHandler propPatchHandler;
	private final EventManager eventManager;
	private Long maxAgeSecsPropFind = null;
	private static final String DAV_FOLDER = "_DAV";
	private List<String> contentTypes = Arrays.asList("application/json", "application/x-javascript");
	private String methodParamName = "METHOD";

	public JsonResourceFactory(ResourceFactory wrapped, EventManager eventManager, JsonPropFindHandler propFindHandler, JsonPropPatchHandler propPatchHandler) {
		this.wrapped = wrapped;
		this.propFindHandler = propFindHandler;
		this.propPatchHandler = propPatchHandler;
		this.eventManager = eventManager;
		log.debug("created with: " + propFindHandler.getClass().getCanonicalName());
	}

	public JsonResourceFactory(ResourceFactory wrapped, EventManager eventManager, List<PropertySource> propertySources, PropPatchSetter patchSetter, PropertyAuthoriser permissionService) {
		this.wrapped = wrapped;
		this.eventManager = eventManager;
		log.debug("using property sources: " + propertySources.size());
		this.propFindHandler = new JsonPropFindHandler(new PropFindPropertyBuilder(propertySources));
		this.propPatchHandler = new JsonPropPatchHandler(patchSetter, permissionService, eventManager);
	}

	@Override
	public Resource getResource(String host, String sPath) throws NotAuthorizedException, BadRequestException {
		LogUtils.trace(log, "getResource", host, sPath);
		Path path = Path.path(sPath);
		Path parent = path.getParent();
		Request request = HttpManager.request();
		String encodedPath = request.getAbsolutePath();

		// This is to support a use case where a developer wants their resources to
		// be accessible through milton-json, but don't want to use DAV urls. Instead
		// they use a parameter and DO NOT implement PostableResource. 
		if (request.getMethod().equals(Method.POST)) {
			Resource wrappedResource = wrapped.getResource(host, sPath);
			if (wrappedResource != null && !(wrappedResource instanceof PostableResource)) {
				LogUtils.trace(log, "getResource: is post, and got a: ", wrappedResource.getClass());
				return new PostJsonResource(host, encodedPath, wrappedResource, methodParamName, this);
			}
		}
		if (request.getMethod().equals(Method.GET) && isMatchingContentType(request.getAcceptHeader())) {
			Resource wrappedResource = wrapped.getResource(host, sPath);
			if (wrappedResource != null) {
				log.trace("getResource: matches content type, and found wrapped resource");
				return wrapResource(host, wrappedResource, Method.PROPFIND.code, encodedPath);
			} else {
				LogUtils.trace(log, "getResource: is GET and matched type, but found no actual resource on", sPath);
			}
		}
		if (isMatchingPath(parent)) {
			log.trace("getResource: is matching path");
			Path resourcePath = parent.getParent();
			if (resourcePath != null) {
				String method = path.getName();
				Resource wrappedResource = wrapped.getResource(host, resourcePath.toString());
				if (wrappedResource != null) {
					Resource r = wrapResource(host, wrappedResource, method, encodedPath);
					LogUtils.trace(log, "returning a", r.getClass());
					return r;
				}
			}
		} else {
			log.trace("getResource: not matching path");
			return wrapped.getResource(host, sPath);
		}
		return null;
	}

	private boolean isMatchingPath(Path parent) {
		return parent != null && parent.getName() != null && parent.getName().equals(DAV_FOLDER);
	}

	public Resource wrapResource(String host, Resource wrappedResource, String method, String href) {
		LogUtils.trace(log, "wrapResource: " , method);
		if (Request.Method.PROPFIND.code.equals(method)) {
			if (wrappedResource instanceof PropFindableResource) {
				return new PropFindJsonResource((PropFindableResource) wrappedResource, propFindHandler, href, maxAgeSecsPropFind);
			} else {
				log.warn("Located a resource for PROPFIND path, but it does not implement PropFindableResource: " + wrappedResource.getClass());
			}
		}
		if (Request.Method.PROPPATCH.code.equals(method)) {
			return new PropPatchJsonResource(wrappedResource, propPatchHandler, href);
		}
		if (Request.Method.PUT.code.equals(method)) {
			if (wrappedResource instanceof PutableResource) {
				return new PutJsonResource((PutableResource) wrappedResource, href);
			}
		}
		if (Request.Method.MKCOL.code.equals(method)) {
			if (wrappedResource instanceof MakeCollectionableResource) {
				return new MkcolJsonResource((MakeCollectionableResource) wrappedResource, href, eventManager);
			}
		}
		if (Request.Method.COPY.code.equals(method)) {
			if (wrappedResource instanceof CopyableResource) {
				return new CopyJsonResource(host, (CopyableResource) wrappedResource, wrapped);
			}
		}
		if (Request.Method.MOVE.code.equals(method)) {
			if (wrappedResource instanceof MoveableResource) {
				return new MoveJsonResource(host, (MoveableResource) wrappedResource, wrapped);
			}
		}
		return wrappedResource;
	}

	public JsonPropFindHandler getPropFindHandler() {
		return propFindHandler;
	}

	public JsonPropPatchHandler getPropPatchHandler() {
		return propPatchHandler;
	}

	public EventManager getEventManager() {
		return eventManager;
	}

	public Long getMaxAgeSecsPropFind() {
		return maxAgeSecsPropFind;
	}

	public void setMaxAgeSecsPropFind(Long maxAgeSecsPropFind) {
		this.maxAgeSecsPropFind = maxAgeSecsPropFind;
	}

	private boolean isMatchingContentType(String acceptsHeader) {
		if (acceptsHeader != null) {
			if (contentTypes != null) {
				for (String s : contentTypes) {
					if (acceptsHeader.contains(s)) {
						return true;
					}
				}
			} else {
				log.trace("no configured content types");
			}
		} else {
			log.trace("No accepts header in the request");
		}
		log.trace("isMatchingContentType: not matching type: {}", acceptsHeader);
		return false;
	}

	public List<String> getContentTypes() {
		return contentTypes;
	}

	public void setContentTypes(List<String> contentTypes) {
		this.contentTypes = contentTypes;
	}
}
