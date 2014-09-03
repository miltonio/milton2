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

package io.milton.http.webdav;

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
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
    PropFindResponse setProperties(String href, PropPatchParseResult parseResult, Resource r) throws NotAuthorizedException, BadRequestException, ConflictException;

    /**
     * Return whether the given resource can be proppatch'ed with this
     * PropPatchSetter
     *
     * @param r
     * @return
     */
    boolean supports(Resource r);
}
