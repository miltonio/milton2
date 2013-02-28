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

package io.milton.http.webdav;

import junit.framework.TestCase;

public class TestMoveHandler extends TestCase {
    public TestMoveHandler() {
    }
    
    public void test() throws Exception {
        Dest dest;
        
        dest = new Dest("abc","/f1/f2/f3");
        assertEquals("abc",dest.host);
        assertEquals("/f1/f2",dest.url);
        assertEquals("f3",dest.name);

        dest = new Dest("abc","/f1/f2/f3/");
        assertEquals("abc",dest.host);
        assertEquals("/f1/f2",dest.url);
        assertEquals("f3",dest.name);
        
        dest = new Dest("abc","http://blah/f1/f2/f3");
        assertEquals("blah",dest.host);
        assertEquals("/f1/f2",dest.url);
        assertEquals("f3",dest.name);

        dest = new Dest("abc","http://blah:80/f1/f2/f3");
        assertEquals("blah",dest.host);
        assertEquals("/f1/f2",dest.url);
        assertEquals("f3",dest.name);        
        
        dest = new Dest("abc","/f1");
        assertEquals("abc",dest.host);
        assertEquals("/",dest.url);
        assertEquals("f1",dest.name);        
        
    }
}
