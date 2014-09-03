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

import io.milton.annotations.CalendarDateRangeQuery;
import io.milton.resource.ICalResource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class CalendarDateRangeQueryAnnotationHandler extends AbstractAnnotationHandler {

	private static final Logger log = LoggerFactory.getLogger(CalendarDateRangeQueryAnnotationHandler.class);

	public CalendarDateRangeQueryAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, CalendarDateRangeQuery.class);
	}

	public List<ICalResource> execute(AnnoCalendarResource parent, Date start, Date finish) {
		Object source = parent.getSource();
		try {
			ControllerMethod cm = getBestMethod(source.getClass());
			if (cm == null) {
				return null;
			} else {
				List<AnnoResource> result = new ArrayList<AnnoResource>();
				Object[] args = annoResourceFactory.buildInvokeArgs(parent, cm.method, start, finish);
				Object eventSources = invoke(cm, parent, args);
				annoResourceFactory.createAndAppend(result, eventSources, parent, cm);	 
				List<ICalResource> list = new ArrayList<ICalResource>();
				for( AnnoResource r : result ) {
					if( r instanceof ICalResource) {
						list.add((ICalResource) r);
					}
				}
				return list;
			}
			
		} catch (Exception e) {
			throw new RuntimeException("Exception executing " + getClass() + " - " + source.getClass(), e);
		}
	}
}
