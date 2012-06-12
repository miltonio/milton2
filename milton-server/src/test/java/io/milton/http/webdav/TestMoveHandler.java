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

import junit.framework.TestCase;

public class TestMoveHandler extends TestCase {
    public TestMoveHandler() {
    }
    
    public void test() throws Exception {
        Dest dest;
        
        dest = new Dest("abc","/f1/f2/f3");
        assertEquals("abc",dest.host);
        assertEquals("/f1/f2",dest.url);
        assertEquals("f3",dest.name);

        dest = new Dest("abc","/f1/f2/f3/");
        assertEquals("abc",dest.host);
        assertEquals("/f1/f2",dest.url);
        assertEquals("f3",dest.name);
        
        dest = new Dest("abc","http://blah/f1/f2/f3");
        assertEquals("blah",dest.host);
        assertEquals("/f1/f2",dest.url);
        assertEquals("f3",dest.name);

        dest = new Dest("abc","http://blah:80/f1/f2/f3");
        assertEquals("blah",dest.host);
        assertEquals("/f1/f2",dest.url);
        assertEquals("f3",dest.name);        
        
        dest = new Dest("abc","/f1");
        assertEquals("abc",dest.host);
        assertEquals("/",dest.url);
        assertEquals("f1",dest.name);        
        
    }
}
