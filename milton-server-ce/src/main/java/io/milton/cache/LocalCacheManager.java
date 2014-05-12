/* 
 *       Copyright McEvoy Software Ltd
 */
package io.milton.cache;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

/**
 * Attempts to connect to a hazelcast cluster. To use this, be sure to include
 * the optional hazelcast dependencies
 *
 * @author brad
 */
public class LocalCacheManager implements CacheManager {
    
    private static final Logger log = LoggerFactory.getLogger(LocalCacheManager.class);
    
	private int maximumWeightedCapacity = 1000;
	
    public LocalCacheManager() {
    }

	
	@Override
    public Map getMap(String name) {
		ConcurrentLinkedHashMap cache = new ConcurrentLinkedHashMap.Builder()
				.maximumWeightedCapacity(maximumWeightedCapacity)
				.build();		
		return cache;
    }

	public int getMaximumWeightedCapacity() {
		return maximumWeightedCapacity;
	}

	public void setMaximumWeightedCapacity(int maximumWeightedCapacity) {
		this.maximumWeightedCapacity = maximumWeightedCapacity;
	}
	
	
}
