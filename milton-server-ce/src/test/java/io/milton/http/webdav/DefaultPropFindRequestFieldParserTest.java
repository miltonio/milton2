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

import io.milton.http.webdav.DefaultPropFindRequestFieldParser;
import io.milton.http.webdav.PropertiesRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Set;
import javax.xml.namespace.QName;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class DefaultPropFindRequestFieldParserTest extends TestCase {

    DefaultPropFindRequestFieldParser fieldParser;

    String namespace = "http://ns.example.com/boxschema/";

    @Override
    protected void setUp() throws Exception {
        fieldParser = new DefaultPropFindRequestFieldParser();
    }



    public void testGetRequestedFields_SingleField() {
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?> " +
            "<D:propfind xmlns:D=\"DAV:\">" +
            "<D:prop xmlns:R=\"" + namespace + "\">" +
            "<R:author/> " +
            "</D:prop> " +
            "</D:propfind>";

        InputStream in = new ByteArrayInputStream( xml.getBytes());
        PropertiesRequest parseResult = fieldParser.getRequestedFields( in );
        Set<QName> set = parseResult.getNames();
        assertEquals( 1, set.size());
        QName qn = set.iterator().next();
        assertEquals( "http://ns.example.com/boxschema/", qn.getNamespaceURI());
        assertEquals( "author", qn.getLocalPart());
    }

    public void testGetRequestedFields_EmptyData() {
        String xml = "";

        InputStream in = new ByteArrayInputStream( xml.getBytes());
        PropertiesRequest parseResult = fieldParser.getRequestedFields( in );
        Set<QName> set = parseResult.getNames();
        assertEquals( 0, set.size());
    }	
}
