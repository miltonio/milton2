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

import io.milton.http.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A very simple nonce provide that users a map to store issued nonces.
 * <p>
 * If the map is constructed internally it will be a ConcurrentHashMap, which
 * will restrict the application to a single machine, and nonces will not be
 * preserved across restarts.
 * <p>
 * To improve reliability and scalability provide an alternative map
 * implementation. For example, it could be a cluster aware map which
 * synchronizes across a cluster. Or it could be a map which persists entries to
 * a database or file.
 *
 * @author brad
 */
public class SimpleMemoryNonceProvider implements NonceProvider {

    private static final Logger log = LoggerFactory.getLogger(SimpleMemoryNonceProvider.class);
    private final int nonceValiditySeconds;
    private final Map<UUID, Nonce> nonces;
    private final ExpiredNonceRemover remover;
    private boolean enableNonceCountChecking;

    public SimpleMemoryNonceProvider(int nonceValiditySeconds, ExpiredNonceRemover remover, Map<UUID, Nonce> nonces) {
        if (log.isTraceEnabled()) {
            log.trace("Created SimpleMemoryNonceProvider(b): nonceValiditySeconds: {}", nonceValiditySeconds);
        }
        this.nonces = nonces;
        this.nonceValiditySeconds = nonceValiditySeconds;
        this.remover = remover;
    }

    public SimpleMemoryNonceProvider(int nonceValiditySeconds) {
        if (log.isTraceEnabled()) {
            log.trace("Created SimpleMemoryNonceProvider(b): nonceValiditySeconds: {}", nonceValiditySeconds);
        }
        this.nonces = new ConcurrentHashMap<>();
        this.nonceValiditySeconds = nonceValiditySeconds;
        this.remover = new ExpiredNonceRemover(nonces, nonceValiditySeconds);
    }

    public Nonce createNonceObject(Request request) {
        UUID id = UUID.randomUUID();
        Date now = new Date();
        Nonce n = new Nonce(id, now);
        nonces.put(n.getValue(), n);
        return n;
    }

    @Override
    public String createNonce(Request request) {
        String n = createNonceObject(request).getValue().toString();
        if (log.isTraceEnabled()) {
            log.trace("Created nonce: {} in map of size: {}", n, nonces.size());
        }
        return n;
    }

    @Override
    public NonceValidity getNonceValidity(String nonce, Long nc) {
        if (log.isTraceEnabled()) {
            log.trace("getNonceValidity: {}", nonce);
        }
        UUID value = null;
        try {
            value = UUID.fromString(nonce);
        } catch (Exception e) {
            log.warn("couldnt parse nonce");
            return NonceValidity.INVALID;
        }
        Nonce n = nonces.get(value);
        if (n == null) {
            log.debug("not found in map of size: {}", nonces.size());
            return NonceValidity.INVALID;
        } else {
            if (isExpired(n.getIssued())) {
                log.debug("nonce has expired");
                return NonceValidity.EXPIRED;
            } else {
                if (nc == null) {
                    log.trace("nonce ok");
                    return NonceValidity.OK;
                } else {
                    if (enableNonceCountChecking && nc <= n.getNonceCount()) {
                        log.warn("nonce-count was not greater then previous, possible replay attack. new: {} old: {}", nc,  n.getNonceCount());
                        return NonceValidity.INVALID;
                    } else {
                        log.trace("nonce and nonce-count ok");
                        Nonce newNonce = n.increaseNonceCount(nc);
                        nonces.put(newNonce.getValue(), newNonce);
                        return NonceValidity.OK;
                    }
                }
            }
        }
    }

    private boolean isExpired(Date issued) {
        long dif = (System.currentTimeMillis() - issued.getTime()) / 1000;
        return dif > nonceValiditySeconds;
    }

    /**
     * IE seems to send nc (nonce count) parameters out of order. To correctly
     * implement checking we need to record which nonces have been sent, and not
     * assume they will be sent in a monotonically increasing sequence.
     * <p>
     * The quick fix here is to disable checking of the nc param, since other
     * common servers seem to do so to.
     * <p>
     * Note that this will allow replay attacks.
     *
     * @return
     */
    public boolean isEnableNonceCountChecking() {
        return enableNonceCountChecking;
    }

    public void setEnableNonceCountChecking(boolean enableNonceCountChecking) {
        this.enableNonceCountChecking = enableNonceCountChecking;
    }

    public Map<UUID, Nonce> getNonces() {
        return nonces;
    }


}


