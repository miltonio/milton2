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

import io.milton.annotations.DisplayName;

/**
 *
 * @author brad
 */
public class DisplayNameAnnotationHandler extends AbstractAnnotationHandler {

	public DisplayNameAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, DisplayName.class);
	}

	public String execute(AnnoResource res) {
		Object source = res.getSource();
		try {
			ControllerMethod cm = getBestMethod(source.getClass());
			if (cm == null) {
				Object o = attemptToReadProperty(source, "displayName", "title");
				if( o != null ) {
					return o.toString();
				}
				return res.getName();
			}

			return (String) cm.method.invoke(cm.controller, source);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
