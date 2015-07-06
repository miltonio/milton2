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
 * Constants and functions relating to DNS opcodes
 *
 * @author Brian Wellington
 */

public final class Opcode {

/** A standard query */
public static final int QUERY		= 0;

/** An inverse query (deprecated) */
public static final int IQUERY		= 1;

/** A server status request (not used) */
public static final int STATUS		= 2;

/**
 * A message from a primary to a secondary server to initiate a zone transfer
 */
public static final int NOTIFY		= 4;

/** A dynamic update message */
public static final int UPDATE		= 5;

private static final Mnemonic opcodes = new Mnemonic("DNS Opcode", Mnemonic.CASE_UPPER);

static {
	opcodes.setMaximum(0xF);
	opcodes.setPrefix("RESERVED");
	opcodes.setNumericAllowed(true);

	opcodes.add(QUERY, "QUERY");
	opcodes.add(IQUERY, "IQUERY");
	opcodes.add(STATUS, "STATUS");
	opcodes.add(NOTIFY, "NOTIFY");
	opcodes.add(UPDATE, "UPDATE");
}

private
Opcode() {}

/** Converts a numeric Opcode into a String */
public static String
string(int i) {
	return opcodes.getText(i);
}

/** Converts a String representation of an Opcode into its numeric value */
public static int
value(String s) {
	return opcodes.getValue(s);
}

}
