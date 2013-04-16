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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class ChildOfAnnotationHandler extends AbstractAnnotationHandler {

	private static final Logger log = LoggerFactory.getLogger(ChildOfAnnotationHandler.class);
	public static final String NOT_ATTEMPTED = "NotAttempted";

	public ChildOfAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, ChildOf.class);
	}

	/**
	 * Will return one of: - ChildOfAnnotationHandler.NOT_ATTEMPTED if no
	 * appropriate method was found - null, if a method was available but no
	 * resource was found - or, the child object with the given name wrapped in
	 * an AnnoResource
	 *
	 * @param source
	 * @return - a tri-value indicating the object which was found, no object
	 * was found, or search was not attempted
	 */
	public Object execute(AnnoCollectionResource parent, String childName) {
		Object source = parent.getSource();
		try {
			List<ControllerMethod> availMethods = getMethods(source.getClass());
			if (availMethods.isEmpty()) {
				return NOT_ATTEMPTED;
			}
			
			for (ControllerMethod cm : availMethods) {
				if (matchesSuffix(cm, childName)) {
					Object o = invoke(cm, parent, childName);
					if (o == null) {
						// ignore
					} else {
						AnnoResource r = annoResourceFactory.instantiate(o, parent, cm.method);
						r.setNameOverride(childName);
						return r;
					}
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	private boolean matchesSuffix(ControllerMethod cm, String childName) {
		ChildOf a = (ChildOf) cm.anno;
		if( !a.pathSuffix().equals("") ) {
			return childName.endsWith(a.pathSuffix());
		} else {
			return true;
		}
	}
}
