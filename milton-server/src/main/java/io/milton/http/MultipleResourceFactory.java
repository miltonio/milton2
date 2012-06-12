package io.milton.http;

import io.milton.resource.Resource;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipleResourceFactory implements ResourceFactory {

    private Logger log = LoggerFactory.getLogger( MultipleResourceFactory.class );
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
