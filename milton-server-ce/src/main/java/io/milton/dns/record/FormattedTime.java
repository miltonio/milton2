/*
 * Copied from the DnsJava project
 *
 * Copyright (c) 1998-2011, Brian Wellington.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package io.milton.dns.record;

/**
 * Routines for converting time values to and from YYYYMMDDHHMMSS format.
 *
 * @author Brian Wellington
 */

import io.milton.dns.TextParseException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public final class FormattedTime {

private static final NumberFormat w2, w4;

static {
	w2 = new DecimalFormat();
	w2.setMinimumIntegerDigits(2);

	w4 = new DecimalFormat();
	w4.setMinimumIntegerDigits(4);
	w4.setGroupingUsed(false);
}

private
FormattedTime() {}

/**
 * Converts a Date into a formatted string.
 * @param date The Date to convert.
 * @return The formatted string.
 */
public static String
format(Date date) {
	Calendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
	StringBuilder sb = new StringBuilder();

	c.setTime(date);
	sb.append(w4.format(c.get(Calendar.YEAR)));
	sb.append(w2.format(c.get(Calendar.MONTH)+1));
	sb.append(w2.format(c.get(Calendar.DAY_OF_MONTH)));
	sb.append(w2.format(c.get(Calendar.HOUR_OF_DAY)));
	sb.append(w2.format(c.get(Calendar.MINUTE)));
	sb.append(w2.format(c.get(Calendar.SECOND)));
	return sb.toString();
}

/**
 * Parses a formatted time string into a Date.
 * @param s The string, in the form YYYYMMDDHHMMSS.
 * @return The Date object.
 * @throws TextParseExcetption The string was invalid.
 */
public static Date
parse(String s) throws TextParseException {
	if (s.length() != 14) {
		throw new TextParseException("Invalid time encoding: " + s);
	}

	Calendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
	c.clear();
	try {
		int year = Integer.parseInt(s.substring(0, 4));
		int month = Integer.parseInt(s.substring(4, 6)) - 1;
		int date = Integer.parseInt(s.substring(6, 8));
		int hour = Integer.parseInt(s.substring(8, 10));
		int minute = Integer.parseInt(s.substring(10, 12));
		int second = Integer.parseInt(s.substring(12, 14));
		c.set(year, month, date, hour, minute, second);
	}
	catch (NumberFormatException e) {
		throw new TextParseException("Invalid time encoding: " + s);
	}
	return c.getTime();
}

}
