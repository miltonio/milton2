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

/**
 * Applies a proppatch result to a resource
 *
 * This interface is only really needed to support updating properties via the
 * old PropPatchableResource.setFields() method. The more modern way of doing
 * things is through the PropertySource interface, which is symmetrical for
 * reading and writing properties.
 *
 *
 * @author brad
 */
public interface PropPatchSetter {


    /**
     * Update the given resource with the properties specified in the parseResult
     * and return appropriate responses
     *
     * @param href - the address of the resource being patched
     * @param parseResult - the list of properties to be mutated
     * @param r - the resource to be updated
     * @return - response indicating success or otherwise for each field. Note
     * that success responses should not contain the value
     */
    PropFindResponse setProperties(String href, PropPatchRequestParser.ParseResult parseResult, Resource r);

    /**
     * Return whether the given resource can be proppatch'ed with this
     * PropPatchSetter
     *
     * @param r
     * @return
     */
    boolean supports(Resource r);
}
