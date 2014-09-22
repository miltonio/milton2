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

import io.milton.annotations.MakeCollection;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class MakeCollectionAnnotationHandler extends AbstractAnnotationHandler {
	
	private static final Logger log = LoggerFactory.getLogger(MakeCollectionAnnotationHandler.class);

	public MakeCollectionAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, MakeCollection.class, Method.MKCOL);
	}

	public Object execute(AnnoResource res, String newName) throws NotAuthorizedException, ConflictException, BadRequestException {
		log.trace("execute MKCOL method");
		Object source = res.getSource();
		ControllerMethod cm = getBestMethod(source.getClass());
		if (cm == null) {
			throw new RuntimeException("Method not found: " + getClass() + " - " + source.getClass());
		}
		try {
			Object[] args = annoResourceFactory.buildInvokeArgs(res, cm.method, newName);
			Object o = cm.method.invoke(cm.controller, args);
			if( o == null ) {
				throw new RuntimeException("Method returned null object or void: " + cm.controller.getClass() + "::" + cm.method.getName() + " - should return newly created object");
			}
			return o;
		} catch(NotAuthorizedException e) {
			throw e;			
		} catch(BadRequestException e) {
			throw e;			
		} catch(ConflictException e) {
			throw e;				
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
    
}
