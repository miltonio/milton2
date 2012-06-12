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

package info.ineighborhood.cardme.util;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Copyright (c) 2004, Neighborhood Technologies
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * Neither the name of Neighborhood Technologies nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * 
 * @author George El-Haddad
 * <br/>
 * Sep 21, 2006
 * 
 * <p>Utility class to help with ISO related formatting.</p>
 */
public final class ISOUtils {

	public static final String ISO8601_UTC_TIME_BASIC_REGEX = "\\d\\d\\d\\d\\d\\d\\d\\dT\\d\\d\\d\\d\\d\\dZ";
	public static final String ISO8601_UTC_TIME_EXTENDED_REGEX = "\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d\\:\\d\\d\\:\\d\\dZ";
	public static final String ISO8601_TIME_EXTENDED_REGEX = "\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d\\:\\d\\d\\:\\d\\d[-\\+]\\d\\d\\:\\d\\d";
	
	public static final String ISO8601_DATE_BASIC_REGEX = "\\d\\d\\d\\d\\d\\d\\d\\d";
	public static final String ISO8601_DATE_EXTENDED_REGEX = "\\d\\d\\d\\d-\\d\\d-\\d\\d";
	
	public static final String ISO8601_TIMEZONE_BASIC_REGEX = "-?\\d{3,4}";
	public static final String ISO8601_TIMEZONE_EXTENDED_REGEX = "-?\\d{1,2}:\\d\\d";
	
	private ISOUtils() {

	}

	/**
	 * <p>Builds a ISO-8601 UTC time formatted string. The format can either be
	 * basic or extended. Depending on the format parameter it will return it in
	 * basic or extended format.</p>
	 * 
	 * @param time
	 * @param format
	 * @return {@link String}
	 */
	public static String toISO8601_UTC_Time(Calendar time, ISOFormat format)
	{
		StringBuilder builder = new StringBuilder();
		switch (format)
		{
			case ISO8601_BASIC:
			{
				builder.append(toISO8601_Date(time, format));
				builder.append("T");
				paddTwoDigits(builder, time.get(Calendar.HOUR_OF_DAY));
				paddTwoDigits(builder, time.get(Calendar.MINUTE));
				paddTwoDigits(builder, time.get(Calendar.SECOND));
				break;
			}
	
			case ISO8601_EXTENDED:
			{
				builder.append(toISO8601_Date(time, format));
				builder.append("T");
				paddTwoDigits(builder, time.get(Calendar.HOUR_OF_DAY));
				builder.append(":");
				paddTwoDigits(builder, time.get(Calendar.MINUTE));
				builder.append(":");
				paddTwoDigits(builder, time.get(Calendar.SECOND));
				builder.append("Z");
				break;
			}
		}

		return builder.toString();
	}


	/**
	 * <p>Given a calendar object and a specific ISO format it will construct an
	 * ISO8601 calendar date string. Depending on the format parameter it will
	 * return it in basic or extended format.</p>
	 * 
	 * @param date
	 * @param format
	 * @return {@link String}
	 */
	public static String toISO8601_Date(Calendar date, ISOFormat format)
	{
		StringBuilder builder = new StringBuilder();
		switch (format)
		{
			case ISO8601_BASIC:
			{
				builder.append(date.get(Calendar.YEAR));
				paddTwoDigits(builder, date.get(Calendar.MONTH)+1);
				paddTwoDigits(builder, date.get(Calendar.DAY_OF_MONTH));
				break;
			}
	
			case ISO8601_EXTENDED:
			{
				builder.append(date.get(Calendar.YEAR));
				builder.append("-");
				paddTwoDigits(builder, date.get(Calendar.MONTH)+1);
				builder.append("-");
				paddTwoDigits(builder, date.get(Calendar.DAY_OF_MONTH));
				break;
			}
		}

		return builder.toString();
	}

