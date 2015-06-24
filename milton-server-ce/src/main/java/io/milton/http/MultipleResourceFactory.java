/*
 *
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.milton.http;

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipleResourceFactory implements ResourceFactory {

	private final Logger log = LoggerFactory.getLogger(MultipleResourceFactory.class);
    protected final List<ResourceFactory> factories;
    protected Map<String, ResourceFactory> mapOfFactoriesByHost;
	
    public MultipleResourceFactory() {
        factories = new ArrayList<ResourceFactory>();
    }

    public MultipleResourceFactory( List<ResourceFactory> factories ) {
        this.factories = factories;
    }

	@Override
    public Resource getResource( String host, String url ) throws NotAuthorizedException, BadRequestException {
        if( log.isTraceEnabled() ) {
            log.trace( "getResource: " + url );
        }
        ResourceFactory hostRf = null;
        if( mapOfFactoriesByHost != null ) {
            hostRf = mapOfFactoriesByHost.get( host );
        }
        Resource theResource;
        if( hostRf != null ) {
            theResource = hostRf.getResource( host, url );
        } else {
            theResource = findFromFactories( host, url );
        }
        if( theResource == null ) {
            log.debug( "no resource factory supplied a resouce" );
        } else {
			
		}
        return theResource;
    }

	/**
	 * Allows factories to be added after construction
	 * 
	 * @param rf 
	 */
	public void add(ResourceFactory rf) {
		factories.add(rf);
	}
	
	public void addAsFirst(ResourceFactory rf) {
		factories.add(0, rf);
	}	
	
    /**
     * When set will always be used exclusively for any matching hosts
     * 
     * @return
     */
    public Map<String, ResourceFactory> getMapOfFactoriesByHost() {
        return mapOfFactoriesByHost;
    }

    public void setMapOfFactoriesByHost( Map<String, ResourceFactory> mapOfFactoriesByHost ) {
        this.mapOfFactoriesByHost = mapOfFactoriesByHost;
    }

    private Resource findFromFactories( String host, String url ) throws NotAuthorizedException, BadRequestException {
        for( ResourceFactory rf : factories ) {
            Resource r = rf.getResource( host, url );
            if( r != null ) {
				if(log.isTraceEnabled()) {
					log.trace("Found resource: " + r.getClass() + " from factory: " + rf.getClass());
				}
                return r;
            }
        }
        return null;
    }	
}
