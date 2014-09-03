/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 */

package io.milton.http.carddav;

import io.milton.http.webdav.ResourceTypeHelper;
import io.milton.resource.AddressBookResource;
import io.milton.resource.Resource;
import io.milton.webdav.utils.LockUtils;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class AddressBookResourceTypeHelper implements ResourceTypeHelper {

    private static final Logger log = LoggerFactory.getLogger(AddressBookResourceTypeHelper.class);
    private final ResourceTypeHelper wrapped;

    public AddressBookResourceTypeHelper(ResourceTypeHelper wrapped) {
        log.debug("CalendarResourceTypeHelper constructed :" + wrapped.getClass().getSimpleName());
        this.wrapped = wrapped;
    }

    @Override
    public List<QName> getResourceTypes(Resource r) {
        if (log.isTraceEnabled()) {
            log.trace("getResourceTypes:" + r.getClass().getCanonicalName());
        }
        QName qn;
        List<QName> list = wrapped.getResourceTypes(r);
		
        if (r instanceof AddressBookResource) {
            log.trace("getResourceTypes: is a AddressBookResource");
            qn = new QName(CardDavProtocol.CARDDAV_NS, "addressbook");
            if (list == null) {
                list = new ArrayList<QName>();
            }
            list.add(qn);
        }
        return list;
    }

    /**
     *
     *
     * @param r
     * @return
     */
    @Override
    public List<String> getSupportedLevels(Resource r) {
        log.debug("getSupportedLevels");
        List<String> list = wrapped.getSupportedLevels(r);
//        if (r instanceof AddressBookResource) {
		LockUtils.add(list,"3");			
		LockUtils.add(list,"addressbook");	
		LockUtils.add(list,"extended-mkcol");			
//        }
        return list;
    }	
}
