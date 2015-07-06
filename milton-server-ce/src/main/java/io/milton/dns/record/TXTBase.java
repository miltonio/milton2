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
import io.milton.dns.TextParseException;

import java.io.*;
import java.util.*;

/**
 * Implements common functionality for the many record types whose format
 * is a list of strings.
 *
 * @author Brian Wellington
 */

abstract class TXTBase extends Record {

private static final long serialVersionUID = -4319510507246305931L;

protected List strings;

protected
TXTBase() {}

protected
TXTBase(Name name, int type, int dclass, long ttl) {
	super(name, type, dclass, ttl);
}

protected
TXTBase(Name name, int type, int dclass, long ttl, List strings) {
	super(name, type, dclass, ttl);
	if (strings == null)
		throw new IllegalArgumentException("strings must not be null");
	this.strings = new ArrayList(strings.size());
	Iterator it = strings.iterator();
	try {
		while (it.hasNext()) {
			String s = (String) it.next();
			this.strings.add(byteArrayFromString(s));
		}
	}
	catch (TextParseException e) {
		throw new IllegalArgumentException(e.getMessage());
	}
}

protected
TXTBase(Name name, int type, int dclass, long ttl, String string) {
	this(name, type, dclass, ttl, Collections.singletonList(string));
}

void
rrFromWire(DNSInput in) throws IOException {
	strings = new ArrayList(2);
	while (in.remaining() > 0) {
		byte [] b = in.readCountedString();
		strings.add(b);
	}
}

void
rdataFromString(Tokenizer st, Name origin) throws IOException {
	strings = new ArrayList(2);
	while (true) {
		Tokenizer.Token t = st.get();
		if (!t.isString())
			break;
		try {
			strings.add(byteArrayFromString(t.value));
		}
		catch (TextParseException e) { 
			throw st.exception(e.getMessage());
		}

	}
	st.unget();
}

/** converts to a String */
String
rrToString() {
	StringBuilder sb = new StringBuilder();
	Iterator it = strings.iterator();
	while (it.hasNext()) {
		byte [] array = (byte []) it.next();
		sb.append(byteArrayToString(array, true));
		if (it.hasNext())
			sb.append(" ");
	}
	return sb.toString();
}

/**
 * Returns the text strings
 * @return A list of Strings corresponding to the text strings.
 */
public List
getStrings() {
	List list = new ArrayList(strings.size());
	for (int i = 0; i < strings.size(); i++)
		list.add(byteArrayToString((byte []) strings.get(i), false));
	return list;
}

/**
 * Returns the text strings
 * @return A list of byte arrays corresponding to the text strings.
 */
public List
getStringsAsByteArrays() {
	return strings;
}

void
rrToWire(DNSOutput out, Compression c, boolean canonical) {
	Iterator it = strings.iterator();
	while (it.hasNext()) {
		byte [] b = (byte []) it.next();
		out.writeCountedString(b);
	}
}

}
