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
import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import io.milton.resource.CollectionResource;
import io.milton.resource.LockableResource;
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
