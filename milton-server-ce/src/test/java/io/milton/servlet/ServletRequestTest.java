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

package io.milton.servlet;

import io.milton.servlet.ServletRequest;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

public class ServletRequestTest extends TestCase{
    public ServletRequestTest() {
    }
    
    public void test() {
        String qs = "Command=FileUpload&Type=Image&CurrentFolder=%2Fusers%2F";
        Map<String,String> map = new HashMap<String,String>();
        ServletRequest.parseQueryString(map,qs);
        assertEquals(3,map.size());
        check(map,"Command","FileUpload");
        check(map,"Type","Image");
        check(map,"CurrentFolder","/users/");
    }
    
    private void check(Map<String,String> map, String key, String expected) {
        String val = map.get(key);
        assertEquals(expected,val);
    }
}
