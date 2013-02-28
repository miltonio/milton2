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

import io.milton.http.LockInfo;
import io.milton.http.LockResult;
import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import io.milton.resource.Resource;
import io.milton.http.exceptions.LockedException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.PreConditionFailedException;

/**
 * webDAV LOCK
 *
 * You should also implement LockingCollectionResource on your collections for full
 * locking support
 * 
 * @author brad
 */
public interface LockableResource extends Resource {
    /**
     * Lock this resource and return a token
     * 
     * @param timeout - in seconds, or null
     * @param lockInfo
     * @return - a result containing the token representing the lock if succesful,
     * otherwise a failure reason code
     */
    public LockResult lock(LockTimeout timeout, LockInfo lockInfo) throws NotAuthorizedException, PreConditionFailedException, LockedException;
    
    /**
     * Renew the lock and return new lock info
     * 
     * @param token
     * @return
     */
    public LockResult refreshLock(String token) throws NotAuthorizedException, PreConditionFailedException;

    /**
     * If the resource is currently locked, and the tokenId  matches the current
     * one, unlock the resource
     *
     * @param tokenId
     */
    public void unlock(String tokenId) throws NotAuthorizedException, PreConditionFailedException;

    /**
     *
     * @return - the current lock, if the resource is locked, or null
     */
    public LockToken getCurrentLock();
}
