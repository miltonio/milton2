/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package io.milton.dns.record;


/**
 * Constants and functions relating to flags in the DNS header.
 *
 * @author Brian Wellington
 */

public final class Flags {

private static Mnemonic flags = new Mnemonic("DNS Header Flag",
					     Mnemonic.CASE_LOWER);

/** query/response */
public static final byte QR		= 0;

/** authoritative answer */
public static final byte AA		= 5;

/** truncated */
public static final byte TC		= 6;

/** recursion desired */
public static final byte RD		= 7;

/** recursion available */
public static final byte RA		= 8;

/** authenticated data */
public static final byte AD		= 10;

/** (security) checking disabled */
public static final byte CD		= 11;

/** dnssec ok (extended) */
public static final int DO		= ExtendedFlags.DO;

static {
	flags.setMaximum(0xF);
	flags.setPrefix("FLAG");
	flags.setNumericAllowed(true);

	flags.add(QR, "qr");
	flags.add(AA, "aa");
	flags.add(TC, "tc");
	flags.add(RD, "rd");
	flags.add(RA, "ra");
	flags.add(AD, "ad");
	flags.add(CD, "cd");
}

private
Flags() {}

/** Converts a numeric Flag into a String */
public static String
string(int i) {
	return flags.getText(i);
}

/** Converts a String representation of an Flag into its numeric value */
public static int
value(String s) {
	return flags.getValue(s);
}

/**
 * Indicates if a bit in the flags field is a flag or not.  If it's part of
 * the rcode or opcode, it's not.
 */
public static boolean
isFlag(int index) {
	flags.check(index);
	if ((index >= 1 && index <= 4) || (index >= 12))
		return false;
	return true;
}

}
