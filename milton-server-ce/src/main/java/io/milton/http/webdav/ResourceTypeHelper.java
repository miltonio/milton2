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

package io.milton.http.webdav;

import io.milton.resource.Resource;
import java.util.List;
import javax.xml.namespace.QName;

/**
 * Assists in determining the logical type of a given resource, for the
 * purpose of the protocol
 *
 * @author brad
 */
public interface ResourceTypeHelper {
    /**
     * Get the resource types for a PROPFIND request. E.g. collection, calendar, etc
     *
     * @param r
     * @return - a list of QName's where the URI determines the namespace (E.g. DAV,
     * http://calendarserver.org/ns/) and the name is the name of the resource type
     * E.g. collection, calendar
     */
    List<QName> getResourceTypes(Resource r);


    /**
     * Gets the list of supported level names for a resource. This is to populate
     * the DAV header, E.g. 1, access-control, calendar-access
     *
     * Typically, this list is determined by the type of the resource. Eg, if the
     * resource supports locking then it returns 1, 2. Note that should **NOTE** be
     * sensitive to authorisation or state. Ie a resource should have supported levels
     * of 1,2 if it supports locking, regardless of whether or not the current user
     * has permission to do so, and regardless of whether the resource can be locked in
     * its current state.
     *
     * @param r - the resource
     * @return - the list of supported level identifiers supported by the given resource
     */
    List<String> getSupportedLevels(Resource r);
}
