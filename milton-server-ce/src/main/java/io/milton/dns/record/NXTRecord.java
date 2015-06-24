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

import io.milton.dns.Name;

import java.io.*;
import java.util.*;

/**
 * Next name - this record contains the following name in an ordered list
 * of names in the zone, and a set of types for which records exist for
 * this name.  The presence of this record in a response signifies a
 * failed query for data in a DNSSEC-signed zone. 
 *
 * @author Brian Wellington
 */

public class NXTRecord extends Record {

private static final long serialVersionUID = -8851454400765507520L;

private Name next;
private BitSet bitmap;

NXTRecord() {}

Record
getObject() {
	return new NXTRecord();
}

/**
 * Creates an NXT Record from the given data
 * @param next The following name in an ordered list of the zone
 * @param bitmap The set of type for which records exist at this name
*/
public
NXTRecord(Name name, int dclass, long ttl, Name next, BitSet bitmap) {
	super(name, Type.NXT, dclass, ttl);
	this.next = checkName("next", next);
	this.bitmap = bitmap;
}

void
rrFromWire(DNSInput in) throws IOException {
	next = new Name(in);
	bitmap = new BitSet();
	int bitmapLength = in.remaining();
	for (int i = 0; i < bitmapLength; i++) {
		int t = in.readU8();
		for (int j = 0; j < 8; j++)
			if ((t & (1 << (7 - j))) != 0)
				bitmap.set(i * 8 + j);
	}
}

void
rdataFromString(Tokenizer st, Name origin) throws IOException {
	next = st.getName(origin);
	bitmap = new BitSet();
	while (true) {
		Tokenizer.Token t = st.get();
		if (!t.isString())
			break;
		int typecode = Type.value(t.value, true);
		if (typecode <= 0 || typecode > 128)
			throw st.exception("Invalid type: " + t.value);
		bitmap.set(typecode);
	}
	st.unget();
}

/** Converts rdata to a String */
String
rrToString() {
	StringBuilder sb = new StringBuilder();
	sb.append(next);
	int length = bitmap.length();
	for (short i = 0; i < length; i++)
		if (bitmap.get(i)) {
			sb.append(" ");
			sb.append(Type.string(i));
		}
	return sb.toString();
}

/** Returns the next name */
public Name
getNext() {
	return next;
}

/** Returns the set of types defined for this name */
public BitSet
getBitmap() {
	return bitmap;
}

void
rrToWire(DNSOutput out, Compression c, boolean canonical) {
	next.toWire(out, null, canonical);
	int length = bitmap.length();
	for (int i = 0, t = 0; i < length; i++) {
		t |= (bitmap.get(i) ? (1 << (7 - i % 8)) : 0);
		if (i % 8 == 7 || i == length - 1) {
			out.writeU8(t);
			t = 0;
		}
	}
}

}
