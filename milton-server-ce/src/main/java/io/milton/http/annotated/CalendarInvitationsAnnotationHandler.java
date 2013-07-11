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

import io.milton.annotations.CalendarInvitations;
import io.milton.resource.ICalResource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Find all calendar objects (ie events) which represent an invitation for the
 * given user.
 *
 * @author brad
 */
public class CalendarInvitationsAnnotationHandler extends AbstractAnnotationHandler {

	private static final Logger log = LoggerFactory.getLogger(CalendarInvitationsAnnotationHandler.class);

	public CalendarInvitationsAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, CalendarInvitations.class);
	}

	public List<ICalResource> getCalendarInvitations(AnnoPrincipalResource parent) {
		List<ICalResource> invitations = new ArrayList<ICalResource>();
		Object source = parent.getSource();
		for (ControllerMethod cm : getMethods(source.getClass())) {
			try {
				Object o = invoke(cm, parent);
				if (o == null) {
					// ignore
				} else if (o instanceof Collection) {
					Collection l = (Collection) o;
					for (Object childSource : l) {
						createAndAdd(invitations, childSource, parent);
					}
				} else if (o.getClass().isArray()) {
					Object[] arr = (Object[]) o;
					for (Object childSource : arr) {
						createAndAdd(invitations, childSource, parent);
					}
				} else {
					createAndAdd(invitations, o, parent);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}
		return invitations;
	}
	
	private void createAndAdd(List<ICalResource> invitations, Object childSource,AnnoPrincipalResource parent ) {
		AnnoEventResource e = new AnnoEventResource(annoResourceFactory, childSource, parent);
		invitations.add(e);
	}
}
