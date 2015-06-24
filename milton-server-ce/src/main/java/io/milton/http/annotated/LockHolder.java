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
package io.milton.http.annotated;

import io.milton.http.LockInfo;
import io.milton.http.LockTimeout;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class LockHolder {
	private final UUID id;
	private String name;
	private String parentCollectionId;
	private final Date createdDate;
	private LockTimeout lockTimeout;
	private LockInfo lockInfo;

	public LockHolder(UUID id) {
		this.id = id;
		this.createdDate = new Date();
	}	
	
	/**
	 * The lock token
	 * 
	 * @return 
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * Name of the resource
	 * 
	 * @return 
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Unique ID of the parent collection which contains this. Also used as
	 * key in the map
	 * 
	 * @return 
	 */
	public String getParentCollectionId() {
		return parentCollectionId;
	}

	public void setParentCollectionId(String parentCollectionId) {
		this.parentCollectionId = parentCollectionId;
	}

	/**
	 * When this was created
	 * 
	 * @return 
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * The lock timeout that was used when this was created. Will be used to 
	 * check for expiry
	 * 
	 * @return 
	 */
	public LockTimeout getLockTimeout() {
		return lockTimeout;
	}

	public void setLockTimeout(LockTimeout lockTimeout) {
		this.lockTimeout = lockTimeout;
	}

	public LockInfo getLockInfo() {
		return lockInfo;
	}

	public void setLockInfo(LockInfo lockInfo) {
		this.lockInfo = lockInfo;
	}
	
	
	
}
