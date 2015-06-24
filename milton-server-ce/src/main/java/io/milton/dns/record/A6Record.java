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

import io.milton.dns.Address;
import io.milton.dns.Name;

import java.io.*;
import java.net.*;

/**
 * A6 Record - maps a domain name to an IPv6 address (experimental)
 *
 * @author Brian Wellington
 */

public class A6Record extends Record {

private static final long serialVersionUID = -8815026887337346789L;

private int prefixBits;
private InetAddress suffix;
private Name prefix;

A6Record() {}

Record
getObject() {
	return new A6Record();
}

/**
 * Creates an A6 Record from the given data
 * @param prefixBits The number of bits in the address prefix
 * @param suffix The address suffix
 * @param prefix The name of the prefix
 */
public
A6Record(Name name, int dclass, long ttl, int prefixBits,
	 InetAddress suffix, Name prefix)
{
	super(name, Type.A6, dclass, ttl);
	this.prefixBits = checkU8("prefixBits", prefixBits);
	if (suffix != null && Address.familyOf(suffix) != Address.IPv6)
		throw new IllegalArgumentException("invalid IPv6 address");
	this.suffix = suffix;
	if (prefix != null)
		this.prefix = checkName("prefix", prefix);
}

void
rrFromWire(DNSInput in) throws IOException {
	prefixBits = in.readU8();
	int suffixbits = 128 - prefixBits;
	int suffixbytes = (suffixbits + 7) / 8;
	if (prefixBits < 128) {
		byte [] bytes = new byte[16];
		in.readByteArray(bytes, 16 - suffixbytes, suffixbytes);
		suffix = InetAddress.getByAddress(bytes);
	}
	if (prefixBits > 0)
		prefix = new Name(in);
}

void
rdataFromString(Tokenizer st, Name origin) throws IOException {
	prefixBits = st.getUInt8();
	if (prefixBits > 128) {
		throw st.exception("prefix bits must be [0..128]");
	} else if (prefixBits < 128) {
		String s = st.getString();
		try {
			suffix = Address.getByAddress(s, Address.IPv6);
		}
		catch (UnknownHostException e) {
			throw st.exception("invalid IPv6 address: " + s);
		}
	}
	if (prefixBits > 0)
		prefix = st.getName(origin);
}

/** Converts rdata to a String */
String
rrToString() {
	StringBuilder sb = new StringBuilder();
	sb.append(prefixBits);
	if (suffix != null) {
		sb.append(" ");
		sb.append(suffix.getHostAddress());
	}
	if (prefix != null) {
		sb.append(" ");
		sb.append(prefix);
	}
	return sb.toString();
}

/** Returns the number of bits in the prefix */
public int
getPrefixBits() {
	return prefixBits;
}

/** Returns the address suffix */
public InetAddress
getSuffix() {
	return suffix;
}

/** Returns the address prefix */
public Name
getPrefix() {
	return prefix;
}

void
rrToWire(DNSOutput out, Compression c, boolean canonical) {
	out.writeU8(prefixBits);
	if (suffix != null) {
		int suffixbits = 128 - prefixBits;
		int suffixbytes = (suffixbits + 7) / 8;
		byte [] data = suffix.getAddress();
		out.writeByteArray(data, 16 - suffixbytes, suffixbytes);
	}
	if (prefix != null)
		prefix.toWire(out, null, canonical);
}

}
