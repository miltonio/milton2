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

package io.milton.common;

import io.milton.common.Path;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class PathTest extends TestCase {
    
    public PathTest(String testName) {
        super(testName);
    }

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

    public void testSingle() {
        Path p = Path.path("abc");
        String s = p.toString();
        assertEquals("abc",s);
    }

    public void testStrip() {
        Path path = Path.path("/a/b/c");
        Path stripped = path.getStripFirst();
        String s = stripped.toString();
        System.out.println("s: " + s);
        assertEquals("/b/c",s);
    }

    public void testAbsolute() {
        Path path = Path.path("/a/b/c");
        assertEquals(false,path.isRelative());
    }

    public void testRelative() {
        Path p1 = Path.path("test.ettrema.com:8080");
        assertEquals( 1,p1.getLength());

        Path path = Path.path("b/c");
        assertEquals(true,path.isRelative());
    }
}
