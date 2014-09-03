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


import io.milton.http.LockInfo;
import io.milton.http.LockResult;
import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import io.milton.http.exceptions.NotAuthorizedException;
import java.io.File;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class MemoryLockManagerTest extends TestCase {

    FsMemoryLockManager lockManager;

    @Override
    protected void setUp() throws Exception {
        lockManager = new FsMemoryLockManager();
    }

    
    public void testLockUnLock() throws NotAuthorizedException {
        LockTimeout timeout = new LockTimeout( 100l );
        LockInfo lockInfo = new LockInfo( LockInfo.LockScope.NONE, LockInfo.LockType.READ, "me", LockInfo.LockDepth.ZERO );
        SimpleFileContentService contentService = new SimpleFileContentService();
        FsResource resource = new FsFileResource( null, null, new File( File.pathSeparator ), contentService );

        // lock it
        LockResult res = lockManager.lock( timeout, lockInfo, resource );
        assertNotNull( res );
        assertTrue( res.isSuccessful() );

        // check is locked
        LockToken token = lockManager.getCurrentToken( resource );
        assertNotNull( token );
        assertEquals( token.tokenId, res.getLockToken().tokenId );

        // unlock
        lockManager.unlock( token.tokenId, resource );

        // check removed
        token = lockManager.getCurrentToken( resource );
        assertNull( token );
    }

    public void testRefresh() {
    }

    public void testUnlock() {
    }

    public void testGetCurrentToken() {
    }
}
