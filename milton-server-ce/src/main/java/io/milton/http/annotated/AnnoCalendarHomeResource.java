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

import io.milton.http.annotated.scheduling.SchedulingInboxResource;
import io.milton.http.annotated.scheduling.SchedulingOutboxResource;
import io.milton.http.caldav.CalendarSearchService;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.Resource;
import java.util.ArrayList;
import java.util.List;

import io.milton.resource.SchedulingHomeResource;
import org.slf4j.LoggerFactory;

/**
 * Just adds schedling inbox and outbox to the collection
 *
 * @author brad
 */
public class AnnoCalendarHomeResource extends AnnoCollectionResource implements SchedulingHomeResource {

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

    @Override
    public String getInboxName() {
        return calendarSearchService.getSchedulingInboxColName();
    }

    @Override
    public String getOutboxName() {
        return calendarSearchService.getSchedulingOutboxColName();
    }
}
