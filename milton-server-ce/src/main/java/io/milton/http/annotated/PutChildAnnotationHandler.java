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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class PutChildAnnotationHandler extends AbstractAnnotationHandler {

	private static final Logger log = LoggerFactory.getLogger(PutChildAnnotationHandler.class);

	public PutChildAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, PutChild.class, Method.PUT);
	}

	public Object execute(AnnoResource res, String newName, InputStream inputStream, Long length, String contentType) {
		log.trace("execute PUT method");
		Object source = res.getSource();
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
			//Object[] args = outer.buildInvokeArgs(source, cm.method, newName, inputStream, length, contentType);
			//return cm.method.invoke(cm.controller, args); 
			return invoke(cm, res, newName, inputStream, length, contentType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void replace(AnnoFileResource fileRes, InputStream inputStream, Long length) {
		log.trace("execute PUT (replace) method");
		Object source = fileRes.getSource();
		ControllerMethod cm = getBestMethod(source.getClass());
		if (cm == null) {
			// ok, cant replace. Maybe we can delete and PUT?
			String name = fileRes.getName();
			annoResourceFactory.deleteAnnotationHandler.execute(fileRes);
			execute(fileRes.getParent(), name, inputStream, length, null);

		} else {
			try {
				invoke(cm, fileRes, inputStream, length, fileRes);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
