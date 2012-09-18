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

import io.milton.http.FileItem;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import java.util.Map;

public interface PostableResource extends  GetableResource {
    
    /**
     * Called after a POST request
     * 
     * @param parameters
     * @param files
     * @return - null,or an address if a redirect is required.
     * @throws BadRequestException
     * @throws NotAuthorizedException
     * @throws ConflictException 
     */
    String processForm(Map<String,String> parameters, Map<String,FileItem> files) throws BadRequestException, NotAuthorizedException, ConflictException;
}
