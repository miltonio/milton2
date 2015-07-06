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

/**
 * Next SECure name - this record contains the following name in an
 * ordered list of names in the zone, and a set of types for which
 * records exist for this name.  The presence of this record in a response
 * signifies a negative response from a DNSSEC-signed zone.
 *
 * This replaces the NXT record.
 *
 * @author Brian Wellington
 * @author David Blacka
 */

public class NSECRecord extends Record {

private static final long serialVersionUID = -5165065768816265385L;

private Name next;
private TypeBitmap types;

NSECRecord() {}

Record
getObject() {
	return new NSECRecord();
}

/**
 * Creates an NSEC Record from the given data.
 * @param next The following name in an ordered list of the zone
 * @param types An array containing the types present.
 */
public
NSECRecord(Name name, int dclass, long ttl, Name next, int [] types) {
	super(name, Type.NSEC, dclass, ttl);
	this.next = checkName("next", next);
	for (int i = 0; i < types.length; i++) {
		Type.check(types[i]);
	}
	this.types = new TypeBitmap(types);
}

void
rrFromWire(DNSInput in) throws IOException {
	next = new Name(in);
	types = new TypeBitmap(in);
}

void
rrToWire(DNSOutput out, Compression c, boolean canonical) {
	// Note: The next name is not lowercased.
	next.toWire(out, null, false);
	types.toWire(out);
}

void
rdataFromString(Tokenizer st, Name origin) throws IOException {
	next = st.getName(origin);
	types = new TypeBitmap(st);
}

/** Converts rdata to a String */
String
rrToString()
{
	StringBuilder sb = new StringBuilder();
	sb.append(next);
	if (!types.empty()) {
		sb.append(' ');
		sb.append(types.toString());
	}
	return sb.toString();
}

/** Returns the next name */
public Name
getNext() {
	return next;
}

/** Returns the set of types defined for this name */
public int []
getTypes() {
	return types.toArray();
}

/** Returns whether a specific type is in the set of types. */
public boolean
hasType(int type) {
	return types.contains(type);
}

}
