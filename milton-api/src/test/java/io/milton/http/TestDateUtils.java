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
    

    public void testParseWebdavDate_LongFormat() throws DateParseException {
        Date dt = DateUtils.parseWebDavDate("Wed, 27 Jun 2012 02:08:54 GMT");
        System.out.println("testParseWebdavDate_LongFormat: " + dt);
    }    
    
    public void testParseWebdavDate_ExpectedFormat() throws DateParseException {
        Date dt = DateUtils.parseWebDavDate("2010-09-03T09:29:43Z");
        System.out.println("testParseWebdavDate_ExpectedFormat: " + dt);
    }    
        
}
