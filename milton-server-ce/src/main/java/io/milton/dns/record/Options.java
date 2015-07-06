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

import java.util.*;

/**
 * Boolean options:<BR>
 * bindttl - Print TTLs in BIND format<BR>
 * multiline - Print records in multiline format<BR>
 * noprintin - Don't print the class of a record if it's IN<BR>
 * verbose - Turn on general debugging statements<BR>
 * verbosemsg - Print all messages sent or received by SimpleResolver<BR>
 * verbosecompression - Print messages related to name compression<BR>
 * verbosesec - Print messages related to signature verification<BR>
 * verbosecache - Print messages related to cache lookups<BR>
 * <BR>
 * Valued options:<BR>
 * tsigfudge=n - Sets the default TSIG fudge value (in seconds)<BR>
 * sig0validity=n - Sets the default SIG(0) validity period (in seconds)<BR>
 *
 * @author Brian Wellington
 */

public final class Options {

private static Map table;

static {
	try {
		refresh();
	}
	catch (SecurityException e) {
	}
}

private
Options() {}

public static void
refresh() {
	String s = System.getProperty("dnsjava.options");
	if (s != null) {
		StringTokenizer st = new StringTokenizer(s, ",");
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			int index = token.indexOf('=');
			if (index == -1)
				set(token);
			else {
				String option = token.substring(0, index);
				String value = token.substring(index + 1);
				set(option, value);
			}
		}
	}
}

/** Clears all defined options */
public static void
clear() {
	table = null;
}

/** Sets an option to "true" */
public static void
set(String option) {
	if (table == null)
		table = new HashMap();
	table.put(option.toLowerCase(), "true");
}

/** Sets an option to the the supplied value */
public static void
set(String option, String value) {
	if (table == null)
		table = new HashMap();
	table.put(option.toLowerCase(), value.toLowerCase());
}

/** Removes an option */
public static void
unset(String option) {
	if (table == null)
		return;
	table.remove(option.toLowerCase());
}

/** Checks if an option is defined */
public static boolean
check(String option) {
	if (table == null)
		return false;
	return (table.get(option.toLowerCase()) != null);
}

/** Returns the value of an option */
public static String
value(String option) {
	if (table == null)
		return null;
	return ((String)table.get(option.toLowerCase()));
}

/**
 * Returns the value of an option as an integer, or -1 if not defined.
 */
public static int
intValue(String option) {
	String s = value(option);
	if (s != null) {
		try {
			int val = Integer.parseInt(s);
			if (val > 0)
				return (val);
		}
		catch (NumberFormatException e) {
		}
	}
	return (-1);
}

}
