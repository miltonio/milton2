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

import io.milton.http.webdav.PropFindRequestFieldParser;
import io.milton.http.webdav.MsPropFindRequestFieldParser;
import io.milton.http.webdav.PropertiesRequest.Property;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import junit.framework.TestCase;

import static org.easymock.classextension.EasyMock.*;

/**
 *
 * @author brad
 */
public class MsPropFindRequestFieldParserTest extends TestCase {

    MsPropFindRequestFieldParser fieldParser;
    PropFindRequestFieldParser wrapped;
    InputStream request;
    Set<QName> set;

    @Override
    protected void setUp() throws Exception {
        request = createMock( InputStream.class );
        wrapped = createMock( PropFindRequestFieldParser.class );
        fieldParser = new MsPropFindRequestFieldParser( wrapped );
        set = new HashSet<QName>();
    }

    public void testGetRequestedFields_WrappedReturnsFields() {
        set.add( new QName( "a" ) );
        PropertiesRequest res = new PropertiesRequest( toProperties(set) );
        expect( wrapped.getRequestedFields( request ) ).andReturn( res );
        replay( wrapped );
        PropertiesRequest actual = fieldParser.getRequestedFields( request );

        verify( wrapped );
        assertSame( res, actual );
    }

    public void testGetRequestedFields_WrappedReturnsNothing() {
        PropertiesRequest res = new PropertiesRequest( toProperties(set) );
        expect( wrapped.getRequestedFields( request ) ).andReturn( res );
        replay( wrapped );
        PropertiesRequest actual = fieldParser.getRequestedFields( request );

        verify( wrapped );
        assertEquals( 7, actual.getNames().size() );
    }
	
	private Set<Property> toProperties(Set<QName> set) {
		Set<Property> props = new HashSet<Property>();
		for(QName n : set ) {
			props.add(new Property(n, null));
		}
		return props;
	}	
}
