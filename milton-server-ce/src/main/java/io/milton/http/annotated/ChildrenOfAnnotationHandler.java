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

import io.milton.annotations.ChildrenOf;
import io.milton.http.Request.Method;
import java.util.List;

/**
 *
 * @author brad
 */
public class ChildrenOfAnnotationHandler extends AbstractAnnotationHandler {
	private final AnnotationResourceFactory outer;

	public ChildrenOfAnnotationHandler(final AnnotationResourceFactory outer) {
		super(ChildrenOf.class, Method.PROPFIND);
		this.outer = outer;
	}

	public List execute(Object source) {
		ControllerMethod cm = getMethod(source.getClass());
		if (cm == null) {
			throw new RuntimeException("Method not found: " + getClass() + " - " + source.getClass());
		}
		try {
			return (List) cm.method.invoke(cm.controller, source); // TODO: other args like request, response, etc
			// TODO: other args like request, response, etc
			// TODO: other args like request, response, etc
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
    
}
