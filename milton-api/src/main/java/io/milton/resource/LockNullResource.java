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

import io.milton.resource.PropFindableResource;

/**
 * (from the spec)<BR/>
 * <B>7.4 Write Locks and Null Resources</B>
 * <P/>
 * It is possible to assert a write lock on a null resource in order to lock the name.
 * <P/>
 * A write locked null resource, referred to as a lock-null resource, MUST respond with
 * a 404 (Not Found) or 405 (Method Not Allowed) to any HTTP/1.1 or DAV methods except
 * for PUT, MKCOL, OPTIONS, PROPFIND, LOCK, and UNLOCK.
 * <P/>
 * A lock-null resource MUST appear
 * as a member of its parent collection. Additionally the lock-null resource MUST have
 * defined on it all mandatory DAV properties. Most of these properties, such as all
 * the get* properties, will have no value as a lock-null resource does not support the GET method.
 * Lock-Null resources MUST have defined values for lockdiscovery and supportedlock properties.
 * <P/>
 * Until a method such as PUT or MKCOL is successfully executed on the lock-null resource 
 * the resource MUST stay in the lock-null state. However, once a PUT or MKCOL is
 * successfully executed on a lock-null resource the resource ceases to be in the lock-null state.
 * <P/>
 * If the resource is unlocked, for any reason, without a PUT, MKCOL, or similar method
 * having been successfully executed upon it then the resource MUST return to the null state.
 *
 *
 */
public interface LockNullResource extends PutableResource, PropFindableResource, LockableResource {
}
