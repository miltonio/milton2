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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class for parsing and formatting HTTP dates as used in cookies and
 * other headers. This class handles dates as defined by RFC 2616 section 3.3.1
 * as well as some other common non-standard formats.
 *
 * @author Christopher Brown
 * @author Michael Becke
 */
public class DateUtils {

    // 2005-03-30T05:18:33Z
    public static final String PATTERN_WEBDAV = "yyyy-MM-dd HH:mm:ss";
    /**
     * Used for response headers, and for modified date in propfind
     */
    public static final String PATTERN_RESPONSE_HEADER = "E, dd MMM yyyy HH:mm:ss z";
    //public static final String PATTERN_RESPONSE_HEADER = "E MMM yyyy H:m:s z";
    private static final ThreadLocal<DateFormat> thHeaderDateFormat = new ThreadLocal<>();
    /**
     * Date format pattern used to parse HTTP date headers in RFC 1123 format.
     */
    public static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

    /**
     * Like PATTERN_RFC1123 but with hyphens between the date components
     */
    public static final String PATTERN_RFC1123_HYPHENS = "EEE, dd-MMM-yyyy HH:mm:ss zzz";

    /**
     * Date format pattern used to parse HTTP date headers in RFC 1123 format.
     */
    public static final String PATTERN_RFC1123_NOSECS = "EEE, dd MMM yyyy HH:mm zzz";
    /**
     * Date format pattern used to parse HTTP date headers in RFC 1036 format.
     */
    public static final String PATTERN_RFC1036 = "EEEE, dd-MMM-yy HH:mm:ss zzz";
    /**
     * Date format pattern used to parse HTTP date headers in ANSI C
     * <code>asctime()</code> format.
     */
    public static final String PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy";
    /**
     * Another Date format pattern used to parse HTTP date headers in ANSI C
     * <code>asctime()</code> format.
     */
    public static final String PATTERN_ASCTIME2 = "EEE MMM yyyy HH:mm:ss zzz";
    private static final Collection<String> DEFAULT_PATTERNS = Arrays.asList(
            PATTERN_ASCTIME, PATTERN_ASCTIME2, PATTERN_RFC1036, PATTERN_RFC1123, PATTERN_RFC1123_NOSECS, PATTERN_WEBDAV);
    private static final Date DEFAULT_TWO_DIGIT_YEAR_START;

