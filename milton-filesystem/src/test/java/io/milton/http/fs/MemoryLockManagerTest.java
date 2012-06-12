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


import io.milton.http.fs.FsMemoryLockManager;
import io.milton.http.fs.FsFileResource;
import io.milton.http.fs.FsResource;
import io.milton.http.fs.SimpleFileContentService;
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
