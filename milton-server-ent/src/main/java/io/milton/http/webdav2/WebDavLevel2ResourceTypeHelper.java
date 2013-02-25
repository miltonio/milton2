/*
 * Copyright 2012 McEvoy Software Ltd.
 */
package io.milton.http.webdav2;

import io.milton.http.webdav.ResourceTypeHelper;
import io.milton.http.webdav.WebDavResourceTypeHelper;
import io.milton.resource.LockableResource;
import io.milton.resource.Resource;
import io.milton.webdav.utils.LockUtils;
import java.util.List;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Advertises support for DAV level 2 for resources which support locking
 *
 * @author brad
 */
public class WebDavLevel2ResourceTypeHelper implements ResourceTypeHelper {

    private static final Logger log = LoggerFactory.getLogger(WebDavResourceTypeHelper.class);

    private final ResourceTypeHelper wrapped;

    public WebDavLevel2ResourceTypeHelper(ResourceTypeHelper wrapped) {
        this.wrapped = wrapped;
    }
    
    
    
    @Override
    public List<QName> getResourceTypes(Resource r) {
        return wrapped.getResourceTypes(r);
    }

    //Need to create a ArrayList as Arrays.asList returns a fixed length list which
    //cannot be extended.
    @Override
    public List<String> getSupportedLevels(Resource r) {
        List<String> list = wrapped.getSupportedLevels(r);
        if (r instanceof LockableResource) {
            LockUtils.add(list, "2");            
        }
        return list;
    }
}
