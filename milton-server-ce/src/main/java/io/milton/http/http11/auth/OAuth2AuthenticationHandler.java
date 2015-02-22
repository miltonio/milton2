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
import io.milton.http.OAuth2TokenResponse;
import io.milton.http.Request;
import io.milton.resource.OAuth2Resource;
import io.milton.resource.Resource;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
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

		log.info("OAuth2AuthenticationHandler start..." + resource);

		try {
			if (resource instanceof OAuth2Resource) {
				OAuth2Resource oAuth2Resource = (OAuth2Resource) resource;
				log.info("This is a OAuth2Resource, and processing the oAuth step: " + oAuth2Resource.getOAuth2Step());

				String oAuth2Code = request.getParams().get(OAuth.OAUTH_CODE);
				log.info("authenticate(), oAuth2Code{}" + oAuth2Code);

				// Sept 1, Build OAuth2 End User Authorization Request URL For Getting a Access Permission
				if (oAuth2Resource.getOAuth2Step() == OAuth2Resource.GRANT_PERMISSION) {

					return this.oAuth2Helper.checkOAuth2URL(oAuth2Resource);
				}

				// Sept 2, After Got The Authorization Code(a Access Permission), then Granting the Access Token.
				if (oAuth2Resource.getOAuth2Step() == OAuth2Resource.OBTAIN_TOKEN) {
					return this.oAuth2Helper.obtainAuth2Token(oAuth2Resource);
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
