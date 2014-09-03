/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 */

package io.milton.http.acl;

import io.milton.resource.AccessControlledResource;
import io.milton.http.webdav.ResourceTypeHelper;
import io.milton.resource.Resource;
import java.util.List;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author alex
 */
public class AccessControlledResourceTypeHelper implements ResourceTypeHelper {

    private static final Logger log = LoggerFactory.getLogger( AccessControlledResourceTypeHelper.class );
    private final ResourceTypeHelper wrapped;

    public AccessControlledResourceTypeHelper( ResourceTypeHelper wrapped ) {
        this.wrapped = wrapped;
    }

	@Override
    public List<QName> getResourceTypes( Resource r ) {
        List<QName> list = wrapped.getResourceTypes( r );
        return list;
    }

	@Override
    public List<String> getSupportedLevels( Resource r ) {
        log.trace( "getSupportedLevels" );
        List<String> list = wrapped.getSupportedLevels( r );
        if( r instanceof AccessControlledResource ) {
            list.add( "access-control" );
        }
        return list;
    }
}
