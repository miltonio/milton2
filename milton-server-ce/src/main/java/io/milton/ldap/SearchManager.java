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

package io.milton.ldap;

import com.sun.jndi.ldap.BerDecoder;
import io.milton.common.LogUtils;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class SearchManager {

    private static final Logger log = LoggerFactory.getLogger(SearchManager.class);
    /**
     * Search threads map
     */
    private final HashMap<LdapConnection, Map<Integer, SearchRunnable>> mapOfSearchesByConnection = new HashMap<LdapConnection, Map<Integer, SearchRunnable>>();
    private final HashMap<UUID, LdapConnection> mapOfUuids = new HashMap<UUID, LdapConnection>();
    private final LdapTransactionManager txManager;

    public SearchManager(LdapTransactionManager txManager) {
        this.txManager = txManager;
    }        

    private Map<Integer, SearchRunnable> getThreadMap(LdapConnection connection) {
        synchronized (mapOfSearchesByConnection) {
            Map<Integer, SearchRunnable> map = mapOfSearchesByConnection.get(connection);
            if (map == null) {
                map = new HashMap<Integer, SearchRunnable>();
                mapOfSearchesByConnection.put(connection, map);
            }
            return map;
        }
    }

    public void cancelAllSearches(LdapConnection aThis) {
        Map<Integer, SearchRunnable> searchThreadMap = getThreadMap(aThis);
        synchronized (searchThreadMap) {
            for (SearchRunnable searchRunnable : searchThreadMap.values()) {
                searchRunnable.abandon();
            }
        }
    }

    public void beginAsyncSearch(LdapConnection aThis, int currentMessageId, final SearchRunnable searchRunnable) {
        searchRunnable.setUuid(UUID.randomUUID());
        Map<Integer, SearchRunnable> searchThreadMap = getThreadMap(aThis);
        synchronized (searchThreadMap) {
            searchThreadMap.put(currentMessageId, searchRunnable);
            mapOfUuids.put(searchRunnable.getUuid(), aThis);
        }
        Runnable runnableRunner = new Runnable() {

            @Override
            public void run() {
                try {
                    txManager.tx(searchRunnable);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        Thread searchThread = new Thread(runnableRunner);
        searchThread.setName(aThis.getName() + "-Search-" + currentMessageId);
        searchThread.start();
    }

    public void search(LdapConnection aThis, SearchRunnable searchRunnable) {
        searchRunnable.run();
    }

    public void abandonSearch(LdapConnection aThis, int currentMessageId, BerDecoder reqBer) {
        int abandonMessageId = 0;
        try {
            abandonMessageId = (Integer) Ldap.PARSE_INT_WITH_TAG_METHOD.invoke(reqBer, Ldap.LDAP_REQ_ABANDON);
            Map<Integer, SearchRunnable> searchThreadMap = getThreadMap(aThis);
            synchronized (searchThreadMap) {
                SearchRunnable searchRunnable = searchThreadMap.get(abandonMessageId);
                if (searchRunnable != null) {
                    searchRunnable.abandon();
                    searchThreadMap.remove(currentMessageId);
                }
            }
        } catch (IllegalAccessException e) {
            log.error("", e);
        } catch (InvocationTargetException e) {
            log.error("", e);
        }
        LogUtils.debug(log, "LOG_LDAP_REQ_ABANDON_SEARCH", currentMessageId, abandonMessageId);
    }

    public void searchComplete(UUID id, Integer currentMessageId) {
        LdapConnection con = mapOfUuids.get(id);
        if (con != null) {
            Map<Integer, SearchRunnable> searchThreadMap = getThreadMap(con);
            synchronized (searchThreadMap) {
                searchThreadMap.remove(currentMessageId);
            }
        }
    }
}
