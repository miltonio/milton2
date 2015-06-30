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

import io.milton.http.Request.Method;
import java.util.List;

/**
 * Common interface for handlers which implement the logic for annotations. Includes
 * method handlers and property handlers
 *
 * @author brad
 */
public interface AnnotationHandler {

	/**
	 * Called on initialisation, the handler should look for annotations on the
	 * controller object and prepare itself to use them
	 * 
	 * @param controller 
	 */
	void parseController(Object controller);

	/**
	 * Return any HTTP methods which this annotation handler supports
	 * 
	 * @return 
	 */
	Method[] getSupportedMethods();

	/**
	 * Determine if this handler is able to support the given source object, ie
	 * if there is a controller method registered in this handler that supports
	 * the given source type
	 * 
	 * @param source
	 * @return 
	 */
	boolean isCompatible(Object source);
    
	/**
	 * List the methods found when parsing annotations on the controller
	 * @return 
	 */
	List<ControllerMethod> getControllerMethods();
	
	/**
	 * Get the annotation class that this handler handles
	 * @return 
	 */
	Class getAnnoClass();
}
