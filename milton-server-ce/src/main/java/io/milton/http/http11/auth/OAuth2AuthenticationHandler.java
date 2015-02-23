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
import io.milton.http.OAuth2TokenResponse;
import io.milton.http.OAuth2TokenUser;
import io.milton.http.Request;
import io.milton.resource.OAuth2Resource;
import io.milton.resource.Resource;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Lee YOU
 */
public class OAuth2AuthenticationHandler implements AuthenticationHandler {

	private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationHandler.class);
	private final NonceProvider nonceProvider;
	private final OAuth2Helper oAuth2Helper;

	public OAuth2AuthenticationHandler(NonceProvider nonceProvider) {
		this.nonceProvider = nonceProvider;
		this.oAuth2Helper = new OAuth2Helper(nonceProvider);
	}

	@Override
	public Object authenticate(Resource resource, Request request) {

		log.info("\n\n............OAuth2AuthenticationHandler start........." + resource);

		if (request == null) {
			return null;
		}

		Auth auth = request.getAuthorization();
		String realm = auth.getRealm();
		Object o = auth.getTag();
		log.trace("auth.getTag{}" + o + " realm{}" + realm + " user{}" + auth.getUser());

		if (o instanceof OAuth2TokenUser) {
			log.info("signed {}:" + o);
			// return null;// TODO for testing
		}

		try {
			if (resource instanceof OAuth2Resource) {
				OAuth2Resource oAuth2Resource = (OAuth2Resource) resource;
				log.info("This is a OAuth2Resource{} " + oAuth2Resource);

				String oAuth2Code = request.getParams().get(OAuth.OAUTH_CODE);
				String oAuth2Error = request.getParams().get("error");
				log.info("authenticate(), error{}" + oAuth2Error + " oAuth2Code{}" + oAuth2Code);

				if (StringUtils.isNotBlank(oAuth2Code) && StringUtils.isBlank(oAuth2Error)) {
					// Step :Obtain the access token
					OAuth2TokenResponse oAuth2Response
							= this.oAuth2Helper.obtainAuth2Token(oAuth2Resource, oAuth2Code);
					log.info("This is a OAuth2TokenResponse{} " + oAuth2Response);

					if (oAuth2Response != null) {
						// Step : Get the profile.
						OAuthResourceResponse resourceResponse
								= this.oAuth2Helper.getOAuth2Profile(oAuth2Response, oAuth2Resource);
						log.info("This is a OAuthResourceResponse{} " + resourceResponse);

						if (resourceResponse != null) {
							// Step : Get the user info.
							return this.oAuth2Helper.getOAuth2UserInfo(resourceResponse, oAuth2Response, oAuth2Code);
						}
					}
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException("OAuth2 Authentication Handler error. ", ex);
		}
		return null;
	}

	@Override
	public boolean supports(Resource r, Request request) {
		log.trace("supports");
		return (r != null) && (r instanceof OAuth2Resource);
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
}
