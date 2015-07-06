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

import java.io.*;

/**
 * Routines for converting between Strings of hex-encoded data and arrays of
 * binary data.  This is not actually used by DNS.
 *
 * @author Brian Wellington
 */

public class base16 {

private static final String Base16 = "0123456789ABCDEF";

private
base16() {}

/**
 * Convert binary data to a hex-encoded String
 * @param b An array containing binary data
 * @return A String containing the encoded data
 */
public static String
toString(byte [] b) {
	ByteArrayOutputStream os = new ByteArrayOutputStream();

	for (int i = 0; i < b.length; i++) {
		short value = (short) (b[i] & 0xFF);
		byte high = (byte) (value >> 4);
		byte low = (byte) (value & 0xF);
		os.write(Base16.charAt(high));
		os.write(Base16.charAt(low));
	}
	return new String(os.toByteArray());
}

/**
 * Convert a hex-encoded String to binary data
 * @param str A String containing the encoded data
 * @return An array containing the binary data, or null if the string is invalid
 */
public static byte []
fromString(String str) {
	ByteArrayOutputStream bs = new ByteArrayOutputStream();
	byte [] raw = str.getBytes();
	for (int i = 0; i < raw.length; i++) {
		if (!Character.isWhitespace((char)raw[i]))
			bs.write(raw[i]);
	}
	byte [] in = bs.toByteArray();
	if (in.length % 2 != 0) {
		return null;
	}

	bs.reset();
	DataOutputStream ds = new DataOutputStream(bs);

	for (int i = 0; i < in.length; i += 2) {
		byte high = (byte) Base16.indexOf(Character.toUpperCase((char)in[i]));
		byte low = (byte) Base16.indexOf(Character.toUpperCase((char)in[i+1]));
		try {
			ds.writeByte((high << 4) + low);
		}
		catch (IOException e) {
		}
	}
	return bs.toByteArray();
}

}
