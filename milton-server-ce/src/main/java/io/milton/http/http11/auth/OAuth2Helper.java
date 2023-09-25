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

import io.milton.common.Utils;
import io.milton.http.OAuth2TokenResponse;
import io.milton.http.Request;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.values.Pair;
import io.milton.resource.OAuth2Provider;
import io.milton.resource.OAuth2Resource.OAuth2ProfileDetails;
import org.apache.commons.lang.StringUtils;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.Map;

/**
 * @author Lee YOU
 */
public class OAuth2Helper {

    private static final Logger log = LoggerFactory.getLogger(OAuth2Helper.class);

    public static URL getOAuth2URL(OAuth2Provider provider, String returnUrl) {
        log.trace("getOAuth2URL {}", provider);

        String oAuth2Location = provider.getAuthLocation();
        String oAuth2ClientId = provider.getClientId();
        String scopes = Utils.toCsv(provider.getPermissionScopes(), false);
        try {
            String state = toState(provider.getProviderId(), returnUrl);
            OAuthClientRequest oAuthRequest = OAuthClientRequest
                    .authorizationLocation(oAuth2Location)
                    .setClientId(oAuth2ClientId)
                    .setResponseType("code")
                    .setScope(scopes)
                    .setState(state)
                    .setRedirectURI(provider.getRedirectURI())
                    .buildQueryMessage();

            return new URL(oAuthRequest.getLocationUri());
        } catch (OAuthSystemException | MalformedURLException oAuthSystemException) {
            throw new RuntimeException(oAuthSystemException);
        }

    }

    public static String toState(String providerId, String returnUrl) {
        StringBuilder sb = new StringBuilder(providerId);
        if (returnUrl != null) {
            sb.append("||");
            sb.append(returnUrl);
        }
        byte[] arr = Base64.getEncoder().encode(sb.toString().getBytes());
        return new String(arr);
    }

    public static Pair<String, String> parseState(String encoded) {
        String decoded = new String(Base64.getDecoder().decode(encoded));
        int i = decoded.indexOf("||");
        String p;
        String r;
        if (i > 0) {
            p = decoded.substring(0, i);
            r = decoded.substring(i + 2);
        } else {
            p = decoded;
            r = null;
        }
        return new Pair<>(p, r);
    }

    private final NonceProvider nonceProvider;

    public OAuth2Helper(NonceProvider nonceProvider) {
        this.nonceProvider = nonceProvider;
    }

    // Sept 2, After Got The Authorization Code(a Access Permission), then Granting the Access Token.
    public OAuthAccessTokenResponse obtainAuth2Token(OAuth2Provider provider, String accessCode) throws OAuthSystemException, OAuthProblemException {
        log.trace("obtainAuth2Token code={}, provider={}", accessCode, provider);

        String oAuth2ClientId = provider.getClientId();
        String oAuth2TokenLocation = provider.getTokenLocation();
        String oAuth2ClientSecret = provider.getClientSecret();
        String oAuth2RedirectURI = provider.getRedirectURI();

        OAuthClientRequest oAuthRequest = OAuthClientRequest
                .tokenLocation(oAuth2TokenLocation)
                .setGrantType(GrantType.AUTHORIZATION_CODE)
                .setRedirectURI(oAuth2RedirectURI)
                .setCode(accessCode)
                .setClientId(oAuth2ClientId)
                .setClientSecret(oAuth2ClientSecret)
                .buildBodyMessage();

        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

        // This works for facebook
        OAuthAccessTokenResponse oAuth2Response2 = oAuthClient.accessToken(oAuthRequest, OAuth2TokenResponse.class);
        //return oAuth2Response;

        // This might work for google
        OAuthJSONAccessTokenResponse o;
        //OAuthAccessTokenResponse oAuth2Response2 = oAuthClient.accessToken(oAuthRequest, OAuth2TokenResponse.class);
        return oAuth2Response2;

    }

    // Sept 3, GET the profile of the user.
    public OAuthResourceResponse getOAuth2Profile(OAuthAccessTokenResponse oAuth2Response, OAuth2Provider provider)
            throws OAuthSystemException, OAuthProblemException {

        log.trace("getOAuth2Profile start {}", oAuth2Response);

        String accessToken = oAuth2Response.getAccessToken();
        String userProfileLocation = provider.getProfileLocation();

        if (StringUtils.isNotBlank(userProfileLocation)) {
            OAuthBearerClientRequest builder = new OAuthBearerClientRequest(userProfileLocation)
                    .setAccessToken(accessToken);

            OAuthClientRequest bearerClientRequest;

            if (null == provider.getOAuth2AccessTokenType()) {
                bearerClientRequest = builder.buildQueryMessage();
            } else {
                switch (provider.getOAuth2AccessTokenType()) {
                    case REQUEST_PARAM:
                        bearerClientRequest = builder.buildQueryMessage();
                        break;
                    case BEARER:
                        bearerClientRequest = builder.buildHeaderMessage();
                        break;
                    case BODY:
                        bearerClientRequest = builder.buildBodyMessage();
                        break;
                    default:
                        bearerClientRequest = builder.buildQueryMessage();
                        break;
                }
            }

            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

            return oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
        }

        return null;
    }

    public OAuth2ProfileDetails getOAuth2UserInfo(Request request, OAuthResourceResponse resourceResponse, OAuthAccessTokenResponse tokenResponse, OAuth2Provider prov, String oAuth2Code, String returnUrl) throws BadRequestException {
        log.trace(" getOAuth2UserId start..." + resourceResponse);

        Map responseMap = null;
        if (resourceResponse != null) {
            String resourceResponseBody = resourceResponse.getBody();
            log.trace(" OAuthResourceResponse, body{}" + resourceResponseBody);

            request.getAttributes().put(OAuth2AuthenticationHandler.REQ_ATT_OAUTH_JSON, resourceResponseBody);

            responseMap = JSONUtils.parseJSON(resourceResponseBody);

            String userID = (String) responseMap.get("id");
            String userName = (String) responseMap.get("username");
            String message = (String) responseMap.get("message");
            int status = -1;
            Object errCode = responseMap.get("status");
            if (errCode instanceof Integer) {
                status = (Integer) errCode;
            } else if (errCode instanceof String) {
                status = Integer.parseInt((String) errCode);
            }

            if (status >= 400) {
                throw new BadRequestException(message);
            }

            if (log.isTraceEnabled()) {
                log.trace(" userID{}" + userID);
                log.trace(" userName{}" + userName);
            }
        }

        OAuth2ProfileDetails user = new OAuth2ProfileDetails();
        user.setCode(oAuth2Code);
        user.setAccessToken(tokenResponse.getAccessToken());
        user.setRefreshToken(tokenResponse.getRefreshToken());
        user.setExpiresIn(tokenResponse.getExpiresIn());
        user.setDetails(responseMap);
        user.setReturnUrl(returnUrl);

        if (prov != null) {
            user.setTokenLocation(prov.getTokenLocation());
            user.setProviderId(prov.getProviderId());
        }

        if (log.isTraceEnabled()) {
            log.trace(" oAuth2Code{}" + oAuth2Code);
            log.trace(" AccessToken{}" + user.getAccessToken());
            log.trace("\n\n");
        }

        return user;
    }
}
