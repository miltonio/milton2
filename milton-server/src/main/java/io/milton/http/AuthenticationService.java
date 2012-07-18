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

import io.milton.resource.GetableResource;
import io.milton.resource.Resource;
import io.milton.common.StringUtils;
import io.milton.sso.ExternalIdentityProvider;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class AuthenticationService {

	private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
	private List<AuthenticationHandler> authenticationHandlers;
	private List<ExternalIdentityProvider> externalIdentityProviders;
	private boolean disableExternal;
	private String[] browserIds = {"msie", "firefox", "chrome", "safari", "opera"};

	/**
	 * Creates a AuthenticationService using the given handlers. Use this if you
	 * don't want the default of a BasicAuthHandler and a
	 * DigestAuthenticationHandler
	 *
	 * @param authenticationHandlers
	 */
	public AuthenticationService(List<AuthenticationHandler> authenticationHandlers) {
		this.authenticationHandlers = authenticationHandlers;
	}

	/**
	 * Looks for an AuthenticationHandler which supports the given resource and
	 * authorization header, and then returns the result of that handler's
	 * authenticate method.
	 *
	 * Returns null if no handlers support the request
	 *
	 * @param resource
	 * @param request
	 * @return - null if no authentication was attempted. Otherwise, an
	 * AuthStatus object containing the Auth object and a boolean indicating
	 * whether the login succeeded
	 */
	public AuthStatus authenticate(Resource resource, Request request) {
		log.trace("authenticate");
		Auth auth = request.getAuthorization();
		boolean preAuthenticated = (auth != null && auth.getTag() != null);
		if (preAuthenticated) {
			log.trace("request is pre-authenticated");
			return new AuthStatus(auth, false);
		}
		for (AuthenticationHandler h : authenticationHandlers) {
			if (h.supports(resource, request)) {
				Object loginToken = h.authenticate(resource, request);
				if (loginToken == null) {
					log.warn("authentication failed by AuthenticationHandler:" + h.getClass());
					return new AuthStatus(auth, true);
				} else {
					if (log.isTraceEnabled()) {
						log.trace("authentication passed by: " + h.getClass());
					}
					if (auth == null) { // some authentication handlers do not require an Auth object
						auth = new Auth(Auth.Scheme.FORM, null, loginToken);
						request.setAuthorization(auth);
					}
					auth.setTag(loginToken);
				}
				return new AuthStatus(auth, false);
			} else {
				if (log.isTraceEnabled()) {
					log.trace("handler does not support this resource and request. handler: " + h.getClass() + " resource: " + resource.getClass());
				}
			}
		}
		log.trace("authentication did not locate a user, because no handler accepted the request");
		return null;
	}

	/**
	 * Generates a list of http authentication challenges, one for each
	 * supported authentication method, to be sent to the client.
	 *
	 * @param resource - the resoruce being requested
	 * @param request - the current request
	 * @return - a list of http challenges
	 */
	public List<String> getChallenges(Resource resource, Request request) {
		List<String> challenges = new ArrayList<String>();
		for (AuthenticationHandler h : authenticationHandlers) {
			if (h.isCompatible(resource, request)) {
				log.debug("challenge for auth: " + h.getClass());
				String ch = h.getChallenge(resource, request);
				if (ch != null) {
					challenges.add(ch);
				}
			} else {
				log.debug("not challenging for auth: " + h.getClass() + " for resource type: " + (resource == null ? "" : resource.getClass()));
			}
		}
		return challenges;
	}

	public List<AuthenticationHandler> getAuthenticationHandlers() {
		return authenticationHandlers;
	}

	public List<ExternalIdentityProvider> getExternalIdentityProviders() {
		return externalIdentityProviders;
	}

	public void setExternalIdentityProviders(List<ExternalIdentityProvider> externalIdentityProviders) {
		this.externalIdentityProviders = externalIdentityProviders;
	}

	public boolean isDisableExternal() {
		return disableExternal;
	}

	public void setDisableExternal(boolean disableExternal) {
		this.disableExternal = disableExternal;
	}

	/**
	 * Determine if we can use external identify providers to authenticate this
	 * request
	 *
	 * @param resource
	 * @param request
	 * @return
	 */
	public boolean canUseExternalAuth(Resource resource, Request request) {
		if (isDisableExternal()) {
			log.trace("auth svc has disabled external auth");
			return false;
		}
		if (getExternalIdentityProviders() == null || getExternalIdentityProviders().isEmpty()) {
			log.trace("auth service has no external auth providers");
			return false;
		}

		// external authentication requires redirecting the user's browser to another
		// site and displaying a login form. 

		// This can only be done if the resource
		// being requested is a webpage. This means that it is Getable and that it
		// has a content type of html
		if (resource instanceof GetableResource) {
			GetableResource gr = (GetableResource) resource;
			String ct = gr.getContentType("text/html");
			if (ct == null || !ct.contains("html")) {
				log.trace("is not of content type html");
				return false;
			}
		} else {
			log.trace("is not getable");
			return false; // not getable, so definitely not suitable for external auth
		}

		// This can only be done for user agents which support displaying forms and redirection
		// Ie typical web browsers are ok, webdav clients are generally not ok
		String ua = request.getUserAgentHeader();
		if (StringUtils.contains(ua.toLowerCase(), browserIds)) {
			log.trace("is a known web browser, so can offer external auth");
			return true;
		} else {
			log.trace("not a known web browser, so cannot offer external auth");
			return false;
		}

	}

	public static class AuthStatus {

		public final Auth auth;
		public final boolean loginFailed;

		public AuthStatus(Auth auth, boolean loginFailed) {
			this.auth = auth;
			this.loginFailed = loginFailed;
		}

		@Override
		public String toString() {
			if (auth == null) {
				return "AuthStatus: no creds";
			}
			if (loginFailed) {
				return "AuthStatus: login failed: " + auth.getUser();
			} else {
				return "AuthStatus: logged in: " + auth.getUser();
			}
		}
	}
}
