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
 * Routines for parsing BIND-style TTL values.  These values consist of
 * numbers followed by 1 letter units of time (W - week, D - day, H - hour,
 * M - minute, S - second).
 *
 * @author Brian Wellington
 */

public final class TTL {

public static final long MAX_VALUE = 0x7FFFFFFFL;

private
TTL() {}

public static void
check(long i) {
	if (i < 0 || i > MAX_VALUE)
		throw new InvalidTTLException(i);
}

/**
 * Parses a TTL-like value, which can either be expressed as a number or a
 * BIND-style string with numbers and units.
 * @param s The string representing the numeric value.
 * @param clamp Whether to clamp values in the range [MAX_VALUE + 1, 2^32 -1]
 * to MAX_VALUE.  This should be donw for TTLs, but not other values which
 * can be expressed in this format.
 * @return The value as a number of seconds
 * @throws NumberFormatException The string was not in a valid TTL format.
 */
public static long
parse(String s, boolean clamp) {
	if (s == null || s.length() == 0 || !Character.isDigit(s.charAt(0)))
		throw new NumberFormatException();
	long value = 0;
	long ttl = 0;
	for (int i = 0; i < s.length(); i++) {
		char c = s.charAt(i);
		long oldvalue = value;
		if (Character.isDigit(c)) {
			value = (value * 10) + Character.getNumericValue(c);
			if (value < oldvalue)
				throw new NumberFormatException();
		} else {
			switch (Character.toUpperCase(c)) {
				case 'W': value *= 7;
				case 'D': value *= 24;
				case 'H': value *= 60;
				case 'M': value *= 60;
				case 'S': break;
				default:  throw new NumberFormatException();
			}
			ttl += value;
			value = 0;
			if (ttl > 0xFFFFFFFFL)
				throw new NumberFormatException();
		}
	}
	if (ttl == 0)
		ttl = value;

	if (ttl > 0xFFFFFFFFL)
		throw new NumberFormatException();
	else if (ttl > MAX_VALUE && clamp)
		ttl = MAX_VALUE;
	return ttl;
}

/**
 * Parses a TTL, which can either be expressed as a number or a BIND-style
 * string with numbers and units.
 * @param s The string representing the TTL
 * @return The TTL as a number of seconds
 * @throws NumberFormatException The string was not in a valid TTL format.
 */
public static long
parseTTL(String s) {
	return parse(s, true);
}

public static String
format(long ttl) {
	TTL.check(ttl);
	StringBuilder sb = new StringBuilder();
	long secs, mins, hours, days, weeks;
	secs = ttl % 60;
	ttl /= 60;
	mins = ttl % 60;
	ttl /= 60;
	hours = ttl % 24;
	ttl /= 24;
	days = ttl % 7;
	ttl /= 7;
	weeks = ttl;
	if (weeks > 0)
		sb.append(weeks).append("W");
	if (days > 0)
		sb.append(days).append("D");
	if (hours > 0)
		sb.append(hours).append("H");
	if (mins > 0)
		sb.append(mins).append("M");
	if (secs > 0 || (weeks == 0 && days == 0 && hours == 0 && mins == 0))
		sb.append(secs).append("S");
	return sb.toString();
}

}
