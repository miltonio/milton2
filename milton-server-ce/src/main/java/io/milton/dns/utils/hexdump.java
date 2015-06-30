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

package io.milton.dns.utils;

/**
 * A routine to produce a nice looking hex dump
 *
 * @author Brian Wellington
 */

public class hexdump {

private static final char [] hex = "0123456789ABCDEF".toCharArray();

/**
 * Dumps a byte array into hex format.
 * @param description If not null, a description of the data.
 * @param b The data to be printed.
 * @param offset The start of the data in the array.
 * @param length The length of the data in the array.
 */
public static String
dump(String description, byte [] b, int offset, int length) {
	StringBuilder sb = new StringBuilder();

	sb.append(length).append("b");
	if (description != null)
		sb.append(" (").append(description).append(")");
	sb.append(':');

	int prefixlen = sb.toString().length();
	prefixlen = (prefixlen + 8) & ~ 7;
	sb.append('\t');

	int perline = (80 - prefixlen) / 3;
	for (int i = 0; i < length; i++) {
		if (i != 0 && i % perline == 0) {
			sb.append('\n');
			for (int j = 0; j < prefixlen / 8 ; j++)
				sb.append('\t');
		}
		int value = (int)(b[i + offset]) & 0xFF;
		sb.append(hex[(value >> 4)]);
		sb.append(hex[(value & 0xF)]);
		sb.append(' ');
	}
	sb.append('\n');
	return sb.toString();
}

public static String
dump(String s, byte [] b) {
	return dump(s, b, 0, b.length);
}

}
