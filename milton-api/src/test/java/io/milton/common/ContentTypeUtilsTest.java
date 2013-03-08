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

import io.milton.common.ContentTypeUtils;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class ContentTypeUtilsTest extends TestCase {
    
    public ContentTypeUtilsTest(String testName) {
        super(testName);
    }

    public void testFindContentTypes_Doc() {
        assertEquals( "application/msword", ContentTypeUtils.findContentTypes( "abc.doc"));
    }

    public void testFindContentTypes_Html() {
        assertEquals( "text/html", ContentTypeUtils.findContentTypes( "abc.html"));
    }

    public void testFindContentTypes_Txt() {
        assertEquals( "text/plain", ContentTypeUtils.findContentTypes( "abc.txt"));
    }

    public void testfindAcceptableContentTypeForName() {
        String s = ContentTypeUtils.findAcceptableContentTypeForName("x.mpg", "video");
        assertNotNull(s);
        assertTrue(s.contains("video"));
        
        s = ContentTypeUtils.findAcceptableContentTypeForName("x.flv", "video");
        assertNotNull(s);
        assertTrue(s.contains("video"));
        
        // we always want to return our content type, even if not acceptable
        assertEquals("video/mpeg", ContentTypeUtils.findAcceptableContentTypeForName("x.mpg", "text"));
    }

    public void testFindContentTypes_File() {
    }

    public void testFindAcceptableContentType() {
    }

}
