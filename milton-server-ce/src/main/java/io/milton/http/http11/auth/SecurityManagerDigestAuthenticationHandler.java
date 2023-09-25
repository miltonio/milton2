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

package io.milton.http.http11.auth;

import io.milton.http.Auth;
import io.milton.http.AuthenticationHandler;
import io.milton.http.Request;
import io.milton.http.SecurityManager;
import io.milton.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author brad
 */
public class SecurityManagerDigestAuthenticationHandler implements AuthenticationHandler {

    private static final Logger log = LoggerFactory.getLogger(SecurityManagerDigestAuthenticationHandler.class);
    private final NonceProvider nonceProvider;

    private final SecurityManager securityManager;
    private final DigestHelper digestHelper;

    public SecurityManagerDigestAuthenticationHandler(NonceProvider nonceProvider, SecurityManager securityManager) {
        this.nonceProvider = nonceProvider;
        this.securityManager = securityManager;
        digestHelper = new DigestHelper(nonceProvider);
    }

    public SecurityManagerDigestAuthenticationHandler(SecurityManager securityManager) {
        Map<UUID, Nonce> nonces = new ConcurrentHashMap<>();
        int nonceValiditySeconds = 60 * 60 * 24;
        ExpiredNonceRemover expiredNonceRemover = new ExpiredNonceRemover(nonces, nonceValiditySeconds);
        this.nonceProvider = new SimpleMemoryNonceProvider(nonceValiditySeconds, expiredNonceRemover, nonces);
        this.securityManager = securityManager;
        digestHelper = new DigestHelper(nonceProvider);
    }

    @Override
    public boolean credentialsPresent(Request request) {
        return request.getAuthorization() != null;
    }

    @Override
    public boolean supports(Resource r, Request request) {
        Auth auth = request.getAuthorization();
        if (auth == null) {
            return false;
        }
        return Auth.Scheme.DIGEST.equals(auth.getScheme());
    }

    @Override
    public Object authenticate(Resource r, Request request) {
        Auth auth = request.getAuthorization();
        DigestResponse resp = digestHelper.calculateResponse(auth, securityManager.getRealm(request.getHostHeader()), request.getMethod());
        if (resp == null) {
            log.debug("requested digest authentication is invalid or incorrectly formatted");
            return null;
        } else {
            return securityManager.authenticate(resp);
        }

    }

    @Override
    public void appendChallenges(Resource resource, Request request, List<String> challenges) {
        String nonceValue = nonceProvider.createNonce(request);
        challenges.add(digestHelper.getChallenge(nonceValue, request.getAuthorization(), securityManager.getRealm(request.getHostHeader())));
    }

    @Override
    public boolean isCompatible(Resource resource, Request request) {
        return true;
    }
}

