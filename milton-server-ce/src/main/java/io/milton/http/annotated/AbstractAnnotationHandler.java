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

import io.milton.annotations.Get;
import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public abstract class AbstractAnnotationHandler implements AnnotationHandler {

	private static final Logger log = LoggerFactory.getLogger(AbstractAnnotationHandler.class);
	protected final AnnotationResourceFactory outer;
	protected final Class annoClass;
	protected final Method[] methods;
	/**
	 * Map of methods for this annotation, keyed on the class of the source
	 */
	List<ControllerMethod> controllerMethods = new ArrayList<ControllerMethod>();

	public AbstractAnnotationHandler(AnnotationResourceFactory outer, Class annoClass, Method... methods) {
		this.outer = outer;
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
				ControllerMethod cm = new ControllerMethod(controller, m, sourceType, a);
				controllerMethods.add(cm);
			}
		}
	}

	ControllerMethod getBestMethod(Class sourceClass) {
		return getBestMethod(sourceClass, null);
	}

	ControllerMethod getBestMethod(Class sourceClass, String contentType) {
		return getBestMethod(sourceClass, contentType, null, null);
	}

	ControllerMethod getBestMethod(Class sourceClass, String contentType, Map<String, String> params, Class returnType) {
		ControllerMethod foundMethod = null;
		int foundMethodScore = -1;
		for (ControllerMethod cm : controllerMethods) {
			if (cm.sourceType.isAssignableFrom(sourceClass)) {
				if (isReturnTypeMatch(cm.method, returnType)) {
					int score = 0;
					int i = contentTypeMatch(contentType, cm.anno);
					if (i >= 0) {
						score += i;
						i = isParamMatch(params, cm.anno);
						if (i >= 0) {
							score += i;
							if (score > foundMethodScore) {
								foundMethod = cm;
								foundMethodScore = score;
							}
						}
					}
				}
			}
		}
		return foundMethod;
	}

	/**
	 * Locate a ControllerMethod which can create an object of the given type
	 * (may be null) in the given parent
	 *
	 * @param type - final segment of the class name to be created, or null. Eg
	 * to create com.mycompany.Customer use "Customer"
	 * @return null, if none found, otherwise a method which can create the
	 * given resource
	 */
	public ControllerMethod getMethodForType(AnnoCollectionResource parent, String type) {
		List<ControllerMethod> foundMethods = getMethods(parent.getSource().getClass(), type);
		if (foundMethods.isEmpty()) {
			return null;
		} else {
			return foundMethods.get(0);
		}
	}

	List<ControllerMethod> getMethods(Class sourceClass) {
		List<ControllerMethod> foundMethods = new ArrayList<ControllerMethod>();
		for (ControllerMethod cm : controllerMethods) {
			Class key = cm.sourceType;
			if (key.isAssignableFrom(sourceClass)) {
				foundMethods.add(cm);
			}
		}
		return foundMethods;
	}

	/**
	 *
	 * @param sourceClassName - may be null, otherwise final segment of class
	 * name
	 * @return
	 */
	List<ControllerMethod> getMethods(Class sourceClass, String sourceClassName) {
		List<ControllerMethod> foundMethods = new ArrayList<ControllerMethod>();
		for (ControllerMethod cm : controllerMethods) {
			Class key = cm.sourceType;
			if (key.isAssignableFrom(sourceClass)) {
				if (sourceClassName == null || cm.method.getReturnType().getCanonicalName().endsWith(sourceClassName)) {
					foundMethods.add(cm);
				}
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
		String contentType = null;
		Map<String, String> params = null;
		Request req = HttpManager.request();
		if (req != null) {
			contentType = req.getContentTypeHeader();
			params = req.getParams();
		}
		ControllerMethod m = getBestMethod(source.getClass(), contentType, params, null);
		return m != null;
	}

	protected Object attemptToReadProperty(Object source, String... propNames) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		for (String propName : propNames) {
			if (PropertyUtils.isReadable(source, propName)) {
				// found a readable property, so return it				
				Object oName = PropertyUtils.getProperty(source, propName);
				return oName;
			}
		}
		return null;
	}

	/**
	 * Returns true if it was able to set the property
	 * 
	 * @param source
	 * @param propNames
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException 
	 */
	protected boolean attemptToSetProperty(Object source, Object value, String... propNames) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		for (String propName : propNames) {
			if (PropertyUtils.isWriteable(source, propName)) {
				PropertyUtils.setProperty(source, propName, value);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Check if the annotation has a contentType specified. If so it must match
	 * that given
	 *
	 * @param reqContentType
	 * @param anno
	 * @return - negative means does not match. Otherwise is the score of the
	 * match, where more is better
	 */
	private int contentTypeMatch(String reqContentType, Annotation anno) {
		if (anno instanceof Get) {
			Get g = (Get) anno;
			if (g.contentType() != null && g.contentType().length() > 0) {
				System.out.println(g.contentType() + " == " + reqContentType);
				if (g.contentType().equals(reqContentType)) {
					return 1;
				} else {
					return -1;
				}
			}
		}
		return 0;
	}

	private int isParamMatch(Map<String, String> params, Annotation anno) {
		if (anno instanceof Get) {
			Get g = (Get) anno;
			if (g.params().length > 0) {
				for (String paramName : g.params()) {
					if (params == null || !params.containsKey(paramName)) {
						System.out.println("params: " + params);
						//System.out.println("param not tound: " + paramName + " in map: " + params.size());
						return -1; // does not match
					}
				}
				return g.params().length;
			} else {
				return 0;
			}
		}
		return 0;
	}

	protected Object invoke(ControllerMethod cm, AnnoResource sourceRes, Object... values) throws Exception {
		try {
			Object[] args;
			if( values == null || values.length == 0) {
				args = outer.buildInvokeArgs(sourceRes, cm.method);
			} else {
				args = outer.buildInvokeArgs(sourceRes, cm.method, values);
			}
			return cm.method.invoke(cm.controller, args);
		} catch (Exception e) {
			throw new Exception("Method: " + cm, e);
		}

	}

	private boolean isReturnTypeMatch(java.lang.reflect.Method method, Class returnType) {
		if( returnType == null ) {
			return true;
		} else {
			return returnType.isAssignableFrom(method.getReturnType());
		}
	}
}
