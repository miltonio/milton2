/*
 *       Copyright McEvoy Software Ltd
 */
package io.milton.cache;

import java.util.Map;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Attempts to connect to a hazelcast cluster. To use this, be sure to include
 * the optional hazelcast dependencies
 *
 * @author brad
 */
public class LocalCacheManager implements CacheManager {

	private int maximumWeightedCapacity = 1000;

    public LocalCacheManager() {
    }


	@Override
    public Map getMap(String name) {
        return Caffeine.newBuilder()
                .maximumSize(maximumWeightedCapacity)
                .build().asMap();
    }

	public int getMaximumWeightedCapacity() {
		return maximumWeightedCapacity;
	}

	public void setMaximumWeightedCapacity(int maximumWeightedCapacity) {
		this.maximumWeightedCapacity = maximumWeightedCapacity;
	}
}
