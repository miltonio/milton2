/*
 * Copyright 2013 McEvoy Software Ltd.
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
package io.milton.http.caldav;

import io.milton.ent.config.HttpManagerBuilderEnt;
import io.milton.http.annotated.AnnoCalendarResource;
import io.milton.http.annotated.AnnoPrincipalResource;
import io.milton.http.annotated.AnnotationResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.principal.CalDavPrincipal;
import io.milton.resource.CalendarResource;
import io.milton.resource.ICalResource;
import io.milton.resource.SchedulingResponseItem;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class AnnotationsCalendarSearchService implements CalendarSearchService {

    private static final Logger log = LoggerFactory.getLogger(AnnotationsCalendarSearchService.class);
    
    private final CalendarSearchService wrapped;
    private AnnotationResourceFactory annotationResourceFactory;

    public AnnotationsCalendarSearchService(CalendarSearchService wrapped) {
        this.wrapped = wrapped;
    }

    public void setAnnotationResourceFactory(AnnotationResourceFactory annotationResourceFactory) {
        this.annotationResourceFactory = annotationResourceFactory;
    }

    public AnnotationResourceFactory getAnnotationResourceFactory() {
        return annotationResourceFactory;
    }

    @Override
    public List<ICalResource> findCalendarResources(CalendarResource calendar, Date start, Date finish) throws NotAuthorizedException, BadRequestException {
        List<ICalResource> results = null;
        if (calendar instanceof AnnoCalendarResource) {
            results = annotationResourceFactory.getCalendarDateRangeQueryAnnotationHandler().execute((AnnoCalendarResource) calendar, start, finish);            
            if( results == null ) {
                log.trace("Got null results from annotations calendar date range query, so will fallback to iterative query: " + wrapped.getClass());
            }
        }
        if( results == null ) {
            results = wrapped.findCalendarResources(calendar, start, finish);
        }
        return results;
    }
    

    @Override
    public List<SchedulingResponseItem> queryFreeBusy(CalDavPrincipal principal, String iCalText) {
        if (principal instanceof AnnoPrincipalResource) {
            AnnoPrincipalResource p = (AnnoPrincipalResource) principal;
            List<SchedulingResponseItem> list = annotationResourceFactory.getFreeBusyQueryAnnotationHandler().execute(p, iCalText);
            if( list == null ) {
                log.warn("Got null response from getFreeBusyQueryAnnotationHandler");
                list = Collections.EMPTY_LIST;                
            }
            return list;
        } else {
            return wrapped.queryFreeBusy(principal, iCalText);
        }
    }

    @Override
    public List<ICalResource> findAttendeeResources(CalDavPrincipal principal) throws NotAuthorizedException, BadRequestException {
        if (principal instanceof AnnoPrincipalResource) {
            AnnoPrincipalResource p = (AnnoPrincipalResource) principal;
            return annotationResourceFactory.getCalendarInvitationsAnnotationHandler().getCalendarInvitations(p);
        } else {
            return wrapped.findAttendeeResources(principal);
        }
    }

    @Override
    public String findAttendeeResourcesCTag(CalDavPrincipal principal) throws NotAuthorizedException, BadRequestException {
        if (principal instanceof AnnoPrincipalResource) {
            AnnoPrincipalResource p = (AnnoPrincipalResource) principal;
            return annotationResourceFactory.getCalendarInvitationsCTagAnnotationHandler().getCalendarInvitationsCtag(p);
        } else {
            return wrapped.findAttendeeResourcesCTag(principal);
        }
    }

    @Override
    public String getSchedulingColName() {
        return wrapped.getSchedulingColName();
    }

    @Override
    public String getSchedulingInboxColName() {
        return wrapped.getSchedulingInboxColName();
    }

    @Override
    public String getSchedulingOutboxColName() {
        return wrapped.getSchedulingOutboxColName();
    }

    @Override
    public boolean isSchedulingEnabled() {
        return wrapped.isSchedulingEnabled();
    }
}
