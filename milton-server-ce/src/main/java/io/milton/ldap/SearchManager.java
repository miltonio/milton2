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
