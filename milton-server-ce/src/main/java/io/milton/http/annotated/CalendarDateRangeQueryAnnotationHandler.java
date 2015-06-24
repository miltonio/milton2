/*
 *
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.milton.http.annotated;

import io.milton.annotations.CalendarDateRangeQuery;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
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

	public List<ICalResource> execute(AnnoCalendarResource parent, Date start, Date finish) throws NotAuthorizedException, BadRequestException{
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
		} catch (NotAuthorizedException e) {
			throw e;
		} catch (BadRequestException e) {
			throw e;	
		} catch (Exception e) {
			throw new RuntimeException("Exception executing " + getClass() + " - " + source.getClass(), e);
		}
	}
}
