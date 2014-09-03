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

import io.milton.annotations.CalendarInvitationsCTag;
import java.lang.reflect.InvocationTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Find all calendar objects (ie events) which represent an invitation for the
 * given user.
 *
 * @author brad
 */
public class CalendarInvitationsCTagAnnotationHandler extends AbstractAnnotationHandler {

	private static final Logger log = LoggerFactory.getLogger(CalendarInvitationsCTagAnnotationHandler.class);

	public CalendarInvitationsCTagAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, CalendarInvitationsCTag.class);
	}

	public String getCalendarInvitationsCtag(AnnoPrincipalResource parent) {
		Object source = parent.getSource();
		ControllerMethod cm = getBestMethod(source.getClass());
		String ctag = null;
		if( cm != null ) {
			Object rawId;
			try {
				rawId = cm.method.invoke(cm.controller, source);
			} catch (IllegalAccessException ex) {
				throw new RuntimeException(ex);
			} catch (IllegalArgumentException ex) {
				throw new RuntimeException(ex);
			} catch (InvocationTargetException ex) {
				throw new RuntimeException(ex);
			}			
			if( rawId != null ) {
				ctag = rawId.toString();
				if( ctag.length() == 0 ) {
					ctag = null;
				}
			}
		}
		return ctag;
	}

}
