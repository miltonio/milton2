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

/**
 *
 * @author brad
 */
public class CommonPropertyAnnotationHandler<T> extends AbstractAnnotationHandler {

	private T defaultValue;
	protected final String[] propertyNames;

	public CommonPropertyAnnotationHandler(Class annoClass, final AnnotationResourceFactory outer) {
		super(outer, annoClass);
		propertyNames = new String[0];
	}

	public CommonPropertyAnnotationHandler(Class annoClass, final AnnotationResourceFactory outer, String ... propNames) {
		super(outer, annoClass);
		propertyNames = propNames;
	}

	public T get(Object source) {
		try {
			ControllerMethod cm = getBestMethod(source.getClass(), null, null, Object.class);
			if (cm == null) {
				// look for an annotation on the source itself
				java.lang.reflect.Method m = outer.findMethodForAnno(source.getClass(), annoClass);
				if (m != null) {
					T val = (T) m.invoke(source, (Object) null);
					return val;
				}
				for( String propName : propertyNames) {
					Object s = attemptToReadProperty(source, propName);
					if (s != null) {
						return (T)s;
					}					
				}
				return defaultValue;
			}
			T val = (T) invoke(cm, source);
			return val;
		} catch (Exception e) {
			throw new RuntimeException("Exception executing " + annoClass + " - " + source.getClass(), e);
		}
	}

	public void set(Object source, T newValue) {
		try {
			ControllerMethod cm = getBestMethod(source.getClass(), null, null, Void.TYPE);
			if (cm == null) {
				// look for an annotation on the source itself
				java.lang.reflect.Method m = outer.findMethodForAnno(source.getClass(), annoClass);
				if (m != null) {
					m.invoke(source, (Object) null);
					return ;
				}
				// look for a bean property
				for( String propName : propertyNames) {
					if( attemptToSetProperty(source, propName) ) {
						return ;
					}
				}
			} else {
				invoke(cm, source, newValue);
			}
		} catch (Exception e) {
			throw new RuntimeException("Exception executing " + annoClass + " - " + source.getClass(), e);
		}
		
	}
	
	public T getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(T defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	protected T deriveDefaultValue(Object source) {
		return getDefaultValue();
	}
}
