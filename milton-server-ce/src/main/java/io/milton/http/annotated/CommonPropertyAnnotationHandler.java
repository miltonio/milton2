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
package io.milton.http.annotated;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class CommonPropertyAnnotationHandler<T> extends AbstractAnnotationHandler {

	private static final Logger log = LoggerFactory.getLogger(CommonPropertyAnnotationHandler.class);
	
	private T defaultValue;
	protected final String[] propertyNames;

	public CommonPropertyAnnotationHandler(Class annoClass, final AnnotationResourceFactory outer) {
		super(outer, annoClass);
		propertyNames = new String[0];
	}

	public CommonPropertyAnnotationHandler(Class annoClass, final AnnotationResourceFactory outer, String... propNames) {
		super(outer, annoClass);
		propertyNames = propNames;
	}

	public T get(AnnoResource res) {
		Object source = res.getSource();
		log.trace("get.1: source type={}", source.getClass());		
		try {
			ControllerMethod cm = getBestMethod(source.getClass(), null, null, Object.class);
			if (cm != null) {
				log.trace("get.2: found method={}", cm.method.getName());
				return (T) invoke(cm, res);
			} else {
				log.trace("get.3: couldnt find annotated controllere method, look for method on the source object");
				// look for an annotation on the source itself
				java.lang.reflect.Method m = annoResourceFactory.findMethodForAnno(source.getClass(), annoClass);
				if (m != null && m.getParameterTypes().length ==0 ) {
					log.trace("get.4: found method on source={}", m.getName());
					return (T) m.invoke(source);
				}
				for (String propName : propertyNames) {
					Object s = attemptToReadProperty(source, propName);
					if (s != null) {
						log.trace("get.5: found value from source property={}", propName);
						return (T) s;
					}
				}
				log.trace("get.6: couldnt get a value from annotated methods or properties, so look for a default value");
				return deriveDefaultValue(source);
			}
		} catch (Exception e) {
			throw new RuntimeException("Exception executing " + annoClass + " - " + source.getClass(), e);
		}
	}

	public void set(AnnoResource res, T newValue) {
		Object source = res.getSource();
		try {
			ControllerMethod cm = getBestMethod(source.getClass(), null, null, Void.TYPE);
			if (cm == null) {
				// look for an annotation on the source itself
				java.lang.reflect.Method m = annoResourceFactory.findMethodForAnno(source.getClass(), annoClass);
				if (m != null) {
					m.invoke(source, (Object) null);
					return;
				}
				// look for a bean property
				for (String propName : propertyNames) {
					if (attemptToSetProperty(source, propName)) {
						return;
					}
				}
			} else {
				invoke(cm, res, newValue);
			}
		} catch (Exception e) {
			throw new RuntimeException("Exception executing " + annoClass + " - " + source.getClass(), e);
		}

	}

	public T getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(T defaultValue) {
		this.defaultValue = defaultValue;
	}

	protected T deriveDefaultValue(Object source) {
		return getDefaultValue();
	}
}
