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
import io.milton.resource.DigestResource;
import io.milton.http.Request;
import io.milton.resource.Resource;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class DigestAuthenticationHandler implements AuthenticationHandler {

    private static final Logger log = LoggerFactory.getLogger( DigestAuthenticationHandler.class );
    private final NonceProvider nonceProvider;
    private final DigestHelper digestHelper;


    public DigestAuthenticationHandler( NonceProvider nonceProvider ) {
        this.nonceProvider = nonceProvider;
        this.digestHelper = new DigestHelper(nonceProvider);
    }

	@Override
	public boolean credentialsPresent(Request request) {
		return request.getAuthorization() != null;
	}	
	
	@Override
    public boolean supports( Resource r, Request request ) {
        Auth auth = request.getAuthorization();
        if( auth == null ) {
            return false;
        }
        boolean b;
        if( r instanceof DigestResource ) {
            DigestResource dr = (DigestResource) r;
            if( dr.isDigestAllowed()) {
                b = Auth.Scheme.DIGEST.equals( auth.getScheme() );
            } else {
                log.trace("digest auth is not allowed");
                b = false;
            }
        } else {
            log.trace( "resource is not an instanceof DigestResource" );
            b = false;
        }
        return b;
    }

	@Override
    public Object authenticate( Resource r, Request request ) {
        DigestResource digestResource = (DigestResource) r;
        Auth auth = request.getAuthorization();
		String realm = r.getRealm();
		if( realm == null ) {
			throw new NullPointerException("Got null realm from resource: " + r.getClass());
		}
        DigestResponse resp = digestHelper.calculateResponse(auth, realm, request.getMethod());
        if( resp == null ) {
            log.info("requested digest authentication is invalid or incorrectly formatted");
            return null;
        } else {
            Object o = digestResource.authenticate( resp );
			if( o == null ) {
				log.info("digest authentication failed from resource: " + digestResource.getClass() + " - " + digestResource.getName() + " for user: " + resp.getUser());
			}
            return o;
        }
    }

	@Override
    public void appendChallenges( Resource resource, Request request, List<String> challenges ) {

        String nonceValue = nonceProvider.createNonce( resource, request );
        challenges.add( digestHelper.getChallenge(nonceValue, request.getAuthorization(), resource.getRealm()) );
    }

	@Override
    public boolean isCompatible( Resource resource, Request request ) {
        if ( resource instanceof DigestResource ) {
			DigestResource dr = (DigestResource) resource;
			return dr.isDigestAllowed();
		} else {
			log.trace("Digest auth not supported because class does not implement DigestResource");
			return false;
		}
    }
}

