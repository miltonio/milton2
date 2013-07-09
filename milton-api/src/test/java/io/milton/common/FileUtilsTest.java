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

import io.milton.common.FileUtils;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class FileUtilsTest extends TestCase {
    
    public FileUtilsTest(String testName) {
        super(testName);
    }

    public static void testStripExtension() {
        assertEquals( "abc", FileUtils.stripExtension( "abc.def"));
        String s = FileUtils.stripExtension( "abc");
        assertEquals( "abc", s);
        assertEquals( "", FileUtils.stripExtension( ".def"));
        assertEquals( "abc.def", FileUtils.stripExtension( "abc.def.xxx"));
    }
    
    public void testGetExtension() {
        String s = FileUtils.getExtension("abc.txt");
        assertEquals("txt", s);
    }
    
    public void testGetExtension_NoPreExt() {
        String s = FileUtils.getExtension(".txt");
        assertEquals("txt", s);
    }    
    
    public void testGetExtension_NoExt() {
        String s = FileUtils.getExtension("abc");
        assertNull(s);
    }    
    
    public void testGetExtension_EndsWithDot() {
        String s = FileUtils.getExtension("abc.");
        assertEquals("", s);
    }        
    
    public void testGetExtension_Blank() {
        String s = FileUtils.getExtension("");
        assertNull(s);
    }        
    
    public void testGetExtension_Path() {
        String s = FileUtils.getExtension("/theme/../assets/fonts/Lato-Lig-webfont.eot");
        assertEquals("eot", s);
    }        
}
