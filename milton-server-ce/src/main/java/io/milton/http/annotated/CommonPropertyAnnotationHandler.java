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

import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author brad
 */
public class CommonPropertyAnnotationHandler<T> extends AbstractAnnotationHandler {
	
	private T defaultValue;

	public CommonPropertyAnnotationHandler(Class annoClass, final AnnotationResourceFactory outer) {
		super(outer, annoClass);
	}

	public T execute(Object source) {
		try {
			ControllerMethod cm = getBestMethod(source.getClass());
			if (cm == null) {
				// look for an annotation on the source itself
				java.lang.reflect.Method m = outer.findMethodForAnno(source.getClass(), annoClass);
				if (m != null) {
					return (T) m.invoke(source, (Object) null);
				}
				return defaultValue;
			}
			return (T) cm.method.invoke(cm.controller, source);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Exception executing " + getClass() + " - " + source.getClass());			
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Exception executing " + getClass() + " - " + source.getClass());			
		}
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(T defaultValue) {
		this.defaultValue = defaultValue;
	}
    
	
}
