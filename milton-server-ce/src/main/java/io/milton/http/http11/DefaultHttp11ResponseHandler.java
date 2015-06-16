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
package io.milton.http.http11;

import io.milton.http.Auth;
import io.milton.http.AuthenticationService;
import io.milton.http.ExternalIdentityProvider;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.Response;
import io.milton.http.Response.Status;
import io.milton.http.entity.BufferingGetableResourceEntity;
import io.milton.http.entity.GetableResourceEntity;
import io.milton.http.entity.PartialEntity;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.resource.BufferingControlResource;
import io.milton.resource.GetableResource;
import io.milton.resource.Resource;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DefaultHttp11ResponseHandler implements Http11ResponseHandler, Bufferable {

	private static final Logger log = LoggerFactory.getLogger(DefaultHttp11ResponseHandler.class);
	private static final String miltonVerson;

	static {
		Properties props = new Properties();
		try {
			props.load(DefaultHttp11ResponseHandler.class.getResourceAsStream("/milton.properties"));
		} catch (IOException ex) {
			log.warn("Failed lot load milton properties file", ex);
		}
		miltonVerson = props.getProperty("milton.version");
	}

	public enum BUFFERING {

		always,
		never,
		whenNeeded
	}

	private final AuthenticationService authenticationService;
	private final ETagGenerator eTagGenerator;
	private final ContentGenerator contentGenerator;
	private CacheControlHelper cacheControlHelper = new DefaultCacheControlHelper();
	private int maxMemorySize = 100000;
	private BUFFERING buffering;
	private String multipartBoundary = UUID.randomUUID().toString();

	public DefaultHttp11ResponseHandler(AuthenticationService authenticationService, ETagGenerator eTagGenerator, ContentGenerator contentGenerator) {
		this.authenticationService = authenticationService;
		this.eTagGenerator = eTagGenerator;
		this.contentGenerator = contentGenerator;
	}

	/**
	 * Defaults to com.bradmcevoy.http.http11.DefaultCacheControlHelper
	 *
	 * @return
	 */
	public CacheControlHelper getCacheControlHelper() {
		return cacheControlHelper;
	}

	public void setCacheControlHelper(CacheControlHelper cacheControlHelper) {
		this.cacheControlHelper = cacheControlHelper;
	}

	@Override
	public String generateEtag(Resource r) {
		return eTagGenerator.generateEtag(r);
	}

	@Override
	public void respondWithOptions(Resource resource, Response response, Request request, List<String> methodsAllowed) {
		setRespondCommonHeaders(response, resource, Status.SC_OK, request.getAuthorization());
		response.setAllowHeader(methodsAllowed);
		response.setContentLengthHeader((long) 0); // Note that setting content length must be done last for tomcat5	
	}

	@Override
	public void respondNotFound(Response response, Request request) {
		response.setStatus(Response.Status.SC_NOT_FOUND);
		response.setContentTypeHeader("text/html");
		contentGenerator.generate(null, request, response, Status.SC_NOT_FOUND);
	}

	@Override
	public void respondUnauthorised(Resource resource, Response response, Request request) {
		if (authenticationService.canUseExternalAuth(resource, request)) {
			log.info("respondUnauthorised: use external authentication");
			initiateExternalAuth(resource, request, response);
		} else {
			Auth auth = request.getAuthorization();
			if (auth == null || auth.getTag() == null) {
				log.info("respondUnauthorised: no authenticated user, so return status: " + Response.Status.SC_UNAUTHORIZED);
				response.setStatus(Response.Status.SC_UNAUTHORIZED);
				List<String> challenges = authenticationService.getChallenges(resource, request);
				response.setAuthenticateHeader(challenges);

			} else {
				log.info("respondUnauthorised: request has an authenticated user, so return status: " + Response.Status.SC_FORBIDDEN);
				response.setStatus(Response.Status.SC_FORBIDDEN);

			}
		}
	}

	@Override
	public void respondMethodNotImplemented(Resource resource, Response response, Request request) {
		response.setStatus(Response.Status.SC_NOT_IMPLEMENTED);
		contentGenerator.generate(resource, request, response, Status.SC_NOT_IMPLEMENTED);
	}

	@Override
	public void respondMethodNotAllowed(Resource res, Response response, Request request) {
		log.debug("method not allowed. handler: " + this.getClass().getName() + " resource: " + res.getClass().getName());
		response.setStatus(Response.Status.SC_METHOD_NOT_ALLOWED);
		contentGenerator.generate(res, request, response, Status.SC_METHOD_NOT_ALLOWED);
	}

	/**
	 *
	 * @param resource
	 * @param response
	 * @param request
	 * @param message - optional message to output in the body content
	 */
	@Override
	public void respondConflict(Resource resource, Response response, Request request, String message) {
		log.debug("respondConflict");
		response.setStatus(Response.Status.SC_CONFLICT);
		contentGenerator.generate(resource, request, response, Status.SC_CONFLICT);
	}

	@Override
	public void respondServerError(Request request, Response response, String reason) {
		response.setStatus(Status.SC_INTERNAL_SERVER_ERROR);
		contentGenerator.generate(null, request, response, Status.SC_INTERNAL_SERVER_ERROR);
	}

	@Override
	public void respondRedirect(Response response, Request request, String redirectUrl) {
		if (redirectUrl == null) {
			throw new NullPointerException("redirectUrl cannot be null");
		}
		log.trace("respondRedirect");
		// delegate to the response, because this can be server dependent
		response.sendRedirect(redirectUrl);
//        response.setStatus(Response.Status.SC_MOVED_TEMPORARILY);
//        response.setLocationHeader(redirectUrl);
	}

	@Override
	public void respondExpectationFailed(Response response, Request request) {
		response.setStatus(Response.Status.SC_EXPECTATION_FAILED);
	}

	@Override
	public void respondCreated(Resource resource, Response response, Request request) {
//        log.debug( "respondCreated" );
		setRespondCommonHeaders(response, resource, Status.SC_CREATED, request.getAuthorization());
	}

	@Override
	public void respondNoContent(Resource resource, Response response, Request request) {
//        log.debug( "respondNoContent" );
		//response.setStatus(Response.Status.SC_OK);
		// see comments in http://www.ettrema.com:8080/browse/MIL-87
		setRespondCommonHeaders(response, resource, Status.SC_NO_CONTENT, request.getAuthorization());
	}

	@Override
	public void respondPartialContent(GetableResource resource, Response response, Request request, Map<String, String> params, Range range) throws NotAuthorizedException, BadRequestException, NotFoundException {
		log.debug("respondPartialContent: " + range.getStart() + " - " + range.getFinish());
		response.setStatus(Response.Status.SC_PARTIAL_CONTENT);
		long st = range.getStart() == null ? 0 : range.getStart();
		long fn;
		Long cl = resource.getContentLength();
		if (range.getFinish() == null) {
			if (cl != null) {
				fn = cl - 1; // position is one less then length
			} else {
				log.warn("Couldnt calculate range end position because the resource is not reporting a content length, and no end position was requested by the client: " + resource.getName() + " - " + resource.getClass());
				fn = -1;
			}
		} else {
			if (cl != null && cl < range.getFinish()) {
				fn = cl - 1;
			} else if (cl == null) {
				log.warn("Couldnt calculate range end position because the resource is not reporting a content length, and no end position was requested by the client: " + resource.getName() + " - " + resource.getClass());
				fn = -1;
			} else {
				fn = range.getFinish();
			}
		}
		response.setContentRangeHeader(st, fn, cl);
		long contentLength = fn - st + 1;
		response.setDateHeader(new Date());
		String etag = eTagGenerator.generateEtag(resource);
		if (etag != null) {
			response.setEtag(etag);
		}
		//String acc = request.getAcceptHeader();
//		String ct = resource.getContentType(acc);
//		if (ct != null) {
//			response.setContentTypeHeader(ct);
//		}
		response.setContentLengthHeader(contentLength);
		response.setEntity(new GetableResourceEntity(resource, range, params, null));
	}

	/**
	 * Send a partial content response with multiple ranges
	 *
	 * @param resource
	 * @param response
	 * @param request
	 * @param params
	 * @param ranges
	 * @throws NotAuthorizedException
	 * @throws BadRequestException
	 * @throws NotFoundException
	 */
	@Override
	public void respondPartialContent(GetableResource resource, Response response, Request request, Map<String, String> params, List<Range> ranges) throws NotAuthorizedException, BadRequestException, NotFoundException {
		log.debug("respondPartialContent - multiple ranges");
		response.setStatus(Response.Status.SC_PARTIAL_CONTENT);
		response.setAcceptRanges("bytes");
		response.setDateHeader(new Date());
		String etag = eTagGenerator.generateEtag(resource);
		if (etag != null) {
			response.setEtag(etag);
		}
		response.setContentTypeHeader("multipart/byteranges; boundary=" + multipartBoundary);
		String acc = request.getAcceptHeader();
		String ct = resource.getContentType(acc);

		response.setEntity(
				new PartialEntity(resource, ranges, params, ct, multipartBoundary)
		);

	}

	@Override
	public void respondHead(Resource resource, Response response, Request request) {
		//setRespondContentCommonHeaders(response, resource, Response.Status.SC_NO_CONTENT, request.getAuthorization());
		setRespondContentCommonHeaders(response, resource, Response.Status.SC_OK, request.getAuthorization());
		if (!(resource instanceof GetableResource)) {
			return;
		}
		GetableResource gr = (GetableResource) resource;
		String acc = request.getAcceptHeader();
		String ct = gr.getContentType(acc);
		if (ct != null) {
			ct = pickBestContentType(ct);
			if (ct != null) {
				response.setContentTypeHeader(ct);
			}
		}
		Long contentLength = gr.getContentLength();
		if (contentLength != null) {
			response.setContentLengthHeader(contentLength);
		} else {
			log.trace("No content length is available for HEAD request");
		}
	}

	@Override
	public void respondContent(Resource resource, Response response, Request request, Map<String, String> params) throws NotAuthorizedException, BadRequestException, NotFoundException {
		log.debug("respondContent: " + resource.getClass());
		Auth auth = request.getAuthorization();
		setRespondContentCommonHeaders(response, resource, auth);
		if (resource instanceof GetableResource) {
			GetableResource gr = (GetableResource) resource;
			String acc = request.getAcceptHeader();
			String ct = gr.getContentType(acc);
			if (ct != null) {
				ct = pickBestContentType(ct);
				response.setContentTypeHeader(ct);
			}
			cacheControlHelper.setCacheControl(gr, response, request.getAuthorization());

			Long contentLength = gr.getContentLength();
			Boolean doBuffering = null;
			if (resource instanceof BufferingControlResource) {
				BufferingControlResource bcr = (BufferingControlResource) resource;
				doBuffering = bcr.isBufferingRequired();
			}
			if (doBuffering == null) {
				if (buffering == null || buffering == BUFFERING.whenNeeded) {
					doBuffering = (contentLength == null); // if no content length then we buffer content to find content length
				} else {
					doBuffering = (buffering == BUFFERING.always); // if not null or whenNeeded then buffering is explicitly enabled or disabled
				}
			}
			if (!doBuffering) {
				log.trace("sending content with known content length: " + contentLength);
				if (contentLength != null) {
					response.setContentLengthHeader(contentLength);
				}
				response.setEntity(new GetableResourceEntity(gr, params, ct));
			} else {
				BufferingGetableResourceEntity e = new BufferingGetableResourceEntity(gr, params, ct, contentLength, getMaxMemorySize());
				response.setEntity(e);
			}
		}
	}

	@Override
	public void respondNotModified(GetableResource resource, Response response, Request request) {
		log.trace("respondNotModified");
		response.setStatus(Response.Status.SC_NOT_MODIFIED);
		response.setDateHeader(new Date());
		String etag = eTagGenerator.generateEtag(resource);
		if (etag != null) {
			response.setEtag(etag);
		}

		// Note that we use a simpler modified date handling here then when
		// responding with content, because in a not-modified situation the
		// modified date MUST be that of the actual resource
		Date modDate = resource.getModifiedDate();
		response.setLastModifiedHeader(modDate);

		cacheControlHelper.setCacheControl(resource, response, request.getAuthorization());
	}

	protected void setRespondContentCommonHeaders(Response response, Resource resource, Auth auth) {
		setRespondContentCommonHeaders(response, resource, Response.Status.SC_OK, auth);
	}

	protected void setRespondContentCommonHeaders(Response response, Resource resource, Response.Status status, Auth auth) {
		setRespondCommonHeaders(response, resource, status, auth);
		setModifiedDate(response, resource, auth);
	}

	protected void setRespondCommonHeaders(Response response, Resource resource, Response.Status status, Auth auth) {
		response.setStatus(status);
		response.setNonStandardHeader("Server", "milton.io-" + miltonVerson);
		response.setDateHeader(new Date());
		response.setNonStandardHeader("Accept-Ranges", "bytes");
		String etag = eTagGenerator.generateEtag(resource);
		if (etag != null) {
			response.setEtag(etag);
		}
	}

	/**
	 * The modified date response header is used by the client for content
	 * caching. It seems obvious that if we have a modified date on the resource
	 * we should set it. BUT, because of the interaction with max-age we should
	 * always set it to the current date if we have max-age The problem, is that
	 * if we find that a condition GET has an expired mod-date (based on maxAge)
	 * then we want to respond with content (even if our mod-date hasnt changed.
	 * But if we use the actual mod-date in that case, then the browser will
	 * continue to use the old mod-date, so will forever more respond with
	 * content. So we send a mod-date of now to ensure that future requests will
	 * be given a 304 not modified.*
	 *
	 * @param response
	 * @param resource
	 * @param auth
	 */
	public static void setModifiedDate(Response response, Resource resource, Auth auth) {
		Date modDate = resource.getModifiedDate();
		if (modDate != null) {
			// HACH - see if this helps IE
			response.setLastModifiedHeader(modDate);
//            if (resource instanceof GetableResource) {
//                GetableResource gr = (GetableResource) resource;
//                Long maxAge = gr.getMaxAgeSeconds(auth);
//                if (maxAge != null && maxAge > 0) {
//                    log.trace("setModifiedDate: has a modified date and a positive maxAge, so adjust modDate");
//                    long tm = System.currentTimeMillis() - 60000; // modified 1 minute ago
//                    modDate = new Date(tm); // have max-age, so use current date
//                }
//            }
//            response.setLastModifiedHeader(modDate);
		}
	}

	@Override
	public void respondBadRequest(Resource resource, Response response, Request request) {
		response.setStatus(Response.Status.SC_BAD_REQUEST);
	}

	@Override
	public void respondForbidden(Resource resource, Response response, Request request) {
		response.setStatus(Response.Status.SC_FORBIDDEN);
	}

	@Override
	public void respondDeleteFailed(Request request, Response response, Resource resource, Status status) {
		response.setStatus(status);
	}

	@Override
	public void respondPreconditionFailed(Request request, Response response, Resource resource) {
		response.setStatus(Status.SC_PRECONDITION_FAILED);
	}

	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	/**
	 * Maximum size of data to hold in memory per request when buffering output
	 * data.
	 *
	 * @return
	 */
	public int getMaxMemorySize() {
		return maxMemorySize;
	}

	public void setMaxMemorySize(int maxMemorySize) {
		this.maxMemorySize = maxMemorySize;
	}

	@Override
	public BUFFERING getBuffering() {
		return buffering;
	}

	@Override
	public void setBuffering(BUFFERING buffering) {
		this.buffering = buffering;
	}

	/**
	 * Sometimes we'll get a content type list, such as image/jpeg,image/pjpeg
	 *
	 * In this case we should pick the first in the list
	 *
	 * @param ct
	 * @return
	 */
	private String pickBestContentType(String ct) {
		if (ct == null) {
			return null;
		} else if (ct.contains(",")) {
			return ct.split(",")[0];
		} else {
			return ct;
		}
	}

	public void initiateExternalAuth(Resource resource, Request request, Response response) {
		ExternalIdentityProvider eip = getSelectedIP(request);
		if (eip == null) {
			// means that the user needs to select an identity provider, so generate appropriate page
		} else {
			eip.initiateExternalAuth(resource, request, response);
		}
	}

	private ExternalIdentityProvider getSelectedIP(Request request) {
		List<ExternalIdentityProvider> list = authenticationService.getExternalIdentityProviders();
		if (list.size() == 1) {
			return list.get(0);
		} else {
			String ipName = request.getParams().get("_ip");
			if (ipName != null && ipName.length() > 0) {
				for (ExternalIdentityProvider eip : list) {
					if (ipName.equals(eip.getName())) {
						return eip;
					}

				}
			}
			return null;
		}
	}

	public ContentGenerator getContentGenerator() {
		return contentGenerator;
	}

	public String getMultipartBoundary() {
		return multipartBoundary;
	}

	public void setMultipartBoundary(String multipartBoundary) {
		this.multipartBoundary = multipartBoundary;
	}
}
