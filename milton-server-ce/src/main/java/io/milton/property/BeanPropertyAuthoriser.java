/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.milton.property;

import io.milton.annotations.BeanPropertyResource;
import io.milton.annotations.BeanProperty;
import io.milton.http.AclUtils;
import io.milton.http.Request;
import io.milton.http.Response;
import io.milton.resource.AccessControlledResource;
import io.milton.resource.Resource;
import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class BeanPropertyAuthoriser implements PropertyAuthoriser{
	
	private static final Logger log = LoggerFactory.getLogger(BeanPropertyAuthoriser.class);

	private final BeanPropertySource beanPropertySource;
	private final PropertyAuthoriser wrapped;

	public BeanPropertyAuthoriser(BeanPropertySource beanPropertySource, PropertyAuthoriser wrapped) {
		this.beanPropertySource = beanPropertySource;
		this.wrapped = wrapped;
	}
		
	
	@Override
	public Set<CheckResult> checkPermissions(Request request, Request.Method method, PropertyPermission perm, Set<QName> fields, Resource resource) {
		log.trace("checkPermissions");
		Set<CheckResult> results = null;
		BeanPropertyResource anno = beanPropertySource.getAnnotation(resource);
		if (anno == null) {
			return results;
		}
		if( !(resource instanceof AccessControlledResource)) {
			return results;
		}
		AccessControlledResource acr = (AccessControlledResource) resource;
		List<AccessControlledResource.Priviledge> actualPrivs = acr.getPriviledges(request.getAuthorization());
		if( actualPrivs == null ) {
			log.trace("got null priviledges");
			return results;
		} else {
			if( log.isTraceEnabled() ) {
				log.trace("found priviledges: " + actualPrivs + " from resource: " + acr.getClass());
			}
		}
		for (QName name : fields) {
			if (!name.getNamespaceURI().equals(anno.value())) {
				log.debug("different namespace", anno.value(), name.getNamespaceURI());
			} else {
				PropertyDescriptor pd = beanPropertySource.getPropertyDescriptor(resource, name.getLocalPart());
				if (pd != null) {					
					AccessControlledResource.Priviledge role = getRequiredRole(name, resource, perm);
					if (role != null) {						
						if (log.isTraceEnabled()) {
							log.trace("requires Priviledge: " + role + "  for field: " + name);
						}
						// Now check if user has that priviledge on the resource												
						if( !AclUtils.containsPriviledge(role, actualPrivs)) {
							log.debug("not authorised to access field: " + name);
							if (results == null) {
								results = new HashSet<CheckResult>();
							}
							results.add(new CheckResult(name, Response.Status.SC_UNAUTHORIZED, "Not authorised to edit field: " + name.getLocalPart(), resource));
						}
					}
				}
			}
		}
		if (log.isTraceEnabled()) {
			if (results == null) {
				log.trace("no field errors");
			} else {
				log.trace("field errors: " + results.size());
			}
		}
		return results;
	}
	
	

    private AccessControlledResource.Priviledge getRequiredRole(QName name, Resource resource, PropertyPermission propertyPermission) {
        if (log.isTraceEnabled()) {
            log.trace("getRequiredRole: " + name);
        }

        PropertyDescriptor pd = beanPropertySource.getPropertyDescriptor(resource, name.getLocalPart());
        if (pd == null || pd.getReadMethod() == null) {
            log.trace("property not found, so use default role");
            return defaultRequiredRole(resource, propertyPermission);
        } else {
            BeanProperty anno = pd.getReadMethod().getAnnotation(BeanProperty.class);
            if (anno == null) {
                log.trace("no annotation");
                return defaultRequiredRole(resource, propertyPermission);
            }
            log.trace("got annotation");

            if (propertyPermission == PropertyPermission.READ) {
                return anno.readRole();
            } else {
                return anno.writeRole();
            }
        }
    }	
	
    private AccessControlledResource.Priviledge defaultRequiredRole(Resource resource, PropertyPermission propertyPermission) {
        if (propertyPermission == PropertyPermission.READ) {
            return AccessControlledResource.Priviledge.READ;
        } else {
			return AccessControlledResource.Priviledge.WRITE;
        }

    }		
	
}
