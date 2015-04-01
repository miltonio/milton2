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
import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import io.milton.http.exceptions.NotAuthorizedException;

/**
 * A collection which allows locking "unmapped resources". This means that a LOCK
 * method can effectively create an empty resource which is immediately locked.
 * Implement this in conjunction with {@see LockableResource} on child resources to fully
 * support locking.
 * <P/>
 * <I>Note that this interface now extends {@see LockableResource} because collection resources
 * need to implement both in most cases.</I>
 * <P/>
 * If, however, you don't want your collection resources to be lockable, just
 * implement {@see ConditionalCompatibleResource}.
 * <P/>
 * See <A HREF="http://www.ettrema.com:8080/browse/MIL-14">http://www.ettrema.com:8080/browse/MIL-14</A>.
 * <P/>
 * @author brad
 */
public interface  LockingCollectionResource extends CollectionResource, LockableResource {
    
    /**
     * Create an empty non-collection resource of the given name and immediately
     * lock it.
     * <P/>
     * It is suggested that the implementor have a specific Resource class to act
     * as the lock null resource. You should consider using the {@see LockNullResource}
     * interface.
     *
     * @see  LockNullResource
     * 
     * @param name - the name of the resource to create
     * @param timeout - in seconds
     * @param lockInfo
     * @return
     */
    public LockToken createAndLock(String name, LockTimeout timeout, LockInfo lockInfo) throws NotAuthorizedException;
    
}
