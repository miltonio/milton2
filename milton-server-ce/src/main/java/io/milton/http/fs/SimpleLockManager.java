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

package io.milton.http.fs;

import io.milton.http.LockManager;
import io.milton.http.LockInfo;
import io.milton.http.LockResult;
import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import io.milton.resource.LockableResource;
import io.milton.http.exceptions.NotAuthorizedException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keys on getUniqueID of the locked resource.
 *
 */
public class SimpleLockManager implements LockManager {

    private static final Logger log = LoggerFactory.getLogger( SimpleLockManager.class );
    /**
     * maps current locks by the file associated with the resource
     */
    Map<String, CurrentLock> locksByUniqueId;
    Map<String, CurrentLock> locksByToken;

    public SimpleLockManager() {
        locksByUniqueId = new HashMap<String, CurrentLock>();
        locksByToken = new HashMap<String, CurrentLock>();
    }

	@Override
    public synchronized LockResult lock( LockTimeout timeout, LockInfo lockInfo, LockableResource r ) {
        LockToken currentLock = currentLock( r );
        if( currentLock != null ) {
            return LockResult.failed( LockResult.FailureReason.ALREADY_LOCKED );
        }

        LockToken newToken = new LockToken( UUID.randomUUID().toString(), lockInfo, timeout );
        CurrentLock newLock = new CurrentLock( r.getUniqueId(), newToken, lockInfo.lockedByUser );
        locksByUniqueId.put( r.getUniqueId(), newLock );
        locksByToken.put( newToken.tokenId, newLock );
        return LockResult.success( newToken );
    }

    public synchronized LockResult refresh( String tokenId, LockableResource resource ) {
        CurrentLock curLock = locksByToken.get( tokenId );
        curLock.token.setFrom( new Date() );
        return LockResult.success( curLock.token );
    }

    public synchronized void unlock( String tokenId, LockableResource r ) throws NotAuthorizedException {
        LockToken lockToken = currentLock( r );
        if( lockToken == null ) {
            log.debug( "not locked" );
            return;
        }
        if( lockToken.tokenId.equals( tokenId ) ) {
            removeLock( lockToken );
        } else {
            throw new NotAuthorizedException( r );
        }
    }

    private LockToken currentLock( LockableResource resource ) {
        CurrentLock curLock = locksByUniqueId.get( resource.getUniqueId() );
        if( curLock == null ) return null;
        LockToken token = curLock.token;
        if( token.isExpired() ) {
            removeLock( token );
            return null;
        } else {
            return token;
        }
    }

    private void removeLock( LockToken token ) {
        log.debug( "removeLock: " + token.tokenId );
        CurrentLock currentLock = locksByToken.get( token.tokenId );
        if( currentLock != null ) {
            locksByUniqueId.remove( currentLock.id );
            locksByToken.remove( currentLock.token.tokenId );
        } else {
            log.warn( "couldnt find lock: " + token.tokenId );
        }
    }

    public LockToken getCurrentToken( LockableResource r ) {
        CurrentLock lock = locksByUniqueId.get( r.getUniqueId() );
        if( lock == null ) return null;
        LockToken token = new LockToken();
        token.info = new LockInfo( LockInfo.LockScope.EXCLUSIVE, LockInfo.LockType.WRITE, lock.lockedByUser, LockInfo.LockDepth.ZERO );
        token.info.lockedByUser = lock.lockedByUser;
        token.timeout = lock.token.timeout;
        token.tokenId = lock.token.tokenId;
        return token;
    }

    class CurrentLock {

        final String id;
        final LockToken token;
        final String lockedByUser;

        public CurrentLock( String id, LockToken token, String lockedByUser ) {
            this.id = id;
            this.token = token;
            this.lockedByUser = lockedByUser;
        }
    }
}

