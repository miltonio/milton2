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
