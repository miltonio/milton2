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

import io.milton.http.SecurityManager;
import java.io.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
public class FileSystemResourceFactoryTest{

    File root;
    FileSystemResourceFactory factory;

    @BeforeEach
    protected void setUp() throws Exception {
        root = new File(System.getProperty("java.home"));
        SecurityManager sm = null;
        factory = new FileSystemResourceFactory( root, sm );
        System.out.println("testing with root: " + root.getAbsolutePath());
    }



    @Test
    public void testResolvePath_Root() {
        File f = factory.resolvePath(root, "/");
        assertEquals(root, f);
    }

    @Test
    public void testResolvePath_SubDir() {
        File f = factory.resolvePath(root, "/lib");
        assertEquals(new File(root,"lib"), f);
    }

    @Test
    public void testResolvePath_SubSubDir() {
        File f = factory.resolvePath(root, "/lib/security");
        assertEquals("security", f.getName());
        assertTrue(f.exists());
        assertTrue(f.isDirectory());
    }

    @Test
    public void testResolvePath_File() {
        File f = factory.resolvePath(root, "/lib/security/default.policy");
        assertEquals("default.policy", f.getName());
        assertTrue(f.exists());
        assertFalse(f.isDirectory());

    }

    @Test
    public void testResolvePath_RejectsDotDot() {
        assertNull(factory.resolvePath(root, "/../secret.txt"));
        assertNull(factory.resolvePath(root, "/lib/../../secret.txt"));
        assertNull(factory.resolvePath(root, "/.."));
    }

    @Test
    public void testResolvePath_RejectsSingleDot() {
        assertNull(factory.resolvePath(root, "/./lib"));
    }

    @Test
    public void testGetResource_RejectsTraversal() {
        assertNull(factory.getResource("localhost", "/../secret.txt"));
    }
}
