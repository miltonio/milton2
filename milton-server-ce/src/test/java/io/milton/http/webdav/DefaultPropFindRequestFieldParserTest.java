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
