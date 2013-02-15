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
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public abstract class AbstractAnnotationHandler implements AnnotationHandler {
	
	private static final Logger log = LoggerFactory.getLogger(AbstractAnnotationHandler.class);
	
	protected final Class annoClass;
	protected final Method[] methods;
	/**
	 * Map of methods for this annotation, keyed on the class of the source
	 */
	List<ControllerMethod> controllerMethods = new ArrayList<ControllerMethod>();

	public AbstractAnnotationHandler(Class annoClass, Method... methods) {
		this.annoClass = annoClass;
		this.methods = methods;
	}

	@Override
	public void parseController(Object controller) {
		log.info("parseController: " + controller + " handler: " + getClass());
		for (java.lang.reflect.Method m : controller.getClass().getMethods()) {
			Annotation a = m.getAnnotation(annoClass);
			if (a != null) {
				log.info(" found method: " + m.getName());
				Class<?>[] params = m.getParameterTypes();
				if (params == null || params.length == 0) {
					throw new RuntimeException("Invalid controller method: " + m.getName() + " does not have a source argument");
				}
				Class sourceType = params[0];
				ControllerMethod cm = new ControllerMethod(controller, m, sourceType);
				controllerMethods.add( cm);
			}
		}
	}

	ControllerMethod getBestMethod(Class sourceClass) {
		ControllerMethod foundMethod = null;
		for (ControllerMethod cm : controllerMethods ) {
			if (cm.sourceType.isAssignableFrom(sourceClass)) {
				if (foundMethod == null) {
					foundMethod = cm;
				} else if (foundMethod.sourceType.isAssignableFrom(cm.sourceType)) {
					foundMethod = cm; // this key is more specific then the last one found
					// this key is more specific then the last one found
				}
			}
		}
		return foundMethod;
	}

	List<ControllerMethod> getMethods(Class sourceClass) {
		List<ControllerMethod> foundMethods = new ArrayList<ControllerMethod>();
		for (ControllerMethod cm : controllerMethods ) {
			Class key = cm.sourceType;
			if (key.isAssignableFrom(sourceClass)) {
				foundMethods.add(cm);
			}
		}
		return foundMethods;
	}
	
	@Override
	public Method[] getSupportedMethods() {
		return methods;
	}

	@Override
	public boolean isCompatible(Object source) {
		ControllerMethod m = getBestMethod(source.getClass());
		return m != null;
	}
    
}
