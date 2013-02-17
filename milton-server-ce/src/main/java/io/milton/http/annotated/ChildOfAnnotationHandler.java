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

import io.milton.annotations.ChildOf;
import io.milton.http.Request.Method;
import java.util.List;

/**
 *
 * @author brad
 */
public class ChildOfAnnotationHandler extends AbstractAnnotationHandler {

	public static final String NOT_ATTEMPTED = "NotAttempted";
		
	private final AnnotationResourceFactory annoFactory;

	public ChildOfAnnotationHandler(final AnnotationResourceFactory outer) {
		super(ChildOf.class, Method.PROPFIND);
		this.annoFactory = outer;
	}

	/**
	 * Will return one of:
	 *	- ChildOfAnnotationHandler.NOT_ATTEMPTED if no appropriate method was found
	 *	- null, if a method was available but no resource was found
	 *	- or, the child object with the given name wrapped in an AnnoResource
	 * 
	 * @param source
	 * @return - a tri-value indicating the object which was found, no object was found, or search was not attempted
	 */
	public Object execute(AnnoCollectionResource parent) {
		Object source = parent.getSource();
		List<ControllerMethod> availMethods = getMethods(source.getClass());
		if( availMethods.isEmpty()) {
			return NOT_ATTEMPTED;
		}
		for (ControllerMethod cm : availMethods) {
			try {
				Object o = cm.method.invoke(cm.controller, source);
				if( o == null ) {
					// ignore
				} else {
					return annoFactory.instantiate(o, parent, cm.method);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}
		return null;
	}
}
