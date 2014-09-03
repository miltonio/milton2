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

import io.milton.cache.CacheManager;
import io.milton.http.Auth;
import io.milton.http.HttpManager;
import io.milton.http.LockManager;
import io.milton.http.LockInfo;
import io.milton.http.LockResult;
import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import io.milton.http.Request;
import io.milton.resource.LockableResource;
import io.milton.http.exceptions.NotAuthorizedException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keys on getUniqueID of the locked resource.
 *
 */
public class SimpleLockManager implements LockManager {

	private static final Logger log = LoggerFactory.getLogger(SimpleLockManager.class);

	private static CurrentLock toCurrentLock(String formattedLock) {
		if (formattedLock == null) {
			return null;
		}
		try {
			String[] arr = formattedLock.split("\n");
			String id = arr[0];
			String tokenId = arr[1];
			long tm = Long.parseLong(arr[2]);
			Date dt = new Date(tm);
			String lockedBy = arr[3];
			Long secs = null;
			if (arr.length > 4 && arr[4] != null) {
				secs = Long.parseLong(arr[4]);
			}
			return new CurrentLock(id, tokenId, dt, lockedBy, secs);
		} catch (Throwable e) {
			log.error("Exception parsing lock: " + formattedLock, e);
			return null;
		}
	}

	private static String toString(CurrentLock lock) {
		String id = lock.id;
		String token = lock.token.tokenId;
		long tm = lock.token.getFrom().getTime();
		String lockedBy = lock.lockedByUser;
		Long secs = lock.token.timeout.getSeconds();
		return id + "\n" + token + "\n" + tm + "\n" + lockedBy + "\n" + (secs != null ? secs : "");
	}

	/**
	 * maps current locks by the file associated with the resource
	 */
	private final Map<String, String> locksByUniqueId;
	private final Map<String, String> locksByToken;

	public SimpleLockManager(CacheManager cacheManager) {
		locksByUniqueId = cacheManager.getMap("fuse-locks-byuniqueId");
		locksByToken = cacheManager.getMap("fuse-locks-bytoken");
	}

	@Override
	public LockResult lock(LockTimeout timeout, LockInfo lockInfo, LockableResource r) {
		return lock(timeout, lockInfo, r.getUniqueId());
	}

	public LockResult lock(LockTimeout timeout, LockInfo lockInfo, String uniqueId) {
		String token = UUID.randomUUID().toString();
		return lock(timeout, lockInfo, uniqueId, token);
	}

	private LockResult lock(LockTimeout timeout, LockInfo lockInfo, LockableResource r, String token) {
		return lock(timeout, lockInfo, r.getUniqueId(), token);
	}

	public synchronized LockResult lock(LockTimeout timeout, LockInfo lockInfo, String uniqueId, String token) {
		LockToken currentLock = currentLock(uniqueId);
		if (currentLock != null) {
			return LockResult.failed(LockResult.FailureReason.ALREADY_LOCKED);
		}
		LockToken newToken = new LockToken(token, lockInfo, timeout);
		String lockedByUser = lockInfo.lockedByUser; // Use this by default, but will normally overwrite with current user
		Request req = HttpManager.request();
		if (req != null) {
			Auth auth = req.getAuthorization();
			if ( auth != null && auth.getUser() != null) {
				lockedByUser = auth.getUser();
			}
		}

		log.info("Lock as user {}", lockedByUser);
		CurrentLock newLock = new CurrentLock(uniqueId, newToken, lockedByUser);
		String sNewLock = newLock.toString();
		locksByUniqueId.put(uniqueId, sNewLock);
		locksByToken.put(token, sNewLock);
		return LockResult.success(newToken);
	}

	@Override
	public synchronized LockResult refresh(String tokenId, LockableResource resource) {
		String sCurLock = locksByToken.get(tokenId);
		CurrentLock curLock = null;
		if (sCurLock != null) {
			curLock = toCurrentLock(sCurLock);
		}

        // Some clients (yes thats you cadaver) send etags instead of lock tokens in the If header
		// So if the resource is locked by the current user just do a normal refresh
		if (curLock == null) {
			sCurLock = locksByUniqueId.get(resource.getUniqueId());
			if (sCurLock != null) {
				curLock = toCurrentLock(sCurLock);
			}
		}

		if (curLock == null || curLock.token == null) {

			log.warn("attempt to refresh missing token/etaqg: " + tokenId + " on resource: " + resource.getName() + " will create a new lock");
			LockTimeout timeout = new LockTimeout(60 * 60l);
			String lockedByUser = null;
			Auth auth = HttpManager.request().getAuthorization();
			if (auth != null) {
				lockedByUser = auth.getUser();
			} else {
				log.warn("No user in context, lock wont be very effective");
			}
			LockInfo lockInfo = new LockInfo(LockInfo.LockScope.EXCLUSIVE, LockInfo.LockType.WRITE, lockedByUser, LockInfo.LockDepth.ZERO);
			return lock(timeout, lockInfo, resource, UUID.randomUUID().toString());
		} else {
			curLock.token.setFrom(new Date());
			return LockResult.success(curLock.token);
		}
	}

