package io.milton.http.fs;

import io.milton.cache.CacheManager;
import io.milton.http.PropertyManager;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.property.PropertySource;
import io.milton.resource.MultiNamespaceCustomPropertyResource;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keys on getUniqueID of the locked resource.
 */
public class SimplePropertyManager implements PropertyManager {

    private final Map<String, Map<QName, Object>> propertiesByUniqueId;

    public SimplePropertyManager(CacheManager cacheManager) {
        this.propertiesByUniqueId = cacheManager.getMap("fuse-properties-byuniqueId");
    }

    @Override
    public Object getProperty(QName name, MultiNamespaceCustomPropertyResource resource) {
        final Map<QName, Object> resourceProperties = propertiesByUniqueId.get(resource.getUniqueId());
        if (resourceProperties != null) {
            return resourceProperties.get(name);
        }
        return null;
    }

    @Override
    public void setProperty(QName name, Object value, MultiNamespaceCustomPropertyResource resource) {
        propertiesByUniqueId.computeIfAbsent(resource.getUniqueId(), k -> new HashMap<>());
        final Map<QName, Object> map = propertiesByUniqueId.get(resource.getUniqueId());
        if (value == null) {
            map.remove(name);
        } else {
            map.put(name, value);
        }
    }

    @Override
    public PropertySource.PropertyMetaData getPropertyMetaData(QName name, MultiNamespaceCustomPropertyResource resource) {
        if (!name.getNamespaceURI().equals(WebDavProtocol.NS_DAV.getName()) && !name.getNamespaceURI().contains("caldav") && !name.getNamespaceURI().contains("carddav")) {
            final Map<QName, Object> map = propertiesByUniqueId.get(resource.getUniqueId());
            if (map != null && map.containsKey(name)) {
                return new PropertySource.PropertyMetaData(PropertySource.PropertyAccessibility.WRITABLE, String.class);
            } else {
                return new PropertySource.PropertyMetaData(PropertySource.PropertyAccessibility.UNKNOWN, String.class, true);
            }
        }
        return null;
    }

        @Override
        public List<QName> getAllPropertyNames (MultiNamespaceCustomPropertyResource resource){
            final Map<QName, Object> resourceProperties = propertiesByUniqueId.get(resource.getUniqueId());
            if (resourceProperties != null) {
                return new ArrayList<>(resourceProperties.keySet());
            }
            return null;
        }
    }
