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

package io.milton.gae;

import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import io.milton.http.Request;
import io.milton.http.http11.auth.Nonce;
import io.milton.http.http11.auth.NonceProvider;
import io.milton.http.http11.auth.NonceProvider.NonceValidity;
import io.milton.resource.Resource;

/**
 * Uses google's memcache implementation to store nonces which are available
 * across the cluster.
 *
 * Note that memcache access is **not secure** so these nonce values will
 * be accessible by other applications.
 *
 * However, the since nonce's are public information anyway (ie they are sent
 * in clear text in http request and responses) then that should not cause
 * any security problems.
 *
 * @author Scott Hernandez
 */
public class AppEngineMemcacheNonceProvider implements NonceProvider {

	private static final Logger log = Logger.getLogger(AppEngineMemcacheNonceProvider.class.getName() );
    
    protected final int nonceValiditySeconds;
    protected boolean enableNonceCountChecking;
    protected MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
    
    public AppEngineMemcacheNonceProvider( int nonceValiditySeconds ) {
        this.nonceValiditySeconds = nonceValiditySeconds;
        log.info( "created" );
    }

    @Override
    public String createNonce( Resource resource, Request request ) {
        UUID id = UUID.randomUUID();
        Date now = new Date();
        Nonce n = new Nonce( id, now );
        memcache.put( n.getValue(), n, Expiration.byDeltaSeconds(nonceValiditySeconds));
        log.info( "created nonce: " + n.getValue() );
        return n.getValue().toString();
    }

    @Override
    public NonceValidity getNonceValidity( String nonce, Long nc ) {
        log.info( "getNonceValidity: " + nonce );
        UUID value = null;
        try {
            value = UUID.fromString( nonce );
        } catch( Exception e ) {
            log.info( "couldnt parse nonce" );
            return NonceValidity.INVALID;
        }
        Nonce n = (Nonce)memcache.get(value);
        if( n == null ) {
            log.info( "not found in cache" );
            return NonceValidity.INVALID;
        } else {
            if( isExpired( n.getIssued() ) ) {
                log.info( "nonce has expired; that is unusual as it should have been evicted from the cache already." );
                return NonceValidity.EXPIRED;
            } else {
                if( nc == null ) {
                    log.info( "nonce ok" );
                    return NonceValidity.OK;
                } else {
                    if( enableNonceCountChecking && nc <= n.getNonceCount() ) {
                        log.warning( "nonce-count was not greater then previous, possible replay attack. new: " + nc + " old:" + n.getNonceCount() );
                        return NonceValidity.INVALID;
                    } else {
                        log.info( "nonce and nonce-count ok" );
                        Nonce newNonce = n.increaseNonceCount( nc );
                        memcache.put( newNonce.getValue(), newNonce, Expiration.byDeltaSeconds(nonceValiditySeconds));
                        return NonceValidity.OK;
                    }
                }
            }
        }
    }

    private boolean isExpired( Date issued ) {
        long dif = ( System.currentTimeMillis() - issued.getTime() ) / 1000;
        return dif > nonceValiditySeconds;
    }

    /**
     * IE seems to send nc (nonce count) parameters out of order. To correctly
     * implement checking we need to record which nonces have been sent, and not
     * assume they will be sent in a monotonically increasing sequence.
     *
     * The quick fix here is to disable checking of the nc param, since other
     * common servers seem to do so to.
     *
     * Note that this will allow replay attacks.
     *
     * @return
     */
    public boolean isEnableNonceCountChecking() {
        return enableNonceCountChecking;
    }

    public void setEnableNonceCountChecking( boolean enableNonceCountChecking ) {
        this.enableNonceCountChecking = enableNonceCountChecking;
    }

}
