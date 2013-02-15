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

import io.milton.http.Request.Method;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.PropertyUtils;

/**
 *
 * @author brad
 */
public class UniqueIdAnnotationHandler extends AbstractAnnotationHandler {

	private final String[] ID_PROP_NAMES = {"id", "uniqueId", "code"};
	private final AnnotationResourceFactory outer;

	public UniqueIdAnnotationHandler(Class annoClass, final AnnotationResourceFactory outer) {
		super(annoClass);
		this.outer = outer;
	}

	public String execute(Object source) {
		try {
			Object rawId = null;
			ControllerMethod cm = getBestMethod(source.getClass());
			if (cm == null) {
				// look for an annotation on the source itself
				java.lang.reflect.Method m = outer.findMethodForAnno(source.getClass(), annoClass);
				if (m != null) {
					rawId = m.invoke(source, (Object) null);
				} else {
					for (String nameProp : ID_PROP_NAMES) {
						if (PropertyUtils.isReadable(source, "name")) {
							Object oPropVal = PropertyUtils.getProperty(source, nameProp);
							if (oPropVal != null) {
								rawId = oPropVal;
								break;
							}
						}
					}					
				}
			} else {
				rawId = cm.method.invoke(cm.controller, source);
			}
			if (rawId != null) {
				return rawId.toString();
			} else {
				return null;
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Exception executing " + getClass() + " - " + source.getClass());
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Exception executing " + getClass() + " - " + source.getClass());
		} catch(NoSuchMethodException e) {
			throw new RuntimeException("Exception executing " + getClass() + " - " + source.getClass());
		}
	}
}
