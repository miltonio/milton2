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

import io.milton.http.Request;

/**
 * An extension to DeletableResource and CollectionResource, this interface
 * adds a method to support efficient detection of child locks.
 *<P/>
 * This interface ONLY needs to be implemented by those who need to improve
 * performance of deleting collections
 *<P/>
 * In the case of deleting a collection resource it might be inefficient to
 * check for locks by recursively walking through the collection. By implementing
 * this interface you have the ability to use a more efficient approach
 *
 * See HandlerHelper.isLockedOut for an example of checking for locks:
 *<P/>
 * <pre>
 * {@code
 *     public boolean isLockedOut( Request inRequest, Resource inResource ) {
 *       if( inResource == null || !( inResource instanceof LockableResource ) ) {
 *           return false;
 *       }
 *       LockableResource lr = (LockableResource) inResource;
 *       LockToken token = lr.getCurrentLock();
 *       if( token != null ) {
 *           Auth auth = inRequest.getAuthorization();
 *           String lockedByUser = token.info.lockedByUser;
 *           if( lockedByUser == null ) {
 *               log.warn( "Resource is locked with a null user. Ignoring the lock" );
 *               return false;
 *           } else if( !lockedByUser.equals( auth.getUser() ) ) {
 *               log.info( "fail: lock owned by: " + lockedByUser + " not by " + auth.getUser() );
 *               String value = inRequest.getIfHeader();
 *               if( value != null ) {
 *                   if( value.contains( "opaquelocktoken:" + token.tokenId + ">" ) ) {
 *                       log.info( "Contained valid token. so is unlocked" );
 *                       return false;
 *                   }
 *               }
 *               return true;
 *           }
 *       }
 *       return false;
 *   }
 * }
* </pre>
 * @author brad
 */
public interface DeletableCollectionResource extends DeletableResource, CollectionResource {
    /**
     * Check to see if this resource or any child resource are locked by someone
     * other then the current user (as per the Authorisation property of the request)
     *
     * @param request
     * @return - true indicates that the DELETE request must not proceed because
     * this resource or at least one child resource is locked by someone other
     * then the current user
     */
    boolean isLockedOutRecursive(Request request);
}
