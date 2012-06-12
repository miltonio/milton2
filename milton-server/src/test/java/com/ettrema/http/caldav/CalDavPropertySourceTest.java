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

package com.ettrema.http.caldav;

import java.net.URI;
import java.net.URISyntaxException;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class CalDavPropertySourceTest extends TestCase {

    public void testGetProperty() throws URISyntaxException {
        URI uri = new URI( "mailto:bernard@example.com" );
        URI[] arr = new URI[1];
        arr[0] = uri;
        if( arr instanceof URI[]) {
            System.out.println( "hiii" );
        }
    }

    public void testSetProperty() {
    }

    public void testGetPropertyMetaData() {
    }

    public void testClearProperty() {
    }

    public void testGetAllPropertyNames() {
    }
}
