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
import java.util.Set;

/**
 *
 * @author brad
 */
public class ChildrenOfAnnotationHandler extends AbstractAnnotationHandler {

	public ChildrenOfAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, ChildrenOf.class, Method.PROPFIND);
	}

	public Set<AnnoResource> execute(AnnoCollectionResource parent) {
		Object source = parent.getSource();
		Set<AnnoResource> result = new HashSet<AnnoResource>();
		for (ControllerMethod cm : getMethods(source.getClass())) {
			try {
				Object o = invoke(cm, parent);
				if( o == null ) {
					// ignore
				} else if( o instanceof Collection ) {
					Collection l = (Collection)o;
					for( Object item : l) {
						result.add(annoResourceFactory.instantiate(item, parent, cm.method));
					}
				} else if( o.getClass().isArray()) {
					Object[] arr = (Object[]) o;
					for( Object item : arr) {
						result.add(annoResourceFactory.instantiate(item, parent, cm.method));
					}
				} else {
					result.add(annoResourceFactory.instantiate(o, parent, cm.method));
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}
		return result;
	}
}
