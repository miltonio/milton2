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

import io.milton.annotations.PutChild;
import io.milton.http.Request.Method;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class PutChildAnnotationHandler extends AbstractAnnotationHandler {

	private static final Logger log = LoggerFactory.getLogger(PutChildAnnotationHandler.class);
	private final AnnotationResourceFactory outer;

	public PutChildAnnotationHandler(final AnnotationResourceFactory outer) {
		super(PutChild.class, Method.PUT);
		this.outer = outer;
	}

	public Object execute(Object source, String newName, InputStream inputStream, Long length, String contentType) {
		log.trace("execute PUT method");
		ControllerMethod cm = getBestMethod(source.getClass());
		if (cm == null) {			
			if (controllerMethods.isEmpty()) {
				log.info("Method not found for source: " + source.getClass() + " No methods registered for " + PutChild.class);
			} else {
				log.info("Method not found for source " + source.getClass() + " Listing methods registered for " + PutChild.class + " :");
				for (ControllerMethod cmm : controllerMethods) {
					System.out.println("	- " + cmm);
				}
			}
			throw new RuntimeException("Method not found: " + getClass() + " - " + source.getClass());
		}
		try {
			Object[] args = outer.buildInvokeArgs(source, cm.method, newName, inputStream, length, contentType);
			return cm.method.invoke(cm.controller, args); // returns the newly created source object
			// returns the newly created source object
			// returns the newly created source object
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
