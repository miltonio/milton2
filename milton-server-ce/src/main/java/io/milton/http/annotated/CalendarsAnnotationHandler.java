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

import io.milton.annotations.Calendars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attempt to locate an Access Control List of the given resource for the
 * current user
 *
 * @author brad
 */
public class CalendarsAnnotationHandler extends AbstractAnnotationHandler {

	private static final Logger log = LoggerFactory.getLogger(CalendarsAnnotationHandler.class);

	public CalendarsAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, Calendars.class);
	}

	/**
	 * Check if the source object is a source parameter for methods with the 
	 * @Calendars annotation
	 * 
	 * @param source
	 * @return 
	 */
	public boolean hasCalendars(Object source) {
		for( ControllerMethod cm : getMethods(source.getClass())) {
			Calendars c = cm.method.getAnnotation(Calendars.class);
			if( c != null ) {
				return true;
			}
		}
		return false;
	}

}
