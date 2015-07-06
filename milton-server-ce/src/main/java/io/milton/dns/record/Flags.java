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
 * Constants and functions relating to flags in the DNS header.
 *
 * @author Brian Wellington
 */

public final class Flags {

	private static final Mnemonic flags = new Mnemonic("DNS Header Flag",					     Mnemonic.CASE_LOWER);

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
	return !((index >= 1 && index <= 4) || (index >= 12));
}

}
