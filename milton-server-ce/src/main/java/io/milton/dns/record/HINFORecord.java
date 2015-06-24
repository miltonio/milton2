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

/**
 * Host Information - describes the CPU and OS of a host
 *
 * @author Brian Wellington
 */

public class HINFORecord extends Record {

private static final long serialVersionUID = -4732870630947452112L;
	
private byte [] cpu, os;

HINFORecord() {}

Record
getObject() {
	return new HINFORecord();
}

/**
 * Creates an HINFO Record from the given data
 * @param cpu A string describing the host's CPU
 * @param os A string describing the host's OS
 * @throws IllegalArgumentException One of the strings has invalid escapes
 */
public
HINFORecord(Name name, int dclass, long ttl, String cpu, String os) {
	super(name, Type.HINFO, dclass, ttl);
	try {
		this.cpu = byteArrayFromString(cpu);
		this.os = byteArrayFromString(os);
	}
	catch (TextParseException e) {
		throw new IllegalArgumentException(e.getMessage());
	}
}

void
rrFromWire(DNSInput in) throws IOException {
	cpu = in.readCountedString();
	os = in.readCountedString();
}

void
rdataFromString(Tokenizer st, Name origin) throws IOException {
	try {
		cpu = byteArrayFromString(st.getString());
		os = byteArrayFromString(st.getString());
	}
	catch (TextParseException e) {
		throw st.exception(e.getMessage());
	}
}

/**
 * Returns the host's CPU
 */
public String
getCPU() {
	return byteArrayToString(cpu, false);
}

/**
 * Returns the host's OS
 */
public String
getOS() {
	return byteArrayToString(os, false);
}

void
rrToWire(DNSOutput out, Compression c, boolean canonical) {
	out.writeCountedString(cpu);
	out.writeCountedString(os);
}

/**
 * Converts to a string
 */
String
rrToString() {
	StringBuilder sb = new StringBuilder();
	sb.append(byteArrayToString(cpu, true));
	sb.append(" ");
	sb.append(byteArrayToString(os, true));
	return sb.toString();
}

}
