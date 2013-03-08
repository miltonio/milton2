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
