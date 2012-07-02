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

package io.milton.http.http11;

import io.milton.http.Range;
import io.milton.resource.GetableResource;
import io.milton.http.*;
import io.milton.resource.Resource;
import io.milton.http.Response.Status;
import io.milton.http.entity.BufferingGetableResourceEntity;
import io.milton.http.entity.GetableResourceEntity;
import io.milton.http.exceptions.BadRequestException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.sso.ExternalIdentityProvider;
import java.io.IOException;
import java.util.Properties;

/**
 *
 */
public class DefaultHttp11ResponseHandler implements Http11ResponseHandler, Bufferable {

	private static String miltonVerson;	
	{
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
	private static final Logger log = LoggerFactory.getLogger(DefaultHttp11ResponseHandler.class);
	private final AuthenticationService authenticationService;
	private final ETagGenerator eTagGenerator;
	private CacheControlHelper cacheControlHelper = new DefaultCacheControlHelper();
	private ContentGenerator contentGenerator = new SimpleContentGenerator();
	private int maxMemorySize = 100000;
	private BUFFERING buffering;

	public DefaultHttp11ResponseHandler(AuthenticationService authenticationService, ETagGenerator eTagGenerator) {
		this.authenticationService = authenticationService;
		this.eTagGenerator = eTagGenerator;
	}

	/**
	 * Defaults to com.bradmcevoy.http.http11.DefaultCacheControlHelper
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
		response.setContentLengthHeader((long) 0);
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
			log.info("respondUnauthorised: return staus: " + Response.Status.SC_UNAUTHORIZED);
			response.setStatus(Response.Status.SC_UNAUTHORIZED);
			List<String> challenges = authenticationService.getChallenges(resource, request);
			response.setAuthenticateHeader(challenges);
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
		response.setContentRangeHeader(range.getStart(), range.getFinish(), resource.getContentLength());
		response.setDateHeader(new Date());
		String etag = eTagGenerator.generateEtag(resource);
		if (etag != null) {
			response.setEtag(etag);
		}
		String acc = request.getAcceptHeader();
		String ct = resource.getContentType(acc);
		if (ct != null) {
			response.setContentTypeHeader(ct);
		}
        response.setEntity(new GetableResourceEntity(resource, range, params, ct));
	}

	@Override
	public void respondHead(Resource resource, Response response, Request request) {
		//setRespondContentCommonHeaders(response, resource, Response.Status.SC_NO_CONTENT, request.getAuthorization());
		setRespondContentCommonHeaders(response, resource, Response.Status.SC_OK, request.getAuthorization());
		if (!(resource instanceof GetableResource)) {
			return;
		}
		GetableResource gr = (GetableResource) resource;
		Long contentLength = gr.getContentLength();
		if (contentLength != null) {
			response.setContentLengthHeader(contentLength);
		} else {
			log.trace("No content length is available for HEAD request");
		}
		String acc = request.getAcceptHeader();
		String ct = gr.getContentType(acc);
		if (ct != null) {
			ct = pickBestContentType(ct);
			if( ct != null ) {
				response.setContentTypeHeader(ct);
			}
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
			boolean doBuffering;
			if (buffering == null || buffering == BUFFERING.whenNeeded) {
				doBuffering = (contentLength == null); // if no content length then we buffer content to find content length
			} else {
				doBuffering = (buffering == BUFFERING.always); // if not null or whenNeeded then buffering is explicitly enabled or disabled
			}
			if (!doBuffering) {
				log.trace("sending content with known content length: " + contentLength);
				if (contentLength != null) {
					response.setContentLengthHeader(contentLength);
				}
                response.setEntity(new GetableResourceEntity(
                   gr, params, ct
                ));
			} else {
                response.setEntity(new BufferingGetableResourceEntity(
                   gr, params, ct, contentLength, getMaxMemorySize()
                ));
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

	public void setContentGenerator(ContentGenerator contentGenerator) {
		this.contentGenerator = contentGenerator;
	}
}
