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

import io.milton.http.DateUtils;
import io.milton.http.DateUtils.DateParseException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import junit.framework.TestCase;

public class TestDateUtils extends TestCase {
    public TestDateUtils() {
    }
    
    public void test() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm");
        Date dt = sdf.parse("1-1-2007 19:03");        
        System.out.println("parsed: " + dt);
        String s = DateUtils.formatDate(dt);
        System.out.println("formatted to: " + s);
    }

    public void testParseNormal() throws DateParseException {
        Date dt = DateUtils.parseDate( "Sun, 28 Mar 2010 01:00:00 GMT");
        System.out.println( dt.getTime() );
        assertEquals( 1269738000000l, dt.getTime());
    }

    /**
     * See http://www.ettrema.com:8080/browse/MIL-60
     *
     * @throws com.bradmcevoy.http.DateUtils.DateParseException
     */
    public void testParseWithoutSeconds() throws DateParseException {
        Date dt = DateUtils.parseDate( "Sun, 28 Mar 2010 01:00 GMT");
        System.out.println( dt.getTime() );
        assertEquals( 1269738000000l, dt.getTime());
    }

    public void testParseHeaderFormat() throws DateParseException {
        Date dt = DateUtils.parseDate("2010-04-11 12:00:00");
        System.out.println("dt: " + dt);
    }
}
