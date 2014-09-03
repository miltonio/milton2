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


import io.milton.http.LockInfoSaxHandler;
import io.milton.http.webdav.PropFindSaxHandler;
import java.util.Map;

import junit.framework.TestCase;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import io.milton.http.LockInfo.LockScope;
import io.milton.http.LockInfo.LockType;
import javax.xml.namespace.QName;

public class TestSaxHandler extends TestCase {
    public void testPropFind() throws Exception{
        XMLReader reader = XMLReaderFactory.createXMLReader();
        PropFindSaxHandler handler = new PropFindSaxHandler();
        reader.setContentHandler(handler);
        reader.parse(new InputSource(PropFindSaxHandler.class.getResourceAsStream("/sample_prop_find.xml")));
        Map<QName,String> result = handler.getAttributes();
        assertEquals("httpd/unix-directory", result.get(new QName( "DAV:", "getcontenttype")));
        assertEquals("", result.get(new QName( "DAV:", "resourcetype")));
        assertEquals("Thu, 01 Jan 1970 00:00:00 GMT", result.get(new QName( "DAV:", "getlastmodified")));
        assertEquals("1970-01-01T00:00:00Z", result.get(new QName( "DAV:", "creationdate")));
    }
    public void testLockInfo() throws Exception{
        XMLReader reader = XMLReaderFactory.createXMLReader();
        LockInfoSaxHandler handler = new LockInfoSaxHandler();
        reader.setContentHandler(handler);
        reader.parse(new InputSource(LockInfoSaxHandler.class.getResourceAsStream("/sample_lockinfo.xml")));
        LockInfo result = handler.getInfo();
        assertEquals(result.scope,LockScope.EXCLUSIVE);
        assertEquals(result.type,LockType.WRITE);
    }
}
