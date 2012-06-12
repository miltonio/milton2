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

import io.milton.http.ServletRequest;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

public class ServletRequestTest extends TestCase{
    public ServletRequestTest() {
    }
    
    public void test() {
        String qs = "Command=FileUpload&Type=Image&CurrentFolder=%2Fusers%2F";
        Map<String,String> map = new HashMap<String,String>();
        ServletRequest.parseQueryString(map,qs);
        assertEquals(3,map.size());
        check(map,"Command","FileUpload");
        check(map,"Type","Image");
        check(map,"CurrentFolder","/users/");
    }
    
    private void check(Map<String,String> map, String key, String expected) {
        String val = map.get(key);
        assertEquals(expected,val);
    }
}
