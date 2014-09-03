/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package io.milton.dns.record;


/**
 * Constants and functions relating to DNS rcodes (error values)
 *
 * @author Brian Wellington
 */

public final class Rcode {

private static Mnemonic rcodes = new Mnemonic("DNS Rcode",
					      Mnemonic.CASE_UPPER);

private static Mnemonic tsigrcodes = new Mnemonic("TSIG rcode",
						  Mnemonic.CASE_UPPER);

/** No error */
public static final int NOERROR		= 0;

/** Format error */
public static final int FORMERR		= 1;

/** Server failure */
public static final int SERVFAIL	= 2;

/** The name does not exist */
public static final int NXDOMAIN	= 3;

/** The operation requested is not implemented */
public static final int NOTIMP		= 4;

/** Deprecated synonym for NOTIMP. */
public static final int NOTIMPL		= 4;

/** The operation was refused by the server */
public static final int REFUSED		= 5;

/** The name exists */
public static final int YXDOMAIN	= 6;

/** The RRset (name, type) exists */
public static final int YXRRSET		= 7;

/** The RRset (name, type) does not exist */
public static final int NXRRSET		= 8;

/** The requestor is not authorized to perform this operation */
public static final int NOTAUTH		= 9;

/** The zone specified is not a zone */
public static final int NOTZONE		= 10;

/* EDNS extended rcodes */
/** Unsupported EDNS level */
public static final int BADVERS		= 16;

/* TSIG/TKEY only rcodes */
/** The signature is invalid (TSIG/TKEY extended error) */
public static final int BADSIG		= 16;

/** The key is invalid (TSIG/TKEY extended error) */
public static final int BADKEY		= 17;

/** The time is out of range (TSIG/TKEY extended error) */
public static final int BADTIME		= 18;

/** The mode is invalid (TKEY extended error) */
public static final int BADMODE		= 19;

static {
	rcodes.setMaximum(0xFFF);
	rcodes.setPrefix("RESERVED");
	rcodes.setNumericAllowed(true);

	rcodes.add(NOERROR, "NOERROR");
	rcodes.add(FORMERR, "FORMERR");
	rcodes.add(SERVFAIL, "SERVFAIL");
	rcodes.add(NXDOMAIN, "NXDOMAIN");
	rcodes.add(NOTIMP, "NOTIMP");
	rcodes.addAlias(NOTIMP, "NOTIMPL");
	rcodes.add(REFUSED, "REFUSED");
	rcodes.add(YXDOMAIN, "YXDOMAIN");
	rcodes.add(YXRRSET, "YXRRSET");
	rcodes.add(NXRRSET, "NXRRSET");
	rcodes.add(NOTAUTH, "NOTAUTH");
	rcodes.add(NOTZONE, "NOTZONE");
	rcodes.add(BADVERS, "BADVERS");

	tsigrcodes.setMaximum(0xFFFF);
	tsigrcodes.setPrefix("RESERVED");
	tsigrcodes.setNumericAllowed(true);
	tsigrcodes.addAll(rcodes);

	tsigrcodes.add(BADSIG, "BADSIG");
	tsigrcodes.add(BADKEY, "BADKEY");
	tsigrcodes.add(BADTIME, "BADTIME");
	tsigrcodes.add(BADMODE, "BADMODE");
}

private
Rcode() {}

/** Converts a numeric Rcode into a String */
public static String
string(int i) {
	return rcodes.getText(i);
}

/** Converts a numeric TSIG extended Rcode into a String */
public static String
TSIGstring(int i) {
	return tsigrcodes.getText(i);
}

/** Converts a String representation of an Rcode into its numeric value */
public static int
value(String s) {
	return rcodes.getValue(s);
}

}
