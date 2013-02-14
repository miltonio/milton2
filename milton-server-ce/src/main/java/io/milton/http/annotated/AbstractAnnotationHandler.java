/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.milton.http.annotated;

import io.milton.http.Request.Method;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author brad
 */
public abstract class AbstractAnnotationHandler implements AnnotationHandler {
	protected final Class annoClass;
	protected final Method[] methods;
	/**
	 * Map of methods for this annotation, keyed on the class of the source
	 */
	Map<Class, ControllerMethod> mapOfMethods = new HashMap<Class, ControllerMethod>();

	public AbstractAnnotationHandler(Class annoClass, Method... methods) {
		this.annoClass = annoClass;
		this.methods = methods;
	}

	@Override
	public void parseController(Object controller) {
		for (java.lang.reflect.Method m : controller.getClass().getMethods()) {
			Annotation a = m.getAnnotation(annoClass);
			if (a != null) {
				Class<?>[] params = m.getParameterTypes();
				if (params == null || params.length == 0) {
					throw new RuntimeException("Invalid controller method: " + m.getName() + " does not have a source argument");
				}
				Class sourceType = params[0];
				ControllerMethod cm = new ControllerMethod(controller, m);
				mapOfMethods.put(sourceType, cm);
			}
		}
	}

	ControllerMethod getMethod(Class sourceClass) {
		Class foundKey = null;
		for (Class key : mapOfMethods.keySet()) {
			if (key.isAssignableFrom(sourceClass)) {
				if (foundKey == null) {
					foundKey = key;
				} else if (foundKey.isAssignableFrom(key)) {
					foundKey = key; // this key is more specific then the last one found
					// this key is more specific then the last one found
				}
			}
		}
		if (foundKey != null) {
			ControllerMethod cm = mapOfMethods.get(foundKey);
			return cm;
		}
		return null;
	}

	@Override
	public Method[] getSupportedMethods() {
		return methods;
	}

	@Override
	public boolean isCompatible(Object source) {
		ControllerMethod m = getMethod(source.getClass());
		return m != null;
	}
    
}
