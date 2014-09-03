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

package io.milton.http;

import io.milton.http.LockTimeout;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class TestLockTimeout extends TestCase {
    public void testNull() {
        LockTimeout t = LockTimeout.parseTimeout((String)null);
        assertNotNull(t);
        assertNull(t.getSeconds());
    }
    
    public void testEmpty() {
        LockTimeout t = LockTimeout.parseTimeout("");
        assertNotNull(t);
        assertNull(t.getSeconds());        
        
        t = LockTimeout.parseTimeout(" ");
        assertNotNull(t);
        assertNull(t.getSeconds());                
    }
    
    public void testSingleInfinite() {
        LockTimeout t = LockTimeout.parseTimeout("Infinite");
        assertNotNull(t);
        assertEquals((Object)Long.MAX_VALUE, t.getSeconds());        
    }

    public void testSingleSeconds() {
        LockTimeout t = LockTimeout.parseTimeout("Second-5");
        assertNotNull(t);
        assertEquals(new Long(5), t.getSeconds());        
    }
    
    public void testTwo() {
        LockTimeout t = LockTimeout.parseTimeout("Infinite, Second-5");
        assertNotNull(t);
        assertEquals((Object)Long.MAX_VALUE, t.getSeconds());        
        assertNotNull(t.getOtherSeconds());
        assertEquals(new Long(5), t.getOtherSeconds()[0]);
        
    }
    
    public void testMalformed() {
        LockTimeout t = LockTimeout.parseTimeout("Infinite Second5");
        assertNotNull(t);
        assertNull(t.getSeconds());
        assertNull(t.getOtherSeconds());
    }
}
