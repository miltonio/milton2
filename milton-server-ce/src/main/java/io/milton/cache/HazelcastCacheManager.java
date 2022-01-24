/* 
 *       Copyright McEvoy Software Ltd
 */
package io.milton.cache;

import com.hazelcast.cluster.Cluster;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Attempts to connect to a hazelcast cluster. To use this, be sure to include
 * the optional hazelcast dependencies
 *
 * @author brad
 */
public class HazelcastCacheManager implements CacheManager {
    
    private static final Logger log = LoggerFactory.getLogger(HazelcastCacheManager.class);
    
    private final HazelcastInstance hazel;
    private final Cluster cluster;

    public HazelcastCacheManager() {
        hazel = Hazelcast.newHazelcastInstance();
        log.info("Connected to hazelcast cluster");
        cluster = hazel.getCluster();
    }

	@Override
    public Map getMap(String name) {
        if( hazel != null ) {
            return hazel.getMap(name);
        } else {
			log.warn("Not connected to hazelcast, using local data only");
            return new ConcurrentHashMap();
        }
    }
}
