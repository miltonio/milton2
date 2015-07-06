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

import io.milton.http.annotated.scheduling.SchedulingInboxResource;
import io.milton.http.annotated.scheduling.SchedulingOutboxResource;
import io.milton.http.caldav.CalendarSearchService;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.Resource;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;

/**
 * Just adds schedling inbox and outbox to the collection
 *
 * @author brad
 */
public class AnnoCalendarHomeResource extends AnnoCollectionResource {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(AnnoCalendarHomeResource.class);

	private final AnnoPrincipalResource principal;
	private final CalendarSearchService calendarSearchService;
	private SchedulingInboxResource inboxResource;
	private SchedulingOutboxResource outboxResource;

	public AnnoCalendarHomeResource(final AnnotationResourceFactory outer, Object source, AnnoPrincipalResource parent, CalendarSearchService calendarSearchService) {
		super(outer, source, parent);
		this.principal = parent;
		this.calendarSearchService = calendarSearchService;
	}


	@Override
	public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
		return getChildren(false);
	}
	
	@Override
	public List<? extends Resource> getChildren(boolean isChildLoopup) throws NotAuthorizedException, BadRequestException {
		ResourceList annoResources = findChildren(isChildLoopup);
		List<Resource> list = new ArrayList<Resource>(annoResources);
		list.add(inboxResource);
		list.add(outboxResource);
		return list;
	}	

	@Override
	protected void initChildren(boolean isChildLookup) throws NotAuthorizedException, BadRequestException {
		super.initChildren(isChildLookup);
		inboxResource = new SchedulingInboxResource(principal, calendarSearchService, calendarSearchService.getSchedulingInboxColName());
		outboxResource = new SchedulingOutboxResource(principal, calendarSearchService, calendarSearchService.getSchedulingOutboxColName());
	}

	
	
	
}
