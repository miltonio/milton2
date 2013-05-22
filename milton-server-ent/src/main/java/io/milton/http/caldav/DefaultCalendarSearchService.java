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
import io.milton.resource.Resource;
import io.milton.resource.SchedulingResponseItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author brad
 */
public class DefaultCalendarSearchService implements CalendarSearchService{

    private final ICalFormatter formatter = new ICalFormatter();
    
    @Override
    public List<ICalResource> findCalendarResources(CalendarResource calendar, Date start, Date end) throws NotAuthorizedException, BadRequestException {
        // build a list of all calendar resources
        List<ICalResource> list = new ArrayList<ICalResource>();
        for (Resource r : calendar.getChildren()) {
            if (r instanceof ICalResource) {
                ICalResource cr = (ICalResource) r;
                list.add(cr);
            }
        }

        // So now we have (or might have) start and end dates, so filter list
        Iterator<ICalResource> it = list.iterator();
        while (it.hasNext()) {
            ICalResource r = it.next();
            if (outsideDates(r, start, end)) {
                it.remove();
            }
        }
        return list;

    }
    

    private boolean outsideDates(ICalResource r, Date start, Date end) {
        EventResource data;
        if (r instanceof EventResource) {
            data = (EventResource) r;
        } else {
            data = new EventResourceImpl();
            formatter.parseEvent(data, r.getICalData());
        }

        if (start != null) {
            if (data.getStart().before(start)) {
                return true;
            }
        }

        if (end != null) {
            if (data.getEnd().after(end)) {
                return true;
            }
        }

        return false;
    }    

    @Override
    public List<SchedulingResponseItem> queryFreeBusy(CalDavPrincipal principal, String iCalText) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<ICalResource> findAttendeeResources(CalDavPrincipal attendee) throws NotAuthorizedException, BadRequestException {
        return Collections.EMPTY_LIST;
    }
}
