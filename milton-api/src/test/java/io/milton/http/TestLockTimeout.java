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

import io.milton.http.LockTimeout;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class TestLockTimeout extends TestCase {
    public void testNull() {
        LockTimeout t = LockTimeout.parseTimeout((String)null);
        assertNotNull(t);
        assertNull(t.getSeconds());
    }
    
    public void testEmpty() {
        LockTimeout t = LockTimeout.parseTimeout("");
        assertNotNull(t);
        assertNull(t.getSeconds());        
        
        t = LockTimeout.parseTimeout(" ");
        assertNotNull(t);
        assertNull(t.getSeconds());                
    }
    
    public void testSingleInfinite() {
        LockTimeout t = LockTimeout.parseTimeout("Infinite");
        assertNotNull(t);
        assertEquals((Object)Long.MAX_VALUE, t.getSeconds());        
    }

    public void testSingleSeconds() {
        LockTimeout t = LockTimeout.parseTimeout("Second-5");
        assertNotNull(t);
        assertEquals(new Long(5), t.getSeconds());        
    }
    
    public void testTwo() {
        LockTimeout t = LockTimeout.parseTimeout("Infinite, Second-5");
        assertNotNull(t);
        assertEquals((Object)Long.MAX_VALUE, t.getSeconds());        
        assertNotNull(t.getOtherSeconds());
        assertEquals(new Long(5), t.getOtherSeconds()[0]);
        
    }
    
    public void testMalformed() {
        LockTimeout t = LockTimeout.parseTimeout("Infinite Second5");
        assertNotNull(t);
        assertNull(t.getSeconds());
        assertNull(t.getOtherSeconds());
    }
}
