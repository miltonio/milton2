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

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.principal.CalDavPrincipal;
import io.milton.resource.CalendarResource;
import io.milton.resource.ICalResource;
import io.milton.resource.SchedulingResponseItem;
import java.util.Date;
import java.util.List;

/**
 * Allows searching for calendar items by date range
 * 
 * A default implementation will be used in CaldavProtocol, but you can implement
 * your own to optimise for SQL searching, etc
 *
 * @author brad
 */
public interface CalendarSearchService {
    
    /**
     * Query the free busy status of the given principal
     * 
     * http://tools.ietf.org/html/rfc6638#section-2.1
     * 
     * @param principal
     * @param iCalText
     * @return 
     */
    List<SchedulingResponseItem> queryFreeBusy(CalDavPrincipal principal, String iCalText);    
    
    List<ICalResource> findCalendarResources(CalendarResource calendar, Date start, Date finish) throws NotAuthorizedException, BadRequestException;
    
    List<ICalResource> findAttendeeResources(CalDavPrincipal attendee) throws NotAuthorizedException, BadRequestException;
}
