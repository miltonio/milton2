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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	public Set execute(Object source) {
		Set result = new HashSet();
		for (ControllerMethod cm : getMethods(source.getClass())) {
			try {
				Object o = cm.method.invoke(cm.controller, source);
				if( o == null ) {
					// ignore
				} else if( o instanceof Collection ) {
					Collection l = (Collection)o;
					result.addAll(l);
				} else if( o.getClass().isArray()) {
					Object[] arr = (Object[]) o;
					for( Object item : arr) {
						result.add(item);
					}
				} else {
					result.add(o);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}
		return result;
	}
}
