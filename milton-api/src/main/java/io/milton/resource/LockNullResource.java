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
