/*
 *
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.milton.resource;

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;

import java.net.URI;
import java.util.Map;

/**
 * CollectionResource which supports sync-token.
 * @author pomu0325
 *
 */
public interface SyncCollectionResource extends CollectionResource {
  /**
   * Current sync-token.
   * @return
   */
  URI getSyncToken();
  
  /**
   * @return Map contains href -> Resource. Removed resource should be included as {@link io.milton.resource.RemovedResource}.
   */
  Map<String, Resource> findResourcesBySyncToken(URI syncToken) throws NotAuthorizedException, BadRequestException;

  /* 3.4.  Types of Changes Reported on Initial Synchronization

   When the DAV:sync-collection request contains an empty DAV:sync-token
   element, the server MUST return all member URLs of the collection
   (taking account of the DAV:sync-level XML element value as per
   Section 3.3, and optional truncation of the result set as per
   Section 3.6) and it MUST NOT return any removed member URLs.  All
   types of member (collection or non-collection) MUST be reported.
   */
}
