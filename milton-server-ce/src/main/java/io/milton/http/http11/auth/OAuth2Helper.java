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

import io.milton.http.OAuth2TokenResponse;
import io.milton.resource.OAuth2Resource;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Lee YOU
 */
public class OAuth2Helper {

	private static final Logger log = LoggerFactory.getLogger(OAuth2Helper.class);

	private final NonceProvider nonceProvider;

	public OAuth2Helper(NonceProvider nonceProvider) {
		this.nonceProvider = nonceProvider;
	}

	// Sept 1, Build OAuth2 End User Authorization Request URL For Getting a Access Permission
	public OAuth2TokenResponse checkOAuth2URL(OAuth2Resource oAuth2Resource) throws OAuthSystemException, MalformedURLException {
		log.trace("checkOAuth2URL start..." + oAuth2Resource);
		if (!(oAuth2Resource != null && oAuth2Resource instanceof OAuth2Resource)) {
			return null;
		}

		String oAuth2Location = oAuth2Resource.getOAuth2Location();
		String oAuth2ClientId = oAuth2Resource.getOAuth2ClientId();
		//String oAuth2ClientSecret = oAuth2Resource.getOAuth2ClientSecret();
		String oAuth2RedirectURI = oAuth2Resource.getOAuth2RedirectURI();

		OAuthClientRequest oAuthRequest = OAuthClientRequest
				.authorizationLocation(oAuth2Location)
				.setClientId(oAuth2ClientId)
				//.setResponseType("code") // 
				.setRedirectURI(oAuth2RedirectURI)
				.buildQueryMessage();

		OAuth2TokenResponse oAuth2Response = new OAuth2TokenResponse();
		URL urlTemp = new URL(oAuthRequest.getLocationUri());

		oAuth2Response.setOAuth2URL(urlTemp.toString());

		return oAuth2Response;
	}

	// Sept 2, After Got The Authorization Code(a Access Permission), then Granting the Access Token.
	public OAuth2TokenResponse obtainAuth2Token(OAuth2Resource oAuth2Resource) throws OAuthSystemException, OAuthProblemException {
		log.trace("obtainAuth2Token start..." + oAuth2Resource);
		if (!(oAuth2Resource != null && oAuth2Resource instanceof OAuth2Resource)) {
			return null;
		}

		String oAuth2Location = oAuth2Resource.getOAuth2Location();
		String oAuth2ClientId = oAuth2Resource.getOAuth2ClientId();
		String oAuth2ClientSecret = oAuth2Resource.getOAuth2ClientSecret();
		String oAuth2RedirectURI = oAuth2Resource.getOAuth2RedirectURI();
		String oAuth2PermissionResponse = oAuth2Resource.getOAuth2PermissionResponse();

		//OAuthAuthzResponse oar = OAuthAuthzResponse.oauthCodeAuthzResponse(request);
		//String code = oar.getCode();//
		OAuthClientRequest oAuthRequest = OAuthClientRequest
				.tokenLocation(oAuth2Location)
				.setGrantType(GrantType.AUTHORIZATION_CODE)
				.setRedirectURI(oAuth2RedirectURI)
				.setCode("")// TODO
				.setClientId(oAuth2ClientId)
				.setClientSecret(oAuth2ClientSecret)
				.buildBodyMessage();

		OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
		OAuth2TokenResponse oAuth2Response = oAuthClient.accessToken(oAuthRequest, OAuth2TokenResponse.class);

		return oAuth2Response;
	}
}