	/**
	 * <p>Returns a TimeZone object as a ISO-8601 format. This can be either basic
	 * or extended format. Depending on the format parameter it will return it
	 * in basic or extended format.</p>
	 * 
	 * @param timeZone
	 * @param format
	 * @return {@link String}
	 */
	public static String toISO8601_TimeZone(TimeZone timeZone, ISOFormat format)
	{
		StringBuilder sb = new StringBuilder();
		double offset = (((timeZone.getRawOffset() / 1000) / 60) / 60);
		String sOffset = String.valueOf(offset);
		
		if (sOffset.indexOf(".") != -1) {
			int num = Integer.parseInt(sOffset.substring(sOffset.indexOf(".") + 1));
			if (num > 4) {
				sb.append(sOffset.substring(0, sOffset.indexOf(".")));
				switch (format)
				{
					case ISO8601_BASIC:
					{
						sb.append("30");
						break;
					}
					
					case ISO8601_EXTENDED:
					{
						sb.append(":30");
						break;
					}
				}
			}
			else {
				sb.append(sOffset.substring(0, sOffset.indexOf(".")));

				switch (format)
				{
					case ISO8601_BASIC:
					{
						sb.append("00");
						break;
					}
					
					case ISO8601_EXTENDED:
					{
						sb.append(":00");
						break;
					}
				}
			}
		}
		else {
			sb.append(sOffset);
			
			switch (format)
			{
				case ISO8601_BASIC:
				{
					sb.append("00");
					break;
				}
				
				case ISO8601_EXTENDED:
				{
					sb.append(":00");
					break;
				}
			}
		}

		return sb.toString();
	}
	
	
	/**
	 * <p>Pads a number with an extra zero if it is less than 10.</p>
	 * 
	 * @param sb
	 * @param number
	 */
	private static void paddTwoDigits(StringBuilder sb, int number) {
		if(number < 10) {
			sb.append("0");
			sb.append(number);
		}
		else {
			sb.append(number);
		}
	}
	
//	/**
//	 * <p>Un-comment this for testing purposes.</p>
//	 * 
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		
//		System.out.println("Test Current Date/Time");
//		System.out.println("ISO Date");
//		System.out.println(toISO8601_Date(Calendar.getInstance(), ISOFormat.ISO8601_BASIC));
//		System.out.println(toISO8601_Date(Calendar.getInstance(), ISOFormat.ISO8601_EXTENDED));
//		System.out.println();
//		
//		System.out.println("ISO UTC Time");
//		System.out.println(toISO8601_UTC_Time(Calendar.getInstance(), ISOFormat.ISO8601_BASIC));
//		System.out.println(toISO8601_UTC_Time(Calendar.getInstance(), ISOFormat.ISO8601_EXTENDED));
//		System.out.println();
//		
//		TimeZone tz = TimeZone.getDefault();
//		tz.setRawOffset(((4*1000)*60)*60);	//Offset it four hours somewhere
//		
//		System.out.println("ISO Timezone");
//		System.out.println(toISO8601_TimeZone(tz, ISOFormat.ISO8601_BASIC));
//		System.out.println(toISO8601_TimeZone(tz, ISOFormat.ISO8601_EXTENDED));
//		System.out.println();
//		
//		System.out.println("ISO Timezone");
//		System.out.println(toISO8601_TimeZone(TimeZone.getDefault(), ISOFormat.ISO8601_BASIC));
//		System.out.println(toISO8601_TimeZone(TimeZone.getDefault(), ISOFormat.ISO8601_EXTENDED));
//		System.out.println();
//		
//		System.out.println("Test Single Digit Date/Time");
//		Calendar cal = Calendar.getInstance();
//		cal.clear();
//		cal.set(Calendar.YEAR, 2006);
//		cal.set(Calendar.MONTH, Calendar.JANUARY);
//		cal.set(Calendar.DAY_OF_MONTH, 1);
//		cal.set(Calendar.HOUR_OF_DAY, 1);
//		cal.set(Calendar.MINUTE, 1);
//		cal.set(Calendar.SECOND, 1);
//		
//		System.out.println("ISO Date");
//		System.out.println(toISO8601_Date(cal, ISOFormat.ISO8601_BASIC));
//		System.out.println(toISO8601_Date(cal, ISOFormat.ISO8601_EXTENDED));
//		System.out.println();
//		
//		System.out.println("ISO UTC Time");
//		System.out.println(toISO8601_UTC_Time(cal, ISOFormat.ISO8601_BASIC));
//		System.out.println(toISO8601_UTC_Time(cal, ISOFormat.ISO8601_EXTENDED));
//		System.out.println();
//	}
}
