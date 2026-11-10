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

import io.milton.http.webdav.PropertiesRequest.Property;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import static org.easymock.EasyMock.*;

/**
 *
 * @author brad
 */
public class MsPropFindRequestFieldParserTest {

    MsPropFindRequestFieldParser fieldParser;
    PropFindRequestFieldParser wrapped;
    InputStream request;
    Set<QName> set;

    @BeforeEach
    protected void setUp() throws Exception {
        request = createMock( InputStream.class );
        wrapped = createMock( PropFindRequestFieldParser.class );
        fieldParser = new MsPropFindRequestFieldParser( wrapped );
        set = new HashSet<>();
    }

    @Test
    public void testGetRequestedFields_WrappedReturnsFields() {
        set.add( new QName( "a" ) );
        PropertiesRequest res = new PropertiesRequest( toProperties(set) );
        expect( wrapped.getRequestedFields( request ) ).andReturn( res );
        replay( wrapped );
        PropertiesRequest actual = fieldParser.getRequestedFields( request );

        verify( wrapped );
        assertSame( res, actual );
    }

    @Test
    public void testGetRequestedFields_WrappedReturnsNothing() {
        PropertiesRequest res = new PropertiesRequest( toProperties(set) );
        expect( wrapped.getRequestedFields( request ) ).andReturn( res );
        replay( wrapped );
        PropertiesRequest actual = fieldParser.getRequestedFields( request );

        verify( wrapped );
        assertEquals( 7, actual.getNames().size() );
    }
	
	private Set<Property> toProperties(Set<QName> set) {
		Set<Property> props = new HashSet<>();
		for(QName n : set ) {
			props.add(new Property(n, null));
		}
		return props;
	}	
}
