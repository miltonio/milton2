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

package io.milton.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author brad
 */
public class PathTest {


    @Test
    public void test() {
        Path path = Path.path("/brad/test/1");
        System.out.println("path name: " + path.getName());

        assertEquals("1",path.getName());

        Path p2 = Path.path("/brad/test/1");
        assertEquals(path,p2);
        Path parent = Path.path("/brad/test");
        assertEquals(parent,path.getParent());
        System.out.println("----------------------");
    }

    @Test
    public void testSingle() {
        Path p = Path.path("abc");
        String s = p.toString();
        assertEquals("abc",s);
    }

    @Test
    public void testStrip() {
        Path path = Path.path("/a/b/c");
        Path stripped = path.getStripFirst();
        String s = stripped.toString();
        System.out.println("s: " + s);
        assertEquals("/b/c",s);
    }

    @Test
    public void testAbsolute() {
        Path path = Path.path("/a/b/c");
        assertFalse(path.isRelative());
    }

    @Test
    public void testRelative() {
        Path p1 = Path.path("test.ettrema.com:8080");
        assertEquals( 1,p1.getLength());

        Path path = Path.path("b/c");
        assertTrue(path.isRelative());
    }

    @Test
    public void testDoubleSlash() {
        Path p = Path.path("/a//b/c");
        assertEquals("/a/b/c", p.toString());
    }
}
