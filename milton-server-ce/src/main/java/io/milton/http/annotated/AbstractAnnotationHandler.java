/*
 *
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.milton.http.annotated;

import io.milton.annotations.Get;
import io.milton.annotations.Post;
import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
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
	protected final AnnotationResourceFactory annoResourceFactory;
	protected final Class annoClass;
	protected final Method[] methods;
	/**
	 * Map of methods for this annotation, keyed on the class of the source
	 */
	List<ControllerMethod> controllerMethods = new ArrayList<ControllerMethod>();

	public AbstractAnnotationHandler(AnnotationResourceFactory outer, Class annoClass, Method... methods) {
		this.annoResourceFactory = outer;
		this.annoClass = annoClass;
		this.methods = methods;
	}

	@Override
	public Class getAnnoClass() {
		return annoClass;
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
							score = score + SpecificityUtils.sourceSpecifityIndex(cm.sourceType, sourceClass);
							if (score > foundMethodScore) {
								foundMethod = cm;
								foundMethodScore = score;
								if(log.isTraceEnabled()) {
									log.trace("Found high score method: " + cm + " with score: " + foundMethod);
								}
							} else {
								if( log.isTraceEnabled() ) {
									log.trace("Not using method: " + cm + " because score:" + score + " is lower then best: " + foundMethodScore);
								}
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
	 * @param parent
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
	 * @param value
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
		String[] matchParams;
		if (anno instanceof Get) {
			Get g = (Get) anno;
			matchParams = g.params();
		} else if (anno instanceof Post) {
			Post p = (Post) anno;
			matchParams = p.params();
		} else {
			matchParams = null;
		}
		if (matchParams != null && matchParams.length > 0) {
			for (String paramName : matchParams) {
				if (params == null || !params.containsKey(paramName)) {
					return -1; // does not match
				}
			}
			return matchParams.length;
		} else {
			return 0;
		}
	}

	protected Object invoke(ControllerMethod cm, AnnoResource sourceRes, Object... values) throws NotAuthorizedException, BadRequestException, NotFoundException, Exception {
		try {
			Object[] args;
			if (values == null || values.length == 0) {
				args = annoResourceFactory.buildInvokeArgs(sourceRes, cm.method);
			} else {
				args = annoResourceFactory.buildInvokeArgs(sourceRes, cm.method, values);
			}
			return cm.method.invoke(cm.controller, args);

		} catch(java.lang.reflect.InvocationTargetException e) {
			Throwable cause = e.getCause();
			if( cause instanceof NotAuthorizedException ) {
				NotAuthorizedException nae = (NotAuthorizedException)cause;
				if( nae.getResource() == null ) {
					throw new NotAuthorizedException(sourceRes, nae); // need exception with resource so we can generate challenge
				}
				throw nae;
			} else if( cause instanceof BadRequestException) {
				throw (BadRequestException)cause;
			} else if( cause instanceof NotFoundException) {
				throw (NotFoundException)cause;
			} else if( cause instanceof ConflictException) {
				throw (ConflictException)cause;				
			}
			throw e;			
		} catch(NotAuthorizedException e) {
			throw e;
		} catch(BadRequestException e) {
			throw e;
		} catch(NotFoundException e) {
			throw e;			
		} catch (Exception e) {
			throw new Exception("Method: " + cm, e);
		}

	}

	private boolean isReturnTypeMatch(java.lang.reflect.Method method, Class returnType) {
		if (returnType == null) {
			return true;
		} else {
			return returnType.isAssignableFrom(method.getReturnType());
		}
	}

	@Override
	public List<ControllerMethod> getControllerMethods() {
		return controllerMethods;
	}
}