	@Override
	public synchronized void unlock(String tokenId, LockableResource r) throws NotAuthorizedException {
		LockToken lockToken = currentLock(r.getUniqueId());
		if (lockToken == null) {
			log.debug("not locked");
			return;
		}
		if (lockToken.tokenId.equals(tokenId)) {
			removeLock(lockToken);
		} else {
			throw new NotAuthorizedException("Non-matching tokens: " + tokenId + " != " + lockToken.tokenId, r);

		}
	}

	private LockToken currentLock(String uniqueId) {
		String sCurLock = locksByUniqueId.get(uniqueId);
		if (sCurLock == null) {
			return null;
		}
		CurrentLock curLock = toCurrentLock(sCurLock);
		if (curLock == null) {
			return null;
		}
		LockToken token = curLock.token;
		if (token.isExpired()) {
			removeLock(token);
			return null;
		} else {
			return token;
		}
	}

	private void removeLock(LockToken token) {
		log.debug("removeLock: " + token.tokenId);
		String sCurrentLock = locksByToken.get(token.tokenId);
		if (sCurrentLock != null) {
			CurrentLock currentLock = toCurrentLock(sCurrentLock);
			locksByUniqueId.remove(currentLock.id);
			locksByToken.remove(currentLock.token.tokenId);
		} else {
			log.warn("couldnt find lock: " + token.tokenId);
		}
	}

	@Override
	public LockToken getCurrentToken(LockableResource r) {
		if (r == null) {
			return null;
		}
		if (r.getUniqueId() == null) {
			log.warn("No uniqueID for resource: " + r.getName() + " :: " + r.getClass());
			return null;
		}
		String sLock = locksByUniqueId.get(r.getUniqueId());
		if (sLock == null) {
			return null;
		}
		CurrentLock lock = toCurrentLock(sLock);
		if (lock == null) {
			return null;
		}
		LockToken token = new LockToken();
		token.info = new LockInfo(LockInfo.LockScope.EXCLUSIVE, LockInfo.LockType.WRITE, lock.lockedByUser, LockInfo.LockDepth.ZERO);
		token.info.lockedByUser = lock.lockedByUser;
		token.timeout = lock.token.timeout;
		token.tokenId = lock.token.tokenId;
		return token;
	}

	public Map<String, String> getLocksByUniqueId() {
		return locksByUniqueId;
	}

	public void clearLocks() {
		log.warn("CLEARING LOCKS!!!");
		locksByToken.clear();
		locksByUniqueId.clear();
	}

	public static class CurrentLock {

		final String id;
		final LockToken token;
		final String lockedByUser;

		public CurrentLock(String uniqueId, LockToken token, String lockedByUser) {
			this.id = uniqueId;
			this.token = token;
			this.lockedByUser = lockedByUser;
		}

		/**
		 *
		 * @param uniqueId - unique ID of the resource
		 * @param tokenId - the lock token
		 * @param from - the date the lock was from
		 * @param lockedByUser - who locked it
		 * @param seconds - seconds to lock the resource for
		 */
		public CurrentLock(String uniqueId, String tokenId, Date from, String lockedByUser, Long seconds) {
			this.id = uniqueId;
			this.lockedByUser = lockedByUser;

			LockTimeout timeout = new LockTimeout(seconds);
			LockInfo info = new LockInfo(LockInfo.LockScope.EXCLUSIVE, LockInfo.LockType.WRITE, lockedByUser, LockInfo.LockDepth.ZERO);
			this.token = new LockToken(tokenId, info, timeout);
			token.setFrom(from);
		}

		@Override
		public String toString() {
			String s = SimpleLockManager.toString(this);
			return s;
		}

	}
}
