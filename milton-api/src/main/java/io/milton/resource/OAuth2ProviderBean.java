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
package io.milton.resource;

import java.util.Collection;

/**
 *
 * @author brad
 */
public class OAuth2ProviderBean implements OAuth2Provider {

    private final String providerId;
    private final String location;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String tokenLocation;
    private final String profileLocation;
    private final Collection scopes;

    public OAuth2ProviderBean(String providerId, String location, String clientId, String clientSecret, String redirectUri, String tokenLocation, String profileLocation, Collection scopes) {
        this.providerId = providerId;
        this.location = location;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.tokenLocation = tokenLocation;
        this.profileLocation = profileLocation;
        this.scopes = scopes;
    }
    
    
    
    
    @Override
    public String getAuthLocation() {
        return location;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    @Override
    public String getRedirectURI() {
        return redirectUri;
    }

    @Override
    public String getTokenLocation() {
        return tokenLocation;
    }

    @Override
    public String getProfileLocation() {
        return profileLocation;
    }

    @Override
    public String getProviderId() {
        return providerId;
    }       

    @Override
    public Collection<String> getPermissionScopes() {
        return scopes;
    }
}
