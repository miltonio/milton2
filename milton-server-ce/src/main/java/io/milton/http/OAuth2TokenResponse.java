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
package io.milton.http;

import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.token.BasicOAuthToken;
import org.apache.oltu.oauth2.common.token.OAuthToken;
import org.apache.oltu.oauth2.common.utils.JSONUtils;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;

/**
 *
 * @author Lee YOU
 */
public class OAuth2TokenResponse extends OAuthAccessTokenResponse {

	private String OAuth2URL;

	public String getOAuth2URL() {
		return OAuth2URL;
	}

	public void setOAuth2URL(String OAuth2URL) {
		this.OAuth2URL = OAuth2URL;
	}

	@Override
	public String getAccessToken() {
		return getParam(OAuth.OAUTH_ACCESS_TOKEN);
	}

	@Override
	public Long getExpiresIn() {
		String value = getParam(OAuth.OAUTH_EXPIRES_IN);
		return value == null ? null : Long.valueOf(value);
	}

	@Override
	public String getRefreshToken() {
		return getParam(OAuth.OAUTH_EXPIRES_IN);
	}

	@Override
	public String getScope() {
		return getParam(OAuth.OAUTH_SCOPE);
	}

	@Override
	public OAuthToken getOAuthToken() {
		return new BasicOAuthToken(getAccessToken(), getExpiresIn(), getRefreshToken(), getScope());
	}

	@Override
	protected void setBody(String body) throws OAuthProblemException {
		this.body = body.trim();
		if (isJson()) {
			try {
				parameters = JSONUtils.parseJSON(body);
			} catch (Throwable e) {
				throw OAuthProblemException.error(OAuthError.CodeResponse.UNSUPPORTED_RESPONSE_TYPE, "Invalid response! Response body is not " + OAuth.ContentType.JSON + " encoded");
			}

		} else {
			parameters = OAuthUtils.decodeForm(body);
		}
	}

	@Override
	protected void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	protected void setResponseCode(int code) {
		this.responseCode = code;
	}

	private boolean isJson() {
		return body != null && body.startsWith("{");
	}

}
