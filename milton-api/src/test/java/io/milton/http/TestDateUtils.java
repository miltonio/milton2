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

import io.milton.http.DateUtils.DateParseException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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

    public void test_FirefoxOSCaldavDate() throws ParseException, DateParseException {
        Date date = DateUtils.parseIcalDateTime("20140104T050000Z");
        System.out.println("formatted to: " + date);
    }


    public void test_iOSCaldavDate() throws ParseException, DateParseException {
        Date date = DateUtils.parseIcalDateTime("20131222T000000Z");
        System.out.println("formatted to: " + date);
    }

	public void test_parseIcalDateTime_old() throws ParseException, DateParseException
	{
		Date date;
		String dateToParse;
		dateToParse = "20140104T050000Z";
		date = DateUtils.parseIcalDateTime_old( dateToParse );
		assertFalse( this.checkDate( dateToParse, date ) );

		dateToParse = "20131222T000000Z";
		date = DateUtils.parseIcalDateTime_old( dateToParse );
		assertFalse( this.checkDate( dateToParse, date ) );

		dateToParse = "20140822T070136Z";
		date = DateUtils.parseIcalDateTime_old( dateToParse );
		assertFalse( this.checkDate( dateToParse, date ) );

		dateToParse = "20380119T031407Z";
		date = DateUtils.parseIcalDateTime_old( dateToParse );
		assertFalse( this.checkDate( dateToParse, date ) );
	}

	public void test_parseIcalDateTime() throws ParseException, DateParseException
	{
		Date date ;
		String dateToParse;
		dateToParse = "20140104T050000Z";
		date = DateUtils.parseIcalDateTime( dateToParse );
		assertTrue( this.checkDate( dateToParse, date ) );

		dateToParse = "20131222T000000Z";
		date = DateUtils.parseIcalDateTime( dateToParse );
		assertTrue( this.checkDate( dateToParse, date ) );

		dateToParse = "20140822T070136Z";
		date = DateUtils.parseIcalDateTime( dateToParse );
		assertTrue( this.checkDate(dateToParse, date));

		dateToParse = "20380119T031407Z";
		date = DateUtils.parseIcalDateTime( dateToParse );
		assertTrue( this.checkDate(dateToParse, date));
	}

	private boolean checkDate( String dateToParse, Date desiredDate ) throws DateParseException
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime( desiredDate );
		SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMdd'T'HHmmss'Z'" );
		sdf.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
		String checkedDate = sdf.format( cal.getTime() );
		System.out.println( "Checking dates : " + dateToParse + " =? " + checkedDate );
		return checkedDate.equalsIgnoreCase( dateToParse );
	}

    public void testParseNormal() throws DateParseException {
        Date dt = DateUtils.parseDate( "Sun, 28 Mar 2010 01:00:00 GMT");
        System.out.println( dt.getTime() );
        assertEquals(1269738000000L, dt.getTime());
    }

    /**
     * See http://www.ettrema.com:8080/browse/MIL-60
     *
     * @throws io.milton.http.DateUtils.DateParseException
     */
    public void testParseWithoutSeconds() throws DateParseException {
        Date dt = DateUtils.parseDate( "Sun, 28 Mar 2010 01:00 GMT");
        System.out.println( dt.getTime() );
        assertEquals(1269738000000L, dt.getTime());
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
