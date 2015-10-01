/*
 * Copyright 2015 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.milton.http.http11.auth;

import io.milton.http.Auth;
import io.milton.http.AuthenticationHandler;
import io.milton.http.Request;
import io.milton.http.exceptions.BadRequestException;
import io.milton.principal.DiscretePrincipal;
import io.milton.resource.OAuth2Provider;
import io.milton.resource.OAuth2Resource;
import io.milton.resource.Resource;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Lee YOU
 */
public class OAuth2AuthenticationHandler implements AuthenticationHandler {

	private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationHandler.class);

	private static final String HANDLER_ATT_NAME = "_OAuth_delegated";

	private final NonceProvider nonceProvider;
	private final OAuth2Helper oAuth2Helper;
	private final List<AuthenticationHandler> handlers;

	public OAuth2AuthenticationHandler(NonceProvider nonceProvider, List<AuthenticationHandler> oauthDelegates) {
		this.nonceProvider = nonceProvider;
		this.oAuth2Helper = new OAuth2Helper(nonceProvider);
		this.handlers = oauthDelegates;
	}
	

	@Override
	public boolean supports(Resource r, Request request) {
		log.trace("supports");
		List<AuthenticationHandler> supportingHandlers = new ArrayList<AuthenticationHandler>();
		for (AuthenticationHandler hnd : handlers) {
			if (hnd.supports(r, request)) {
				log.info("Found child handler who supports this request {}", hnd);
				supportingHandlers.add(hnd);
			}
		}
		if (!supportingHandlers.isEmpty()) {
			request.getAttributes().put(HANDLER_ATT_NAME, supportingHandlers);
			return true;
		}

		if (request != null && request.getParams() != null) {
			String oAuth2Code = request.getParams().get(OAuth.OAUTH_CODE);
			if (r instanceof OAuth2Resource && StringUtils.isNotBlank(oAuth2Code)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object authenticate(Resource resource, Request request) {
		if (request == null) {
			return null;
		}

		// First attempt to authenticate with the wrapped handler. If that returns
		// an authenticated user, and oauth creds are present we will connect them
		// If there is no otherwise authenticated user, then attempt to authenticate with
		// oauth creds
		List<AuthenticationHandler> supportingHandlers = (List<AuthenticationHandler>) request.getAttributes().get(HANDLER_ATT_NAME);
		Object localUser = null;
		if (supportingHandlers != null && !supportingHandlers.isEmpty()) {
			DiscretePrincipal lastUser = null;
			for (AuthenticationHandler delegateHandler : supportingHandlers) {
				if (log.isTraceEnabled()) {
					log.trace("authenticate: use delegateHandler: " + delegateHandler);
				}
				Object tag = delegateHandler.authenticate(resource, request);
				if (tag != null) {
					localUser = tag;
					break;
				}
			}
		}

		if (resource instanceof OAuth2Resource) {
			OAuth2Resource oAuth2Resource = (OAuth2Resource) resource;
			log.info("authenticate: is OAuth2Resource {} ", oAuth2Resource.getClass());

			String oAuth2Code = request.getParams().get(OAuth.OAUTH_CODE);
			String oAuth2Error = request.getParams().get("error");
			log.info("authenticate(), error{}" + oAuth2Error + " oAuth2Code{}" + oAuth2Code);

			if (StringUtils.isNotBlank(oAuth2Code) && StringUtils.isBlank(oAuth2Error)) {
				// Find the provider, by looking for the provider id we put intop the redirect uri
				String provId = request.getParams().get(OAuth.OAUTH_STATE);
				if (StringUtils.isBlank(provId)) {
					log.warn("Could not authenticate oauth2 response because there is no provider ID parameter in the state parameter");
					return localUser;
				}
				OAuth2Provider prov = oAuth2Resource.getOAuth2Providers().get(provId);
				if (prov == null) {
					log.warn("Could not authenticate oauth2 response because couldnt find provider: " + provId);
					return localUser;
				}
				// Step :Obtain the access token
				try {
					OAuthAccessTokenResponse oAuth2Response = this.oAuth2Helper.obtainAuth2Token(prov, oAuth2Code);
					log.info("This is a OAuth2TokenResponse{} " + oAuth2Response);

					if (oAuth2Response != null) {
						// Step : Get the profile.
						OAuthResourceResponse resourceResponse = this.oAuth2Helper.getOAuth2Profile(oAuth2Response, prov);
						log.info("This is a OAuthResourceResponse{} " + resourceResponse);

						if (resourceResponse != null) {
							// Step : Get the user info.
							OAuth2Resource.OAuth2ProfileDetails oAuth2TokenUser = this.oAuth2Helper.getOAuth2UserInfo(resourceResponse, oAuth2Response, prov, oAuth2Code);
							if (oAuth2TokenUser == null) {
								log.warn("Failed to convert oauth2 response to profile");
							} else {
								if (localUser == null) {
									// No local user, so attempt to login with federated creds
									log.info("oauth2 login {}", oAuth2TokenUser);
									Object federatedUser = oAuth2Resource.authenticate(oAuth2TokenUser);
									if( federatedUser == null ) {
										log.info("Could not find user from federated credentials, authentication failed");
										return null;
									} else {
										log.info("authenticate: authenticated using federated credentials {}", federatedUser.getClass());
										return federatedUser;
									}
								} else {
									log.info("authenticate: we have a local user and federarated credentials, so connect accounts");
									oAuth2Resource.connect(oAuth2TokenUser, localUser);
									// We have a local user and federated credentials, so connect accounts
								}
							}

						}
					}
				} catch (OAuthSystemException e) {
					log.warn("OAuthSystemException in oauth processing", e);
					return localUser;
				} catch (OAuthProblemException e) {
					log.warn("OAuthProblemException in oauth processing", e);
					return localUser;
				} catch (BadRequestException e) {
					log.warn("BadRequestException in oauth processing", e);
					return localUser;
				}
			}
		}
		return localUser;// if there is one, thats the authenticated user
	}

	@Override
	public void appendChallenges(Resource resource, Request request, List<String> challenges) {
	}

	@Override
	public boolean isCompatible(Resource resource, Request request) {
		return (request != null) && (request instanceof Request);
	}

	@Override
	public boolean credentialsPresent(Request request) {
		for (AuthenticationHandler h : handlers) {
			if (h.credentialsPresent(request)) {
				return true;
			}
		}
		return false;
	}
}
