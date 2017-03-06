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

import io.milton.http.AuthenticationHandler;
import io.milton.http.Request;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.values.Pair;
import io.milton.resource.OAuth2Provider;
import io.milton.resource.OAuth2Resource;
import io.milton.resource.OAuth2Resource.OAuth2ProfileDetails;
import io.milton.resource.Resource;
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
	public static final String REQ_ATT_LOCAL_USER = "_oauthLocalUser";
	public static final String REQ_ATT_OAUTH_DETAILS = "_oauthDetails";
	public static final String REQ_ATT_OAUTH_JSON = "_oauthJson";

	public static Object getFoundLocalUser(Request r) {
		return r.getAttributes().get(REQ_ATT_LOCAL_USER);
	}

	public static OAuth2ProfileDetails getOAuthDetails(Request r) {
		return (OAuth2ProfileDetails) r.getAttributes().get(REQ_ATT_OAUTH_DETAILS);
	}

	/**
	 * Returns the profile details as json formatted text.
	 *
	 * @param r
	 * @return
	 */
	public static String getOAuthDetailsJson(Request r) {
		return (String) r.getAttributes().get(REQ_ATT_OAUTH_JSON);
	}

	private final NonceProvider nonceProvider;
	private final OAuth2Helper oAuth2Helper;

	public OAuth2AuthenticationHandler(NonceProvider nonceProvider) {
		this.nonceProvider = nonceProvider;
		this.oAuth2Helper = new OAuth2Helper(nonceProvider);
	}

	@Override
	public Object authenticate(Resource resource, Request request) {
		if (request == null) {
			return null;
		}
		return getFoundLocalUser(request);
	}

	@Override
	public boolean supports(Resource r, Request request) {
		log.trace("supports");
		if (request == null || request.getParams() == null) {
			return false;
		}
		String oAuth2Code = request.getParams().get(OAuth.OAUTH_CODE);
		String oAuth2AccessToken = request.getParams().get(OAuth.OAUTH_ACCESS_TOKEN);
		if (StringUtils.isBlank(oAuth2Code) && StringUtils.isBlank(oAuth2AccessToken)) {
			return false;
		}

		try {
			if (r instanceof OAuth2Resource) {
				OAuth2Resource oAuth2Resource = (OAuth2Resource) r;
				log.info("This is a OAuth2Resource {} ", oAuth2Resource);
				OAuth2Resource.OAuth2ProfileDetails oAuth2TokenUser = null;

				if (StringUtils.isNotBlank(oAuth2Code)) {
					oAuth2TokenUser = parse(oAuth2Resource, request);
				} else {
					oAuth2TokenUser = new OAuth2ProfileDetails();
					oAuth2TokenUser.setAccessToken(oAuth2AccessToken);
				}

				if (oAuth2TokenUser == null) {
					log.warn("Failed to convert oauth2 response to profile");
					return false;
				}
				log.info("oauth2 login {}", oAuth2TokenUser);
				request.getAttributes().put(REQ_ATT_OAUTH_DETAILS, oAuth2TokenUser);

				Object localUser = oAuth2Resource.authenticate(oAuth2TokenUser);
				if (localUser == null) {
					log.info("No local user, cannot authenticate");
					return false;
				}

				request.getAttributes().put(REQ_ATT_LOCAL_USER, localUser); // we'll return this in authenticate
				return true;
			} else {
				log.info("Cannot authenticate resource which does not implement OAuth2Resource - {}", r.getClass());
				return false;
			}
		} catch (Exception ex) {
			log.error("OAuth2 Authentication Handler error. ", ex);
			return false;
		}

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
		return true;
	}

	public OAuth2Resource.OAuth2ProfileDetails parse(OAuth2Resource oAuth2Resource, Request request) throws BadRequestException, OAuthSystemException, OAuthProblemException {
		if (request == null) {
			return null;
		}

		log.info("This is a OAuth2Resource{} " + oAuth2Resource);

		String oAuth2Code = request.getParams().get(OAuth.OAUTH_CODE);
		String oAuth2Error = request.getParams().get("error");
		log.info("authenticate(), error{}" + oAuth2Error + " oAuth2Code{}" + oAuth2Code);

		if (StringUtils.isNotBlank(oAuth2Code) && StringUtils.isBlank(oAuth2Error)) {
			// Find the provider, by looking for the provider id we put intop the redirect uri
			String state = request.getParams().get(OAuth.OAUTH_STATE);
			Pair<String, String> statePair = OAuth2Helper.parseState(state);
			String provId = statePair.getObject1();
			String returnUrl = statePair.getObject2();
			if (StringUtils.isBlank(provId)) {
				log.warn("Could not authenticate oauth2 response because there is no provider ID parameter in the state parameter");
				return null;
			}
			OAuth2Provider prov = null;
			if (oAuth2Resource.getOAuth2Providers() != null) {
				prov = oAuth2Resource.getOAuth2Providers().get(provId);
			}
			if (prov == null) {
				log.warn("Could not authenticate oauth2 response because couldnt find provider: " + provId);
				return null;
			}
			// Step :Obtain the access token
			OAuthAccessTokenResponse oAuth2Response = this.oAuth2Helper.obtainAuth2Token(prov, oAuth2Code);
			log.info("This is a OAuth2TokenResponse{} " + oAuth2Response);

			if (oAuth2Response != null) {
				// Step : Get the profile.
				OAuthResourceResponse resourceResponse = this.oAuth2Helper.getOAuth2Profile(oAuth2Response, prov);
				log.info("This is a OAuthResourceResponse{} " + resourceResponse);

				if (resourceResponse != null) {
					// Step : Get the user info.
					OAuth2Resource.OAuth2ProfileDetails oAuth2TokenUser = this.oAuth2Helper.getOAuth2UserInfo(request, resourceResponse, oAuth2Response, prov, oAuth2Code, returnUrl);

					return oAuth2TokenUser;

				}
			}
		}

		return null;
	}
}
