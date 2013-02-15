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

import io.milton.annotations.Name;
import org.apache.commons.beanutils.PropertyUtils;

/**
 *
 * @author brad
 */
public class NameAnnotationHandler extends AbstractAnnotationHandler {

	private final AnnotationResourceFactory outer;

	public NameAnnotationHandler(final AnnotationResourceFactory outer) {
		super(Name.class);
		this.outer = outer;
	}

	public String execute(Object source) {
		try {
			ControllerMethod cm = getBestMethod(source.getClass());
			if (cm == null) {
				if (PropertyUtils.isReadable(source, "name")) {
					Object oName = PropertyUtils.getProperty(source, "name");
					if( oName !=null ) {
						if( oName instanceof String) {
							String s = (String) oName;
							return s;
						}
					}
				}
				throw new RuntimeException("Method not found: " + getClass() + " - " + source.getClass());
			}

			return (String) cm.method.invoke(cm.controller, source);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
