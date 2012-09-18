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

package io.milton.http.quota;

import io.milton.common.Path;
import io.milton.resource.CollectionResource;
import io.milton.http.Request;
import io.milton.resource.Resource;

/**
 * Implementations will check some aspect of whether or not its allowable
 * to load some content into the system. This may be whether there is sufficient
 * disk space, or whether the user's quota is full.
 *
 * This is generally called on a PUT, so there are 2 possibilities:
 *   a. the PUT is to an existing resource which will be replaced. Even if a
 * quota is currently exceeded this might be allowed if the new resource is no
 * larger then the one it is replacing
 *
 *   b. the PUT is to create a new resource. In this case it is simply an add,
 * but the parent folder might or might not exist.
 *
 * @author brad
 */
public interface StorageChecker {
    public enum StorageErrorReason {
        SER_QUOTA_EXCEEDED,
        SER_DISK_FULL
    }

    /**
     * Check to see if the operation should be allowed, when an existing resource
     * is to be overwritten or replaced.
     *
     * @param request
     * @param replaced - the resource being replaced
     * @param host
     * @return - null if the operation should proceed, otherwise a reason for the error
     */
    StorageErrorReason checkStorageOnReplace(Request request, CollectionResource parent, Resource replaced, String host);

    /**
     * Check to see if the operation should be allowed, when there is no existing
     * resource. The parent collection may or may not exist, so only its path is
     * provided.
     *
     * @param request
     * @param parentPath - the path to the parent collection. E.g. http://abc.com/path = /path
     * @param host
     * @return - null if the operation should proceed, or the reason for the failure
     */
    StorageErrorReason checkStorageOnAdd(Request request, CollectionResource nearestParent, Path parentPath, String host);
}
