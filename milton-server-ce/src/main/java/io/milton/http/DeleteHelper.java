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
