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

import java.util.Map;

/**
 *
 * @author Lee YOU
 */
public interface OAuth2Resource extends Resource {

    /**
     * Called when an oauth2 login has been authenticated, with details received
     * from the remote server. The method should return an application specific
     * object representing the user. Or return null to reject the
     * authentication.
     *
     * @param profile - the details about the current user as provided by the
     * remote authentication server
     * @return an object which represents the current principal, or null to
     * reject the login
     */
    Object authenticate(OAuth2ProfileDetails profile);

    Map<String,OAuth2Provider> getOAuth2Providers();
    

    /**
     * This contains the information about the authenticated profile
     */
    public static class OAuth2ProfileDetails {

        private String tokenLocation;
        private String accessToken;
        private String code;

        private Map details;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTokenLocation() {
            return tokenLocation;
        }

        public void setTokenLocation(String tokenLocation) {
            this.tokenLocation = tokenLocation;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public Map getDetails() {
            return details;
        }

        public void setDetails(Map details) {
            this.details = details;
        }

    }

}
