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

package io.milton.http;

import io.milton.http.LockInfo;
import io.milton.http.LockResult;
import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import io.milton.resource.LockableResource;
import io.milton.http.exceptions.NotAuthorizedException;

/**
 *
 */
public interface  LockManager {

    LockResult lock(LockTimeout timeout, LockInfo lockInfo, LockableResource resource) throws NotAuthorizedException;

    LockResult refresh(String token, LockableResource resource) throws NotAuthorizedException;

    void unlock(String tokenId, LockableResource resource) throws NotAuthorizedException;

    LockToken getCurrentToken(LockableResource resource);

}

