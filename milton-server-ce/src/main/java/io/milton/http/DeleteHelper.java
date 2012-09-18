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

package io.milton.http;

import io.milton.resource.DeletableResource;
import io.milton.resource.Resource;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.event.EventManager;

/**
 * Supporting functions for the DeleteHandler
 *
 */
public interface DeleteHelper {
    /**
     * Check if the resource or any child resources are locked or otherwise not
     * deletable
     *
     * @param req
     * @param r
     * @return
     */
    boolean isLockedOut(Request req, Resource r) throws NotAuthorizedException, BadRequestException;

    /**
     * Delete the resource and any child resources
	 * 
	 * The implementation should fire delete events for all resources physically
	 * deleted.
     *
     * @param r
     */
    void delete(DeletableResource r, EventManager eventManager) throws NotAuthorizedException, ConflictException, BadRequestException;
}
