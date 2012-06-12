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

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;

/**
 *
 * @author brad
 */
public interface MakeCalendarResource extends CollectionResource {

    /**
     * Create an empty calendar
     *
     * @param newName
     * @return
     * @throws NotAuthorizedException
     * @throws ConflictException
     * @throws BadRequestException
     */
    CollectionResource createCalendar(String newName) throws NotAuthorizedException, ConflictException, BadRequestException;

}
