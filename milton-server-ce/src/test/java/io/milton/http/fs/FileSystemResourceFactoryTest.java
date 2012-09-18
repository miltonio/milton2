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

import io.milton.http.fs.FileSystemResourceFactory;
import io.milton.http.SecurityManager;
import java.io.File;
import junit.framework.TestCase;

/**
 *
 */
public class FileSystemResourceFactoryTest extends TestCase{

    File root;
    FileSystemResourceFactory factory;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        root = new File(System.getProperty("java.home"));
        SecurityManager sm = null;
        factory = new FileSystemResourceFactory( root, sm );
        System.out.println("testing with root: " + root.getAbsolutePath());
    }
    
    
    
    public void testResolvePath_Root() {
        File f = factory.resolvePath(root, "/");
        assertEquals(root, f);
    }
    
    public void testResolvePath_SubDir() {
        File f = factory.resolvePath(root, "/lib");
        assertEquals(new File(root,"lib"), f);
    }

    public void testResolvePath_SubSubDir() {
        File f = factory.resolvePath(root, "/lib/security");
        assertEquals("security", f.getName());
        assertTrue(f.exists());
        assertTrue(f.isDirectory());
    }
    
    public void testResolvePath_File() {
        File f = factory.resolvePath(root, "/lib/security/java.policy");
        assertEquals("java.policy", f.getName());
        assertTrue(f.exists());
        assertFalse(f.isDirectory());
        
    }
    
}
