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
package io.milton.httpclient.calsync.parse;

import io.milton.httpclient.parse.PropertyAccessor;
import java.lang.reflect.Method;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class TypeConverterTest extends TestCase {
    
    PropertyAccessor typeConverter;

    @Override
    protected void setUp() throws Exception {
        typeConverter = new PropertyAccessor();
    }
    
        
    public void test_GetString() throws Exception{
        MyCalendarEventBean bean = new MyCalendarEventBean();
        bean.setSummary("XXX");
        Method readMethod = bean.getClass().getMethod("getSummary");
        String v = typeConverter.get(bean, readMethod, String.class);
        assertEquals("XXX", v);
    }
    
    public void test_SetString() throws Exception{
        MyCalendarEventBean bean = new MyCalendarEventBean();
        bean.setSummary("XXX");
        Method writeMethod = bean.getClass().getMethod("setSummary", String.class);
        typeConverter.set(bean, writeMethod, "YYY");
        assertEquals("YYY", bean.getSummary());
    }    
}
