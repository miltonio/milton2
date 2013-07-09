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

import io.milton.http.webdav.DefaultPropPatchParser;
import io.milton.http.webdav.PropPatchParseResult;
import java.io.ByteArrayInputStream;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class DefaultPropPatchParserTest extends TestCase {
	
	private String XML_list_property = "<D:propertyupdate xmlns:D=\"DAV:\"  xmlns:Z=\"http://ns.example.com/standards/z39.50/\">\n" +
"<D:set>\n" +
"<D:prop>\n" +
"<Z:Authors>\n" +
"<Z:Author>Jim Whitehead</Z:Author>\n" +
"<Z:Author>Roy Fielding</Z:Author>\n" +
"</Z:Authors>\n" +
"</D:prop>\n" +
"</D:set>\n" +
"</D:propertyupdate>";
	
	public DefaultPropPatchParserTest(String testName) {
		super(testName);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testGetRequestedFields() {
		System.out.println(XML_list_property);
		DefaultPropPatchParser parser = new DefaultPropPatchParser();
		PropPatchParseResult result = parser.getRequestedFields(new ByteArrayInputStream(XML_list_property.getBytes()));
		assertEquals(1, result.getFieldsToSet().size());
		String s = result.getFieldsToSet().values().iterator().next();
		System.out.println(s);
	}
}
