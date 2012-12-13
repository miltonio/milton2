/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.milton.http.caldav;

import io.milton.http.webdav.ResourceTypeHelper;
import io.milton.resource.CalendarResource;
import io.milton.resource.ICalResource;
import io.milton.resource.SchedulingInboxResource;
import io.milton.resource.SchedulingOutboxResource;
import io.milton.resource.CollectionResource;
import io.milton.resource.Resource;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class CalendarResourceTypeHelper implements ResourceTypeHelper {

    private static final Logger log = LoggerFactory.getLogger(CalendarResourceTypeHelper.class);
    private final ResourceTypeHelper wrapped;

    public CalendarResourceTypeHelper(ResourceTypeHelper wrapped) {
        log.debug("CalendarResourceTypeHelper constructed :" + wrapped.getClass().getSimpleName());
        this.wrapped = wrapped;
    }

    @Override
    public List<QName> getResourceTypes(Resource r) {
        if (log.isTraceEnabled()) {
            log.trace("getResourceTypes:" + r.getClass().getCanonicalName());
        }
        // Debugging checks..
        if (r instanceof ICalResource && r instanceof CollectionResource) {
            log.warn("ICal Resource is a collection, this might cause issues with some caldav clients");
        }
        if (r instanceof ICalResource && r instanceof CalendarResource) {
            log.error("EEK!! Resource is both an ical resource (eg an event) and a calendar. Don't implement CalendarResource on events!");
        }

        QName qn;
        List<QName> list = wrapped.getResourceTypes(r);
        if (r instanceof CalendarResource) {
            // http://greenbytes.de/tech/webdav/draft-dusseault-caldav-04.html#new-resources
            log.trace("getResourceTypes: is a calendar");
            qn = new QName(CalDavProtocol.CALDAV_NS, "calendar");
            if (list == null) {
                list = new ArrayList<QName>();
            }
            list.add(qn);
        }
        if (r instanceof SchedulingInboxResource) {
            log.trace("getResourceTypes: is a schedule-inbox");
            qn = new QName(CalDavProtocol.CALDAV_NS, "schedule-inbox");
            if (list == null) {
                list = new ArrayList<QName>();
            }
            list.add(qn);
        }
        if (r instanceof SchedulingOutboxResource) {
            log.trace("getResourceTypes: is a schedule-outbox");
            qn = new QName(CalDavProtocol.CALDAV_NS, "schedule-outbox");
            if (list == null) {
                list = new ArrayList<QName>();
            }
            list.add(qn);
        }
        return list;
    }

    /**
     *
     *
     * @param r
     * @return
     */
    @Override
    public List<String> getSupportedLevels(Resource r) {
        log.debug("getSupportedLevels");
        List<String> list = wrapped.getSupportedLevels(r);
//        if (r instanceof CalendarResource) {
        addIfNotPresent(list, "3");
        list.add("calendar-access");
        
        // if present (but not actually implemented) causes problems with thunderbird
        //list.add("calendar-schedule");
        addIfNotPresent(list, "extended-mkcol");
        list.add("calendar-proxy");
//        }
        if (r instanceof SchedulingInboxResource) {
            list.add("schedule-inbox");
        }
        if (r instanceof SchedulingOutboxResource) {
            list.add("schedule-outbox");
        }
        return list;
    }

    private void addIfNotPresent(List<String> list, String s) {
        if (!list.contains(s)) {
            list.add(s);
        }
    }
}
