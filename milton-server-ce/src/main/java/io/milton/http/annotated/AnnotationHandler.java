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
