/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
