/*
 * Copyright (C) 2012 McEvoy Software Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.milton.httpclient;

import io.milton.common.Path;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class HostTest extends TestCase {

    public HostTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBuildUrl_Simple() {
        Host h = new Host("localhost", 8080, null, null, null);
        assertEquals("http://localhost:8080/", h.encodedUrl());
    }

    public void testBuildUrl_Secure() {
        Host h = new Host("localhost", 8080, null, null, null);
        h.setSecure(true);
        assertEquals("https://localhost:8080/", h.encodedUrl());
    }

    public void testBuildUrl_WithRootPath() {
        Host h = new Host("localhost","/a/", 8080, null, null, null, null);
        String actual = h.encodedUrl();
        assertEquals("http://localhost:8080/a/", actual);
    }

    public void testBuildUrl_WithUnencodedRootPath() {
        Host h = new Host("localhost","/a b/", 8080, null, null, null, null);
        String actual = h.encodedUrl();        
        assertEquals("http://localhost:8080/a%20b/", actual);
    }
    
    public void test_Href_WithUnencodedRootPath() {
        Host h = new Host("localhost","/a b/", 8080, null, null, null, null);
        System.out.println("actual1: " + h.href());
        assertEquals("http://localhost:8080/a b/", h.href());
    }
    public void test_Href() {
        Host h = new Host("localhost", 8080, null, null, null, null);
        assertEquals("http://localhost:8080/", h.href());
    }    
    public void test_Href_DefaultPort() {
        Host h = new Host("localhost", 80, null, null, null, null);
        assertEquals("http://localhost/", h.href());
    }       
    
//    public void testPut() throws Exception{
//        Host h = new Host("localhost", 8085, "admin", "password8", null, null);
//        h.setUseDigestForPreemptiveAuth(false);
//        byte[] arr = new byte[1024];
//        Path p = Path.path("/blobs/606afba38bd84e1838a4e2621076003338c4f6c8");
//        h.doPut(p, arr, "text/plain");
//    }
}
