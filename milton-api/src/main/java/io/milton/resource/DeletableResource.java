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

import io.milton.resource.Resource;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;

/**
 * Implement this to allow your resource to be deleted by webdav clients.
 *
 * Milton will ensure there are no locks which prevent the delete, however the
 * current user might have the resource locked in which case your implementation
 *
 * Usually milton will recursively call delete on all children within a collection
 * being deleted. However you can prevent this my implementing DeletableCollectionResource
 * which causes milton to ONLY call delete on the specific resource being deleted. In
 * which case it is your responsibility to test for locks on all child resources
 * 
 */
public interface DeletableResource extends Resource{

    /**
     * Non-recursive delete of this resource. Milton will call delete on child
     * resources first.
     *
     * @throws NotAuthorizedException - if the operation should not be permitted for security reasons
     * @throws ConflictException - if there is some pre-condition that has not been met, or there is
     * aspect of the resource state which prevents the resource from being deleted
     * @throws BadRequestException - if there is some aspect of the request which means
     * it is not sufficient to perform a delete.
     */
    void delete() throws NotAuthorizedException, ConflictException, BadRequestException;
    
}
