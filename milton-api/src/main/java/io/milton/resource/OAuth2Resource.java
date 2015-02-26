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

/**
 *
 * @author Lee YOU
 */
public interface OAuth2Resource extends Resource {

//    static final int DEFAULT_STEP = -1;
//    static final int GRANT_PERMISSION = 1;
//    static final int OBTAIN_TOKEN = 2;

//    int getOAuth2Step();

    String getOAuth2PermissionResponse();

    String getOAuth2Location();

    String getOAuth2ClientId();

    String getOAuth2ClientSecret();

    String getOAuth2RedirectURI();

    String getOAuth2TokenLocation();

    String getOAuth2UserProfileLocation();

    void setOAuth2TokenUser(Object obj);

    Object getOAuth2TokenUser();

    boolean isOAuth2Authorized();

}
