/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
	private Date createdDate;
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
