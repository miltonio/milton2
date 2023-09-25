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

import io.milton.common.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Periodically checks a map of Nonce's to remove those which
 * have expired.
 * <p>
 * The map should be a reference to the live map in use by a NonceProvider
 *
 * @author brad
 */
public class ExpiredNonceRemover implements Runnable, Service {

    private static final Logger log = LoggerFactory.getLogger(ExpiredNonceRemover.class);

    private static final int INTERVAL = 10;

    private final Map<UUID, Nonce> nonces;
    private final int nonceValiditySeconds;
    private final ScheduledExecutorService scheduler;

    public ExpiredNonceRemover(Map<UUID, Nonce> nonces, int nonceValiditySeconds) {
        this.nonces = nonces;
        this.nonceValiditySeconds = nonceValiditySeconds;
        scheduler = Executors.newScheduledThreadPool(1, new DaemonThreadFactory());
    }

    public void start() {
        log.debug("scheduling checks for expired nonces every {} seconds", INTERVAL);
        scheduler.scheduleAtFixedRate(this, 10, INTERVAL, SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }

    public void run() {
        Iterator<UUID> it = nonces.keySet().iterator();
        while (it.hasNext()) {
            UUID key = it.next();
            Nonce n = nonces.get(key);
            if (isExpired(n.getIssued())) {
                log.debug("removing expired nonce: {}", key);
                it.remove();
            }
        }
    }

    private boolean isExpired(Date issued) {
        long dif = (System.currentTimeMillis() - issued.getTime()) / 1000;
        return dif > nonceValiditySeconds;
    }


    private static class DaemonThreadFactory implements ThreadFactory {

        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, ExpiredNonceRemover.class.getCanonicalName());
            t.setDaemon(true);
            return t;
        }

    }
}
