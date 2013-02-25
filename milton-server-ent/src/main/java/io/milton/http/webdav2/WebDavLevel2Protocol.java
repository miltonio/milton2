/*
 * Copyright 2012 McEvoy Software Ltd.
 */
package io.milton.http.webdav2;

import io.milton.common.NameSpace;
import io.milton.http.*;
import io.milton.http.http11.CustomPostHandler;
import io.milton.http.values.ValueWriters;
import io.milton.property.PropertySource;
import io.milton.resource.LockableResource;
import io.milton.resource.PropFindableResource;
import io.milton.resource.Resource;
import io.milton.http.webdav.PropertyMap;
import io.milton.http.webdav.PropertyMap.StandardProperty;
import io.milton.http.webdav.UserAgentHelper;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.http.webdav.WebDavResponseHandler;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the methods and properties for DAV Level 2 support. Basically just locking
 *
 *
 * @author brad
 */
public class WebDavLevel2Protocol implements HttpExtension, PropertySource {

    private static final Logger log = LoggerFactory.getLogger(WebDavLevel2Protocol.class);
    public static final String DAV_URI = "DAV:";
    public static final String DAV_PREFIX = "d";
    public static final NameSpace NS_DAV = new NameSpace(DAV_URI, DAV_PREFIX);
    private final Set<Handler> handlers;
    private final PropertyMap propertyMap;
    private final UserAgentHelper userAgentHelper;
    private List<CustomPostHandler> customPostHandlers;

    public WebDavLevel2Protocol(HandlerHelper handlerHelper, WebDavResponseHandler responseHandler, ResourceHandlerHelper resourceHandlerHelper, UserAgentHelper userAgentHelper) {
        this.userAgentHelper = userAgentHelper;
        this.propertyMap = new PropertyMap(WebDavProtocol.NS_DAV.getName());
        propertyMap.add(new SupportedLockPropertyWriter());
        propertyMap.add(new LockDiscoveryPropertyWriter());

        handlers = new HashSet<Handler>();
        handlers.add(new LockHandler(responseHandler, handlerHelper));
        handlers.add(new UnlockHandler(resourceHandlerHelper, responseHandler));        
    }

    @Override
    public List<CustomPostHandler> getCustomPostHandlers() {
        return customPostHandlers;
    }

    public void setCustomPostHandlers(List<CustomPostHandler> customPostHandlers) {
        this.customPostHandlers = customPostHandlers;
    }

    @Override
    public Set<Handler> getHandlers() {
        return Collections.unmodifiableSet(handlers);
    }

    @Override
    public Object getProperty(QName name, Resource r) {
        Object o = propertyMap.getProperty(name, r);
        return o;
    }

    @Override
    public void setProperty(QName name, Object value, Resource r) {
        throw new UnsupportedOperationException("Not supported. Standard webdav properties are not writable");
    }    
    
    /**
     * Used as a marker to generate supported locks element in propfind
     * responses
     *
     * See SupportedLockValueWriter
     */
    public static class SupportedLocks {

        private final PropFindableResource res;

        public SupportedLocks(PropFindableResource res) {
            this.res = res;
        }

        public PropFindableResource getResource() {
            return res;
        }
    }


    @Override
    public PropertyMetaData getPropertyMetaData(QName name, Resource r) {
        PropertyMetaData propertyMetaData = propertyMap.getPropertyMetaData(name, r);
        if (propertyMetaData != null) {
            // Nautilus (at least on Ubuntu 12) doesnt like empty properties
            if (userAgentHelper.isNautilus(HttpManager.request())) {
                Object v = getProperty(name, r);
                if (v == null) {
                    return PropertyMetaData.UNKNOWN;
                } else if (v instanceof String) {
                    String s = (String) v;
                    if (s.trim().length() == 0) {
                        return PropertyMetaData.UNKNOWN;
                    }
                }
            }
        }

        return propertyMetaData;
    }

    @Override
    public void clearProperty(QName name, Resource r) {
        throw new UnsupportedOperationException("Not supported. Standard webdav properties are not writable");
    }

    @Override
    public List<QName> getAllPropertyNames(Resource r) {
        return propertyMap.getAllPropertyNames(r);
    }


//    <D:supportedlock/><D:lockdiscovery/>
    class LockDiscoveryPropertyWriter implements StandardProperty<LockToken> {

        @Override
        public LockToken getValue(PropFindableResource res) {
            if (!(res instanceof LockableResource)) {
                return null;
            }
            LockableResource lr = (LockableResource) res;
            LockToken token = lr.getCurrentLock();
            return token;
        }

        @Override
        public String fieldName() {
            return "lockdiscovery";
        }

        @Override
        public Class getValueClass() {
            return LockToken.class;
        }
    }

    class SupportedLockPropertyWriter implements StandardProperty<SupportedLocks> {

        @Override
        public SupportedLocks getValue(PropFindableResource res) {
            if (res instanceof LockableResource) {
                return new SupportedLocks(res);
            } else {
                return null;
            }
        }

        @Override
        public String fieldName() {
            return "supportedlock";
        }

        @Override
        public Class getValueClass() {
            return SupportedLocks.class;
        }
    }
}
