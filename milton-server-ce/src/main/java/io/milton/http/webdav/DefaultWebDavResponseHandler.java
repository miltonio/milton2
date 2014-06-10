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
package io.milton.http.webdav;

import io.milton.resource.GetableResource;
import io.milton.http.HrefStatus;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.resource.Resource;
import io.milton.http.Response;
import io.milton.http.Response.Status;
import io.milton.common.Utils;
import io.milton.http.entity.MultiStatusEntity;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.http.http11.Bufferable;
import io.milton.http.http11.DefaultHttp11ResponseHandler.BUFFERING;
import io.milton.http.http11.Http11ResponseHandler;
import io.milton.http.quota.StorageChecker.StorageErrorReason;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class DefaultWebDavResponseHandler implements WebDavResponseHandler, Bufferable {

	private static final Logger log = LoggerFactory.getLogger(DefaultWebDavResponseHandler.class);
	protected final Http11ResponseHandler wrapped;
	protected final ResourceTypeHelper resourceTypeHelper;
	protected final PropFindXmlGenerator propFindXmlGenerator;

	public DefaultWebDavResponseHandler(Http11ResponseHandler wrapped, ResourceTypeHelper resourceTypeHelper, PropFindXmlGenerator propFindXmlGenerator) {
		this.wrapped = wrapped;
		this.resourceTypeHelper = resourceTypeHelper;
		this.propFindXmlGenerator = propFindXmlGenerator;
	}

	@Override
	public String generateEtag(Resource r) {
		return wrapped.generateEtag(r);
	}

	@Override
	public void respondWithOptions(Resource resource, Response response, Request request, List<String> methodsAllowed) {
		List<String> supportedLevels = resourceTypeHelper.getSupportedLevels(resource);
		String s = Utils.toCsv(supportedLevels);
		response.setDavHeader(s);
		response.setNonStandardHeader("MS-Author-Via", "DAV");
		wrapped.respondWithOptions(resource, response, request, methodsAllowed); // Note that setting content length must be done last for tomcat5		
	}

	@Override
	public void responseMultiStatus(Resource resource, Response response, Request request, List<HrefStatus> statii) {
		response.setStatus(Response.Status.SC_MULTI_STATUS);
		response.setDateHeader(new Date());
		response.setContentTypeHeader(Response.XML);
		List<String> supportedLevels = resourceTypeHelper.getSupportedLevels(resource);
		String s = Utils.toCsv(supportedLevels);
		response.setDavHeader(s);
		response.setEntity(new MultiStatusEntity(statii));
	}

	@Override
	public void respondNoContent(Resource resource, Response response, Request request) {
		wrapped.respondNoContent(resource, response, request);
	}

	@Override
	public void respondContent(Resource resource, Response response, Request request, Map<String, String> params) throws NotAuthorizedException, BadRequestException, NotFoundException {
		wrapped.respondContent(resource, response, request, params);
	}

	@Override
	public void respondPartialContent(GetableResource resource, Response response, Request request, Map<String, String> params, Range range) throws NotAuthorizedException, BadRequestException, NotFoundException {
		wrapped.respondPartialContent(resource, response, request, params, range);
	}
	
	@Override
	public void respondPartialContent(GetableResource resource, Response response, Request request, Map<String, String> params, List<Range> ranges) throws NotAuthorizedException, BadRequestException, NotFoundException {
		wrapped.respondPartialContent(resource, response, request, params, ranges);
	}	

	@Override
	public void respondCreated(Resource resource, Response response, Request request) {
		wrapped.respondCreated(resource, response, request);
	}

	@Override
	public void respondUnauthorised(Resource resource, Response response, Request request) {
		wrapped.respondUnauthorised(resource, response, request);
	}

	@Override
	public void respondMethodNotImplemented(Resource resource, Response response, Request request) {
		wrapped.respondMethodNotImplemented(resource, response, request);
	}

	@Override
	public void respondMethodNotAllowed(Resource res, Response response, Request request) {
		wrapped.respondMethodNotAllowed(res, response, request);
	}

	@Override
	public void respondConflict(Resource resource, Response response, Request request, String message) {
		wrapped.respondConflict(resource, response, request, message);
	}

	@Override
	public void respondRedirect(Response response, Request request, String redirectUrl) {
		wrapped.respondRedirect(response, request, redirectUrl);
	}

	@Override
	public void respondNotModified(GetableResource resource, Response response, Request request) {
		if (log.isTraceEnabled()) {
			log.trace("respondNotModified: " + wrapped.getClass().getCanonicalName());
		}
		wrapped.respondNotModified(resource, response, request);
	}

	@Override
	public void respondNotFound(Response response, Request request) {
		wrapped.respondNotFound(response, request);
	}

	@Override
	public void respondHead(Resource resource, Response response, Request request) {
		wrapped.respondHead(resource, response, request);
	}

	@Override
	public void respondExpectationFailed(Response response, Request request) {
		wrapped.respondExpectationFailed(response, request);
	}

	@Override
	public void respondBadRequest(Resource resource, Response response, Request request) {
		wrapped.respondBadRequest(resource, response, request);
	}

	@Override
	public void respondForbidden(Resource resource, Response response, Request request) {
		wrapped.respondForbidden(resource, response, request);
	}

	@Override
	public void respondServerError(Request request, Response response, String reason) {
		wrapped.respondServerError(request, response, reason);
	}

	@Override
	public void respondDeleteFailed(Request request, Response response, Resource resource, Status status) {
		List<HrefStatus> statii = new ArrayList<HrefStatus>();
		statii.add(new HrefStatus(request.getAbsoluteUrl(), status));
		responseMultiStatus(resource, response, request, statii);

	}

	@Override
	public void respondPropFind(List<PropFindResponse> propFindResponses, Response response, Request request, Resource r) {
		log.trace("respondPropFind");
		response.setStatus(Status.SC_MULTI_STATUS);
		response.setDateHeader(new Date());
		response.setContentTypeHeader("application/xml; charset=utf-8");
		//response.setContentTypeHeader( Response.XML );
		List<String> supportedLevels = resourceTypeHelper.getSupportedLevels(r);
		String s = Utils.toCsv(supportedLevels);
		response.setDavHeader(s);

		//response.setEntity(new ByteArrayEntity(arr));
		OutputStream outputStream = response.getOutputStream();
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			boolean sendErrorProps = !isBriefHeader(request);
			propFindXmlGenerator.generate(propFindResponses, bout, sendErrorProps);
			if( log.isTraceEnabled()) {
				log.trace("Propfind response ----");
				log.trace(bout.toString());
				log.trace("----");
			}
			byte[] arr = bout.toByteArray();
			//response.setContentLengthHeader((long) arr.length);			
			outputStream.write(arr);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		try {
			outputStream.flush();
		} catch (IOException ex) {
			log.warn("exception flushing response", ex.getMessage());
		}
	}

	@Override
	public void respondInsufficientStorage(Request request, Response response, StorageErrorReason storageErrorReason) {
		response.setStatus(Status.SC_INSUFFICIENT_STORAGE);
	}

	@Override
	public void respondLocked(Request request, Response response, Resource existingResource) {
		response.setStatus(Status.SC_LOCKED);
	}

	@Override
	public void respondPreconditionFailed(Request request, Response response, Resource resource) {
		wrapped.respondPreconditionFailed(request, response, resource);
	}

	@Override
	public BUFFERING getBuffering() {
		if (wrapped instanceof Bufferable) {
			return ((Bufferable) wrapped).getBuffering();
		} else {
			throw new RuntimeException("Wrapped class is not a known type");
		}
	}

	@Override
	public void setBuffering(BUFFERING buffering) {
		if (wrapped instanceof Bufferable) {
			((Bufferable) wrapped).setBuffering(buffering);
		} else {
			throw new RuntimeException("Wrapped class is not a known type");
		}
	}

	private boolean isBriefHeader(Request request) {
		String b = request.getHeaders().get("Brief");
		return "t".equals(b);
	}


}
