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
