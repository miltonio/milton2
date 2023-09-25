/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.milton.http;

import io.milton.common.StringUtils;
import io.milton.resource.GetableResource;
import io.milton.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author brad
 */
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    public static final String ATT_AUTH_STATUS = "auth.service.status";
    public static final String ATT_AUTH_CALLED = "auth.service.called";
    private final List<AuthenticationHandler> authenticationHandlers;
    private List<ExternalIdentityProvider> externalIdentityProviders;
    private boolean disableExternal;
    private final String[] browserIds = {"msie", "firefox", "chrome", "safari", "opera"};

    /**
     * Creates a AuthenticationService using the given handlers. Use this if you
     * don't want the default of a BasicAuthHandler and a
     * DigestAuthenticationHandler
     *
     * @param authenticationHandlers
     */
    public AuthenticationService(List<AuthenticationHandler> authenticationHandlers) {
        this.authenticationHandlers = authenticationHandlers;
    }

    /**
     * Looks for an AuthenticationHandler which supports the given resource and
     * authorization header, and then returns the result of that handler's
     * authenticate method.
     * <p>
     * Returns null if no handlers support the request
     * <p>
     * Caches results so can be called multiple times in one request without
     * performacne overhead
     *
     * @param resource
     * @param request
     * @return - null if no authentication was attempted. Otherwise, an
     * AuthStatus object containing the Auth object and a boolean indicating
     * whether the login succeeded
     */
    public AuthStatus authenticate(Resource resource, Request request) {

        if (request.getAttributes().containsKey(ATT_AUTH_STATUS)) {
            return (AuthStatus) request.getAttributes().get(ATT_AUTH_STATUS);
        }

        // This is to prevent recursive calls into authenticate, which can happen
        // when resource location tries to do authentication
        if (request.getAttributes().containsKey(ATT_AUTH_CALLED)) {
            return null;
        }
        request.getAttributes().put(ATT_AUTH_CALLED, Boolean.TRUE);

        AuthStatus authStatus = _authenticate(resource, request);
        request.getAttributes().put(ATT_AUTH_STATUS, authStatus); // maybe null
        return authStatus;
    }

    private AuthStatus _authenticate(Resource resource, Request request) {
        log.trace("authenticate");
        Auth auth = request.getAuthorization();
        boolean preAuthenticated = (auth != null && auth.getTag() != null);
        if (preAuthenticated) {
            log.trace("request is pre-authenticated");
            return new AuthStatus(auth, false);
        }
        if (log.isTraceEnabled()) {
            log.trace("Checking authentication with auth handlers: " + authenticationHandlers.size());
            for (AuthenticationHandler h : authenticationHandlers) {
                log.trace(" - " + h);
            }
        }

        for (AuthenticationHandler h : authenticationHandlers) {
            if (h.supports(resource, request)) {
                Object loginToken = h.authenticate(resource, request);
                if (loginToken == null) {
                    log.warn("authentication failed by AuthenticationHandler:" + h.getClass());
                    return new AuthStatus(auth, true);
                } else {
                    if (log.isTraceEnabled()) {
                        log.trace("authentication passed by: " + h.getClass());
                    }
                    if (auth == null) { // some authentication handlers do not require an Auth object
                        auth = new Auth(Auth.Scheme.FORM, null, loginToken);
                        request.setAuthorization(auth);
                    }
                    auth.setTag(loginToken);
                }
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("handler does not support this resource and request. handler: " + h.getClass() + " resource: " + resource.getClass());
                }
            }
        }

        if (auth != null) {
            return new AuthStatus(auth, false);
        }

        log.trace("authentication did not locate a user, because no handler accepted the request");
        return null;
    }

    /**
     * Generates a list of http authentication challenges, one for each
     * supported authentication method, to be sent to the client.
     *
     * @param resource - the resoruce being requested
     * @param request  - the current request
     * @return - a list of http challenges
     */
    public List<String> getChallenges(Resource resource, Request request) {
        List<String> challenges = new ArrayList<>();
        for (AuthenticationHandler h : authenticationHandlers) {
            if (h.isCompatible(resource, request)) {
                log.debug("challenge for auth: " + h.getClass());
                h.appendChallenges(resource, request, challenges);
            } else {
                log.debug("not challenging for auth: " + h.getClass() + " for resource type: " + (resource == null ? "" : resource.getClass()));
            }
        }
        return challenges;
    }

    public List<AuthenticationHandler> getAuthenticationHandlers() {
        return authenticationHandlers;
    }

    public List<ExternalIdentityProvider> getExternalIdentityProviders() {
        return externalIdentityProviders;
    }

    public void setExternalIdentityProviders(List<ExternalIdentityProvider> externalIdentityProviders) {
        this.externalIdentityProviders = externalIdentityProviders;
    }

    public boolean isDisableExternal() {
        return disableExternal;
    }

    public void setDisableExternal(boolean disableExternal) {
        this.disableExternal = disableExternal;
    }

    /**
     * Determine if we can use external identify providers to authenticate this
     * request
     *
     * @param resource
     * @param request
     * @return
     */
    public boolean canUseExternalAuth(Resource resource, Request request) {
        if (isDisableExternal()) {
            log.trace("auth svc has disabled external auth");
            return false;
        }
        if (getExternalIdentityProviders() == null || getExternalIdentityProviders().isEmpty()) {
            log.trace("auth service has no external auth providers");
            return false;
        }

        // external authentication requires redirecting the user's browser to another
        // site and displaying a login form.
        // This can only be done if the resource
        // being requested is a webpage. This means that it is Getable and that it
        // has a content type of html
        if (resource instanceof GetableResource) {
            GetableResource gr = (GetableResource) resource;
            String ct = gr.getContentType("text/html");
            if (ct == null || !ct.contains("html")) {
                log.trace("is not of content type html");
                return false;
            }
        } else {
            log.trace("is not getable");
            return false; // not getable, so definitely not suitable for external auth
        }

        // This can only be done for user agents which support displaying forms and redirection
        // Ie typical web browsers are ok, webdav clients are generally not ok
        String ua = request.getUserAgentHeader();
        if (StringUtils.contains(ua.toLowerCase(), browserIds)) {
            log.trace("is a known web browser, so can offer external auth");
            return true;
        } else {
            log.trace("not a known web browser, so cannot offer external auth");
            return false;
        }

    }

    /**
     * Determine if there are any credentials present. Note this does not check
     * if the provided credentials are valid, only if they are available
     *
     * @param request
     * @return
     */
    public boolean authenticateDetailsPresent(Request request) {
        for (AuthenticationHandler h : authenticationHandlers) {
            if (h.credentialsPresent(request)) {
                return true;
            }
        }
        return false;
    }

    public static class AuthStatus {

        public final Auth auth;
        public final boolean loginFailed;

        public AuthStatus(Auth auth, boolean loginFailed) {
            this.auth = auth;
            this.loginFailed = loginFailed;
        }

        @Override
        public String toString() {
            if (auth == null) {
                return "AuthStatus: no creds";
            }
            if (loginFailed) {
                return "AuthStatus: login failed: " + auth.getUser();
            } else {
                return "AuthStatus: logged in: " + auth.getUser();
            }
        }
    }
}
