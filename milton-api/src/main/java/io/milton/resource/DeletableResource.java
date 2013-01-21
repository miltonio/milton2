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
 * should permit the operation and remove the lock.
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
