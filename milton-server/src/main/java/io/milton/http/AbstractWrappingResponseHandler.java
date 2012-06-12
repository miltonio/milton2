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

import io.milton.resource.PropFindableResource;
import io.milton.resource.GetableResource;
import io.milton.resource.Resource;
import io.milton.http.Response.Status;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.http.quota.StorageChecker.StorageErrorReason;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.http11.Bufferable;
import io.milton.http.http11.DefaultHttp11ResponseHandler.BUFFERING;
import io.milton.http.webdav.PropFindResponse;
import io.milton.http.webdav.WebDavResponseHandler;

/**
 * Response Handler which wraps another
 *
 * @author brad
 */
public abstract class AbstractWrappingResponseHandler implements WebDavResponseHandler, Bufferable {

	private static final Logger log = LoggerFactory.getLogger(AbstractWrappingResponseHandler.class);
	/**
	 * The underlying respond handler which takes care of actually generating
	 * content
	 */
	protected WebDavResponseHandler wrapped;

	public AbstractWrappingResponseHandler() {
	}

	public AbstractWrappingResponseHandler(WebDavResponseHandler wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public BUFFERING getBuffering() {
		if (wrapped instanceof Bufferable) {
			Bufferable b = (Bufferable) wrapped;
			return b.getBuffering();
		} else {
			return null;
		}
	}

	@Override
	public void setBuffering(BUFFERING buffering) {
		if (wrapped instanceof Bufferable) {
			Bufferable b = (Bufferable) wrapped;
			b.setBuffering(buffering);
		} else {
			throw new IllegalStateException("The wrapped response handle is not Bufferable. Is a: " + wrapped.getClass());
		}
	}

	@Override
	public String generateEtag(Resource r) {
		return wrapped.generateEtag(r);
	}

	@Override
	public void respondContent(Resource resource, Response response, Request request, Map<String, String> params) throws NotAuthorizedException, BadRequestException, NotFoundException {
		wrapped.respondContent(resource, response, request, params);
	}

	public void setWrapped(WebDavResponseHandler wrapped) {
		this.wrapped = wrapped;
	}

	public WebDavResponseHandler getWrapped() {
		return wrapped;
	}

	@Override
	public void respondNoContent(Resource resource, Response response, Request request) {
		wrapped.respondNoContent(resource, response, request);
	}

	@Override
	public void respondPartialContent(GetableResource resource, Response response, Request request, Map<String, String> params, Range range) throws NotAuthorizedException, BadRequestException, NotFoundException {
		wrapped.respondPartialContent(resource, response, request, params, range);
	}

	@Override
	public void respondCreated(Resource resource, Response response, Request request) {
		wrapped.respondCreated(resource, response, request);
	}

	public void respondUnauthorised(Resource resource, Response response, Request request) {
		wrapped.respondUnauthorised(resource, response, request);
	}

	public void respondMethodNotImplemented(Resource resource, Response response, Request request) {
		wrapped.respondMethodNotImplemented(resource, response, request);
	}

	public void respondMethodNotAllowed(Resource res, Response response, Request request) {
		wrapped.respondMethodNotAllowed(res, response, request);
	}

	public void respondConflict(Resource resource, Response response, Request request, String message) {
		wrapped.respondConflict(resource, response, request, message);
	}

	public void respondRedirect(Response response, Request request, String redirectUrl) {
		wrapped.respondRedirect(response, request, redirectUrl);
	}

	public void responseMultiStatus(Resource resource, Response response, Request request, List<HrefStatus> statii) {
		wrapped.responseMultiStatus(resource, response, request, statii);
	}

	public void respondNotModified(GetableResource resource, Response response, Request request) {
		log.trace("respondNotModified");
		wrapped.respondNotModified(resource, response, request);
	}

	public void respondNotFound(Response response, Request request) {
		wrapped.respondNotFound(response, request);
	}

	public void respondWithOptions(Resource resource, Response response, Request request, List<String> methodsAllowed) {
		wrapped.respondWithOptions(resource, response, request, methodsAllowed);
	}

	public void respondHead(Resource resource, Response response, Request request) {
		wrapped.respondHead(resource, response, request);
	}

	public void respondExpectationFailed(Response response, Request request) {
		wrapped.respondExpectationFailed(response, request);
	}

	public void respondBadRequest(Resource resource, Response response, Request request) {
		wrapped.respondBadRequest(resource, response, request);
	}

	public void respondForbidden(Resource resource, Response response, Request request) {
		wrapped.respondForbidden(resource, response, request);
	}

	public void respondDeleteFailed(Request request, Response response, Resource resource, Status status) {
		wrapped.respondDeleteFailed(request, response, resource, status);
	}

	public void respondPropFind(List<PropFindResponse> propFindResponses, Response response, Request request, PropFindableResource pfr) {
		wrapped.respondPropFind(propFindResponses, response, request, pfr);
	}

	public void respondPropFind(List<PropFindResponse> propFindResponses, Response response, Request request, Resource r) {
		wrapped.respondPropFind(propFindResponses, response, request, r);
	}

	public void respondServerError(Request request, Response response, String reason) {
		wrapped.respondServerError(request, response, reason);
	}

	public void respondInsufficientStorage(Request request, Response response, StorageErrorReason storageErrorReason) {
		wrapped.respondInsufficientStorage(request, response, storageErrorReason);
	}

	public void respondLocked(Request request, Response response, Resource existingResource) {
		wrapped.respondLocked(request, response, existingResource);
	}

	public void respondPreconditionFailed(Request request, Response response, Resource resource) {
		wrapped.respondPreconditionFailed(request, response, resource);
	}
}