    static {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2000, Calendar.JANUARY, 1, 0, 0);
        DEFAULT_TWO_DIGIT_YEAR_START = calendar.getTime();
    }

    public static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    private static final Pattern DATE_VALUE = Pattern.compile(
            "(\\d{4,})(\\d\\d)(\\d\\d)"
                    + "(?:T([0-1]\\d|2[0-3])([0-5]\\d)([0-5]\\d)(Z)?)?");

    /**
     * Parse date in format: 2010-09-03T09:29:43Z
     *
     * @param s
     * @return
     */
    public static Date parseWebDavDate(String s) throws DateParseException {
        //2010-09-03T09:29:43Z
        if (!s.contains(" ")) {
            s = s.replace('Z', ' ');
            s = s.replace('T', ' ');
            s = s.trim();
        }
        return parseDate(s);
    }

    /**
     * 20140104T050000Z 20131222T000000Z
     *
     * @param s
     * @return
     */
    public static Date parseIcalDateTime(String s) throws DateParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return sdf.parse(s);
        } catch (ParseException e) {
            throw new DateParseException("Does not match regex: " + s);
        }
    }

    public static Date parseIcalDateTime_old(String s) throws DateParseException {
        Matcher m = DATE_VALUE.matcher(s);
        if (!m.matches()) {
            throw new DateParseException("Does not match regex: " + s);
        }
        int year = Integer.parseInt(m.group(1));
        int month = Integer.parseInt(m.group(2));
        int day = Integer.parseInt(m.group(3));
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);

        if (null != m.group(4)) { // hour
            int hour = Integer.parseInt(m.group(4));
            int minute = Integer.parseInt(m.group(5));
            int second = Integer.parseInt(m.group(6));
            boolean utc = null != m.group(7);
            cal.set(Calendar.HOUR, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, second);
        }
        return cal.getTime();

    }

    /**
     * Parses a date value. The formats used for parsing the date value are
     * retrieved from the default http params.
     *
     * @param dateValue the date value to parse
     * @return the parsed date
     * @throws DateParseException if the value could not be parsed using any of
     *                            the supported date formats
     */
    public static Date parseDate(String dateValue) throws DateParseException {
        return parseDate(dateValue, null, null);
    }

    /**
     * Parses the date value using the given date formats.
     *
     * @param dateValue   the date value to parse
     * @param dateFormats the date formats to use
     * @return the parsed date
     * @throws DateParseException if none of the dataFormats could parse the
     *                            dateValue
     */
    public static Date parseDate(String dateValue, Collection<String> dateFormats)
            throws DateParseException {
        return parseDate(dateValue, dateFormats, null);
    }

    /**
     * Parses the date value using the given date formats.
     *
     * @param dateValue   the date value to parse
     * @param dateFormats the date formats to use
     * @param startDate   During parsing, two digit years will be placed in the
     *                    range <code>startDate</code> to <code>startDate + 100 years</code>. This
     *                    value may be <code>null</code>. When <code>null</code> is given as a
     *                    parameter, year <code>2000</code> will be used.
     * @return the parsed date
     * @throws DateParseException if none of the dataFormats could parse the
     *                            dateValue
     */
    public static Date parseDate(
            String dateValue,
            Collection<String> dateFormats,
            Date startDate) throws DateParseException {

        if (dateValue == null) {
            throw new IllegalArgumentException("dateValue is null");
        }

        if (dateFormats == null) {
            dateFormats = DEFAULT_PATTERNS;
        }
        if (startDate == null) {
            startDate = DEFAULT_TWO_DIGIT_YEAR_START;
        }
        // trim single quotes around date if present
        // see issue #5279
        if (dateValue.length() > 1
                && dateValue.startsWith("'")
                && dateValue.endsWith("'")) {
            dateValue = dateValue.substring(1, dateValue.length() - 1);
        }

        SimpleDateFormat dateParser = null;

        for (String format : dateFormats) {
            if (dateParser == null) {
                dateParser = new SimpleDateFormat(format, Locale.US);
                dateParser.setTimeZone(TimeZone.getTimeZone("GMT"));
                dateParser.set2DigitYearStart(startDate);
            } else {
                dateParser.applyPattern(format);
            }
            try {
                return dateParser.parse(dateValue);
            } catch (ParseException pe) {
                // ignore this exception, we will try the next format
            }
        }

        // we were unable to parse the date
        throw new DateParseException("Unable to parse the date: " + dateValue);
    }

    public static String formatDate(Date date) {
        Calendar cal = Calendar.getInstance(GMT);
        cal.setTime(date);
        return formatDate(cal);
    }

    /**
     * @param cal
     * @return
     * @see #PATTERN_WEBDAV
     */
    public static String formatDate(Calendar cal) {
        // 2005-03-30T05:18:33Z
        //        log.debug(date.toString() + " -> " + s);
        return cal.get(Calendar.YEAR) + "" +
                '-' +
                pad2(cal.get(Calendar.MONTH) + 1) +
                '-' +
                pad2(cal.get(Calendar.DAY_OF_MONTH)) +
//        sb.append('-');
//        sb.append( pad2(cal.get(Calendar.DAY_OF_MONTH)) );
//        sb.append('-');
//        sb.append( pad2(cal.get(Calendar.MONTH)) );
                'T' +
                pad2(cal.get(Calendar.HOUR_OF_DAY)) +
                ':' +
                pad2(cal.get(Calendar.MINUTE)) +
                ':' +
                pad2(cal.get(Calendar.SECOND)) +
                'Z';
    }

    public static String formatForHeader(Date date) {
        DateFormat df = thHeaderDateFormat.get();
        if (df == null) {
            df = new SimpleDateFormat(DateUtils.PATTERN_RESPONSE_HEADER);
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            thHeaderDateFormat.set(df);
        }
        return df.format(date);
    }

    public static String pad2(int i) {
        if (i < 10) {
            return "0" + i;
        } else {
            return i + "";
        }
    }

    /**
     * Formats the given date according to the specified pattern. The pattern
     * must conform to that used by the {@link SimpleDateFormat simple date
     * format} class.
     *
     * @param date    The date to format.
     * @param pattern The pattern to use for formatting the date.
     * @return A formatted date string.
     * @throws IllegalArgumentException If the given date pattern is invalid.
     * @see SimpleDateFormat
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            throw new IllegalArgumentException("date is null");
        }
        if (pattern == null) {
            throw new IllegalArgumentException("pattern is null");
        }

        SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.US);
        formatter.setTimeZone(GMT);
        return formatter.format(date);
    }

    /**
     * Use the long date form required for MS windows explorer
     *
     * @param date
     * @return
     */
    public static String formatForWebDavModifiedDate(Date date) {
        return formatDate(date, PATTERN_RFC1123);
    }

    public static String formatForCookieExpiry(Date date) {
        return formatDate(date, PATTERN_RFC1123_HYPHENS);
    }

    /**
     * This class should not be instantiated.
     */
    private DateUtils() {
    }

    public static class DateParseException extends Exception {

        private static final long serialVersionUID = 4417696455000643370L;

        /**
         *
         */
        public DateParseException() {
            super();
        }

        /**
         * @param message the exception message
         */
        public DateParseException(String message) {
            super(message);
        }
    }
}
