/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.milton.http.http11.auth;

import io.milton.http.Auth;
import io.milton.http.AuthenticationHandler;
import io.milton.http.Request;
import io.milton.resource.Resource;
import io.milton.http.SecurityManager;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class SecurityManagerDigestAuthenticationHandler implements AuthenticationHandler {

    private static final Logger log = LoggerFactory.getLogger( SecurityManagerDigestAuthenticationHandler.class );
    private final NonceProvider nonceProvider;

    private final SecurityManager securityManager;
    private final DigestHelper digestHelper;

    public SecurityManagerDigestAuthenticationHandler( NonceProvider nonceProvider, SecurityManager securityManager ) {
        this.nonceProvider = nonceProvider;
        this.securityManager = securityManager;
        digestHelper = new DigestHelper(nonceProvider);
    }

    public SecurityManagerDigestAuthenticationHandler(SecurityManager securityManager) {
		Map<UUID, Nonce> nonces = new ConcurrentHashMap<UUID, Nonce>();
		int nonceValiditySeconds = 60*60*24;
		ExpiredNonceRemover expiredNonceRemover = new ExpiredNonceRemover(nonces, nonceValiditySeconds);
		this.nonceProvider = new SimpleMemoryNonceProvider(nonceValiditySeconds, expiredNonceRemover, nonces);
        this.securityManager = securityManager;
        digestHelper = new DigestHelper(nonceProvider);
    }

	@Override
    public boolean supports( Resource r, Request request ) {
        Auth auth = request.getAuthorization();
        if( auth == null ) {
            return false;
        }
        return  Auth.Scheme.DIGEST.equals( auth.getScheme() );
    }

	@Override
    public Object authenticate( Resource r, Request request ) {
        Auth auth = request.getAuthorization();
        DigestResponse resp = digestHelper.calculateResponse(auth, securityManager.getRealm(request.getHostHeader()), request.getMethod());
        if( resp == null ) {
            log.debug("requested digest authentication is invalid or incorrectly formatted");
            return null;
        } else {
            Object o = securityManager.authenticate( resp );
            return o;
        }

    }

	@Override
    public String getChallenge( Resource resource, Request request ) {
        String nonceValue = nonceProvider.createNonce( resource, request );
        return digestHelper.getChallenge(nonceValue, request.getAuthorization(), securityManager.getRealm(request.getHostHeader()));
    }

	@Override
    public boolean isCompatible( Resource resource, Request request ) {
        return true;
    }
}

