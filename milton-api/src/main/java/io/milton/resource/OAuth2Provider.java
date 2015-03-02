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
public interface OAuth2Provider {

    /**
     * Identifies the provider to this application, ie 'facebook' or 'twitter' or 'myownserver'
     * 
     * @return 
     */
    String getProviderId();
    
    /**
     * This is the URL we will redirect the user to, where they will enter their
     * username and password into the remote application (if required) and authorise
     * our app
     * 
     * @return 
     */
    String getAuthLocation();

    String getClientId();

    String getClientSecret();

    String getRedirectURI();

    /**
     * This is the URL we will call direct (server to server) to get an access token
     * from the access code received in the redirect back to our site from the oauth server
     * 
     * @return 
     */
    String getTokenLocation();

    String getProfileLocation();
    
    /**
     * Returns a list of named permission scopes, such as "email", "profile", etc,
     * which determine what this client is permitted to do
     * 
     * @return 
     */
    Collection<String> getPermissionScopes();
    
}
