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
		String token = UUID.randomUUID().toString();
		return lock(timeout, lockInfo, r, token);

    }

    private  LockResult lock( LockTimeout timeout, LockInfo lockInfo, LockableResource r, String token ) {
        LockToken currentLock = currentLock( r );
        if( currentLock != null ) {
            return LockResult.failed( LockResult.FailureReason.ALREADY_LOCKED );
        }

        LockToken newToken = new LockToken( token, lockInfo, timeout );
        CurrentLock newLock = new CurrentLock( r.getUniqueId(), newToken, lockInfo.lockedByUser );
        locksByUniqueId.put( r.getUniqueId(), newLock );
        locksByToken.put( newToken.tokenId, newLock );
        return LockResult.success( newToken );
    }	
	
	@Override
    public synchronized LockResult refresh( String tokenId, LockableResource resource ) {
        CurrentLock curLock = locksByToken.get( tokenId );
		if( curLock == null || curLock.token == null ) {
			log.warn("attempt to refresh missing token: " + tokenId + " on resource: " + resource.getName() + " will create a new lock");
			LockTimeout timeout = new LockTimeout(60*60l);
			LockInfo lockInfo = new LockInfo(LockInfo.LockScope.EXCLUSIVE, LockInfo.LockType.WRITE, tokenId, LockInfo.LockDepth.ZERO);
			return lock(timeout, lockInfo, resource, tokenId);
		}
        curLock.token.setFrom( new Date() );
        return LockResult.success( curLock.token );
    }

	@Override
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
        if( curLock == null ) {
			return null;
		}
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

