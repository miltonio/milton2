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
package io.milton.principal;

import io.milton.http.values.HrefList;
import io.milton.http.values.SupportedCalendarComponentListsSet;
import io.milton.resource.PropFindableResource;

/**
 *
 * @author brad
 */
public interface CalDavPrincipal extends DiscretePrincipal, PropFindableResource {

    /**
     * This is usually a single href which identifies the collection which
     * contains the users calendars. This might be the user's own href.
     *
     * Name: calendar-home-set
     *
     * Namespace: urn:ietf:params:xml:ns:caldav
     *
     * Purpose: Identifies the URL of any WebDAV collections that contain
     * calendar collections owned by the associated principal resource.
     *
     * Conformance: This property SHOULD be defined on a principal resource. If
     * defined, it MAY be protected and SHOULD NOT be returned by a PROPFIND
     * DAV:allprop request (as defined in Section 12.14.1 of [RFC2518]).
     *
     * Description: The CALDAV:calendar-home-set property is meant to allow
     * users to easily find the calendar collections owned by the principal.
     * Typically, users will group all the calendar collections that they own
     * under a common collection. This property specifies the URL of collections
     * that are either calendar collections or ordinary collections that have
     * child or descendant calendar collections owned by the principal.
     *
     * Definition:
     *
     * <!ELEMENT calendar-home-set (DAV:href*)>
     *
     * Example:
     *
     * <C:calendar-home-set xmlns:D="DAV:"
     * xmlns:C="urn:ietf:params:xml:ns:caldav">
     * <D:href>http://cal.example.com/home/bernard/calendars/</D:href>
     * </C:calendar-home-set>
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
     * Get the list of supported combinations of component types, or just return
     * null
     *
     * @return
     */
    SupportedCalendarComponentListsSet getSupportedComponentSets();

    /**
     *
     * See
     * http://tools.ietf.org/html/draft-desruisseaux-caldav-sched-08#section-13.2.4
     *
     * Identifies the calendar user type of the associated principal resource.
     * Value: Same values allowed for the iCalendar "CUTYPE" property parameter
     * defined in Section 3.2.3 of [I-D.ietf-calsify-rfc2445bis].
     * 
     * Should be one of: INDIVIDUAL, GROUP, RESOURCE, ROOM, UNKNOWN
     *
     * @return
     */
    String getCalendarUserType();

}
