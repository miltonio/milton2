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

package io.milton.resource;

/**
 * 4.2. Scheduling Inbox Collection


   A scheduling Inbox collection contains copies of incoming scheduling
   messages.  These may be requests sent by an Organizer, or replies
   sent by an Attendee in response to a request.

   A scheduling Inbox collection MUST report the DAV:collection and
   CALDAV:schedule-inbox XML elements in the value of the DAV:
   resourcetype property.  The element type declaration for CALDAV:
   schedule-inbox is:

      <!ELEMENT schedule-inbox EMPTY>

   Example:

      <D:resourcetype xmlns:D="DAV:">
        <D:collection/>
        <C:schedule-inbox xmlns:C="urn:ietf:params:xml:ns:caldav"/>
      </D:resourcetype>

   Scheduling Inbox collections MUST only contain calendar object
   resources that obey the restrictions specified in iTIP [RFC5546].
   Consequently, scheduling Inbox collections MUST NOT contain any types
   of collection resources.  Restrictions defined in Section 4.1 of



Daboo & Desruisseaux     Expires April 28, 2011                [Page 13]


Internet-Draft        CalDAV Scheduling Extensions          October 2010


   CalDAV "calendar-access" [RFC4791] on calendar object resources
   contained in calendar collections (e.g., "UID" uniqueness) don't
   apply to calendar object resources contained in a scheduling Inbox
   collection.  Thus, multiple calendar object resources contained in a
   scheduling Inbox collection can have the same "UID" property value
   (i.e., multiple scheduling messages for the same calendar component).

   New WebDAV ACL [RFC3744] privileges can be set on the scheduling
   Inbox collection to control from whom the Calendar User associated
   with the scheduling Inbox collection will accept scheduling messages
   from.  See Section 13.1 for more details.

   A scheduling Inbox collection MUST NOT be a child (at any depth) of a
   calendar collection resource.

   The following WebDAV properties specified in CalDAV "calendar-access"
   [RFC4791] MAY also be defined on scheduling Inbox collections:

      CALDAV:calendar-timezone - when present this contains a time zone
      that the server can use when calendar date-time operations are
      carried out, for example when a time-range CALDAV:calendar-query
      REPORT is targeted at a scheduling Inbox collection.

      CALDAV:supported-calendar-component-set - when present this
      indicates the allowed calendar component types for scheduling
      messages delivered to the scheduling Inbox collection.

      CALDAV:supported-calendar-data - when present this indicates the
      allowed media types for scheduling messages delivered to the
      scheduling Inbox collection.

      CALDAV:max-resource-size - when present this indicates the maximum
      size of a resource in octets that the server is willing to accept
      for scheduling messages delivered to the scheduling Inbox
      collection.

      CALDAV:min-date-time - when present this indicates the earliest
      date and time (in UTC) that the server is willing to accept for
      any DATE or DATE-TIME value in scheduling messages delivered to
      the scheduling Inbox collection.

      CALDAV:max-date-time - when present this indicates the latest date
      and time (in UTC) that the server is willing to accept for any
      DATE or DATE-TIME value in scheduling messages delivered to the
      scheduling Inbox collection.

      CALDAV:max-instances - when present this indicates the maximum
      number of recurrence instances in scheduling messages delivered to



Daboo & Desruisseaux     Expires April 28, 2011                [Page 14]


Internet-Draft        CalDAV Scheduling Extensions          October 2010


      the scheduling Inbox collection.

      CALDAV:max-attendees-per-instance - when present this indicates
      the maximum number of ATTENDEE properties in any instance of
      scheduling messages delivered to the scheduling Inbox collection.

 *
 * @author brad
 */
public interface SchedulingInboxResource extends BaseSchedulingResource {

}
