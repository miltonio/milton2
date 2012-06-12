/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package io.milton.http.webdav;

import io.milton.http.webdav.DefaultPropPatchParser;
import io.milton.http.webdav.PropPatchRequestParser.ParseResult;
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
		ParseResult result = parser.getRequestedFields(new ByteArrayInputStream(XML_list_property.getBytes()));
		assertEquals(1, result.getFieldsToSet().size());
		String s = result.getFieldsToSet().values().iterator().next();
		System.out.println(s);
	}
}
