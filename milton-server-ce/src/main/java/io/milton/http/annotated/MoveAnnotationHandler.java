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

import io.milton.annotations.Move;
import io.milton.http.Request.Method;
import io.milton.resource.CollectionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class MoveAnnotationHandler extends AbstractAnnotationHandler {
	
	private static final Logger log = LoggerFactory.getLogger(MoveAnnotationHandler.class);
	
	public MoveAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, Move.class, Method.MOVE);
	}

	void execute(AnnoResource res, CollectionResource rDest, String newName) {
		log.trace("execute MOVE method");
		Object source = res.getSource();
		ControllerMethod cm = getBestMethod(source.getClass());
		if (cm == null) {
			throw new RuntimeException("Method not found: " + getClass() + " - " + source.getClass());
		}
		try {
			Object destObject = null;
			if (rDest instanceof AnnoResource) {
				AnnoResource arDest = (AnnoResource) rDest;
				destObject = arDest.getSource();
			}
			Object[] args = outer.buildInvokeArgs(res, cm.method, newName, rDest, destObject);
			cm.method.invoke(cm.controller, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
    
}
