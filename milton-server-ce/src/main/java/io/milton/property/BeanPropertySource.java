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

import io.milton.resource.Resource;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.common.LogUtils;
import io.milton.property.PropertyAuthoriser.PropertyPermission;
import io.milton.resource.AccessControlledResource;
import io.milton.resource.AccessControlledResource.Priviledge;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A read/write source of properties which uses getter/setter style properties
 * on the resource class.
 *
 * This is similar in concept to form variable binding in web frameworks like
 * struts and MVC.
 *
 * @author brad
 */
public class BeanPropertySource implements PropertySource {

	private static final Logger log = LoggerFactory.getLogger(BeanPropertySource.class);
	private static final Object[] NOARGS = new Object[0];

	
	
	public BeanPropertySource() {
	}

	
	
	@Override
	public Object getProperty(QName name, Resource r) throws NotAuthorizedException {
		PropertyDescriptor pd = getPropertyDescriptor(r, name.getLocalPart());
		if (pd == null) {
			throw new IllegalArgumentException("no prop: " + name.getLocalPart() + " on " + r.getClass());
		}
		try {
			return pd.getReadMethod().invoke(r, NOARGS);
		} catch (Exception ex) {
			if (ex.getCause() instanceof NotAuthorizedException) {
				NotAuthorizedException na = (NotAuthorizedException) ex.getCause();
				throw na;
			} else {
				throw new RuntimeException(name.toString(), ex);
			}
		}
	}

	@Override
	public void setProperty(QName name, Object value, Resource r) throws NotAuthorizedException, PropertySetException {
		log.debug("setProperty: " + name + " = " + value);
		PropertyDescriptor pd = getPropertyDescriptor(r, name.getLocalPart());
		try {
			pd.getWriteMethod().invoke(r, value);
		} catch (PropertySetException e) {
			throw e;
		} catch (Exception ex) {
			if (ex.getCause() instanceof NotAuthorizedException) {
				NotAuthorizedException na = (NotAuthorizedException) ex.getCause();
				throw na;
			} else if (ex.getCause() instanceof PropertySetException) {
				PropertySetException na = (PropertySetException) ex.getCause();
				throw na;
			} else {
				if (value == null) {
					log.error("Exception setting property: " + name.toString() + " to null");
				} else {
					log.error("Exception setting property: " + name.toString() + " to value: " + value + " class:" + value.getClass());
				}
				throw new RuntimeException(name.toString(), ex);
			}
		}
	}

	@Override
	public PropertyMetaData getPropertyMetaData(QName name, Resource r) {
		log.debug("getPropertyMetaData");
		BeanPropertyResource anno = getAnnotation(r);
		if (anno == null) {
			log.debug(" no annotation: ", r.getClass().getCanonicalName());
			return PropertyMetaData.UNKNOWN;
		}
		if (!name.getNamespaceURI().equals(anno.value())) {
			log.debug("different namespace", anno.value(), name.getNamespaceURI());
			return PropertyMetaData.UNKNOWN;
		}

		PropertyDescriptor pd = getPropertyDescriptor(r, name.getLocalPart());
		if (pd == null || pd.getReadMethod() == null) {
			LogUtils.debug(log, "getPropertyMetaData: no read method:", name.getLocalPart(), r.getClass());
			return PropertyMetaData.UNKNOWN;
		} else {
			BeanPropertyAccess propAnno = pd.getReadMethod().getAnnotation(BeanPropertyAccess.class);
			if (propAnno != null) {
				if (!propAnno.value()) {
					log.trace("getPropertyMetaData: property is annotated and value is false, so do not allow access");
					return PropertyMetaData.UNKNOWN;
				} else {
					log.trace("getPropertyMetaData: property is annotated and value is true, so allow access");
				}
			} else {
				if (anno.enableByDefault()) {
					log.trace("getPropertyMetaData: no property annotation, property annotation is enable by default so allow access");
				} else {
					log.trace("getPropertyMetaData:no property annotation, class annotation says disable by default, decline access");
					return PropertyMetaData.UNKNOWN;
				}
			}
			if (log.isDebugEnabled()) {
				log.debug("writable: " + anno.writable() + " - " + (pd.getWriteMethod() != null));
			}
			boolean writable = anno.writable() && (pd.getWriteMethod() != null);
			if (writable) {
				return new PropertyMetaData(PropertyAccessibility.WRITABLE, pd.getPropertyType());
			} else {
				return new PropertyMetaData(PropertyAccessibility.READ_ONLY, pd.getPropertyType());
			}
		}
	}

	@Override
	public void clearProperty(QName name, Resource r) throws NotAuthorizedException {
		setProperty(name, null, r);
	}

	@Override
	public List<QName> getAllPropertyNames(Resource r) {
		BeanPropertyResource anno = getAnnotation(r);
		if (anno == null) {
			return null;
		}
		PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(r);
		List<QName> list = new ArrayList<QName>();
		for (PropertyDescriptor pd : pds) {
			if (pd.getReadMethod() != null) {
				list.add(new QName(anno.value(), pd.getName()));
			}
		}
		return list;
	}

	

	public BeanPropertyResource getAnnotation(Resource r) {
		return r.getClass().getAnnotation(BeanPropertyResource.class);
	}

	public PropertyDescriptor getPropertyDescriptor(Resource r, String name) {
		try {
			PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(r, name);
			return pd;
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		} catch (InvocationTargetException ex) {
			throw new RuntimeException(ex);
		} catch (NoSuchMethodException ex) {
			return null;
		}
	}
}
