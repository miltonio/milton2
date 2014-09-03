/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package io.milton.dns.record;

/**
 * Routines for converting time values to and from YYYYMMDDHHMMSS format.
 *
 * @author Brian Wellington
 */

import io.milton.dns.TextParseException;

import java.util.*;
import java.text.*;

public final class FormattedTime {

private static NumberFormat w2, w4;

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
	StringBuffer sb = new StringBuffer();

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
