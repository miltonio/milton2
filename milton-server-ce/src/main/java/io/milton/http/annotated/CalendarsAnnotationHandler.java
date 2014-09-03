/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.milton.http.annotated;

import io.milton.annotations.Calendars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
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
