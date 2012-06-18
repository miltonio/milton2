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

import io.milton.http.values.HrefList;
import io.milton.principal.DiscretePrincipal;

/**
 *
 * @author brad
 */
public interface CalDavPrincipal extends DiscretePrincipal {
    /**
     * This is usually a single href which identifies the collection which
     * contains the users calendars. This might be the user's own href.
     *
     * Name:  calendar-home-set

   Namespace:  urn:ietf:params:xml:ns:caldav

   Purpose:  Identifies the URL of any WebDAV collections that contain
      calendar collections owned by the associated principal resource.

   Conformance:  This property SHOULD be defined on a principal
      resource.  If defined, it MAY be protected and SHOULD NOT be
      returned by a PROPFIND DAV:allprop request (as defined in Section
      12.14.1 of [RFC2518]).

   Description:  The CALDAV:calendar-home-set property is meant to allow
      users to easily find the calendar collections owned by the
      principal.  Typically, users will group all the calendar
      collections that they own under a common collection.  This
      property specifies the URL of collections that are either calendar
      collections or ordinary collections that have child or descendant
      calendar collections owned by the principal.

   Definition:

         <!ELEMENT calendar-home-set (DAV:href*)>

   Example:

       <C:calendar-home-set xmlns:D="DAV:"
                            xmlns:C="urn:ietf:params:xml:ns:caldav">
         <D:href>http://cal.example.com/home/bernard/calendars/</D:href>
       </C:calendar-home-set>
     *
     * @return
     */
    HrefList getCalendarHomeSet();


    /**
     * Return identifiers for this user:
     *
     * "Identify the calendar addresses of the associated principal resource."
     *
     * Eg: mailto:xxx@mysite.org
     *
     * @return
     */
    HrefList getCalendarUserAddressSet();


    /**
     * Return the path to the scheduling inbox. This should refer to a collection
     * with a resource type of schedule-inbox
     *
     * Scheduling is used to show when the principal is free or busy
     *
     * @return
     */
    String getScheduleInboxUrl();

    /**
     * Return the path to the scheduling outbox. This should refer to a collection
     * with a resource type of schedule-outbox
     *
     * Scheduling is used to show when the principal is free or busy
     *
     *
     *
     * @return
     */
    String getScheduleOutboxUrl();

    /**
     * Couldnt find any info about this property. Can only guess what its supposed to do.
     * 
     * @return
     */
    String getDropBoxUrl();
}
