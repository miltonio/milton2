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

import io.milton.resource.Resource;


/**
 * Indicates a principle which is identifiable by a URL, like a user or
 * an application defined group
 *
 * @author brad
 */
public interface DiscretePrincipal extends Principal, Resource{

        
    /**
     * A URL to identify this principle **and** the owner of this principal. Note the relationship between this and
	 * the AccessControlledResource.getPrincipalURL method which returns the principal
	 * that owns the resource.
	 * 
	 * It is assumed that where a AccessControlledResource instance is also a DiscretePrincipal
	 * that the getPrincipalURL method will return the url of itself.
	 * 
	 * In other words, we make the semantic decision that a principle owns itself.
     *
     * @return
     */
    public String getPrincipalURL();


}
