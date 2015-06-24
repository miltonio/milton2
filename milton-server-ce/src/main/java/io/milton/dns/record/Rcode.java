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
 * Constants and functions relating to DNS rcodes (error values)
 *
 * @author Brian Wellington
 */

public final class Rcode {

private static final Mnemonic rcodes = new Mnemonic("DNS Rcode", Mnemonic.CASE_UPPER);

private static final Mnemonic tsigrcodes = new Mnemonic("TSIG rcode", Mnemonic.CASE_UPPER);

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
