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
 * Server Selection Record  - finds hosts running services in a domain.  An
 * SRV record will normally be named _&lt;service&gt;._&lt;protocol&gt;.domain
 * - examples would be _sips._tcp.example.org (for the secure SIP protocol) and
 * _http._tcp.example.com (if HTTP used SRV records)
 *
 * @author Brian Wellington
 */

public class SRVRecord extends Record {

private static final long serialVersionUID = -3886460132387522052L;

private int priority, weight, port;
private Name target;

SRVRecord() {}

Record
getObject() {
	return new SRVRecord();
}

/**
 * Creates an SRV Record from the given data
 * @param priority The priority of this SRV.  Records with lower priority
 * are preferred.
 * @param weight The weight, used to select between records at the same
 * priority.
 * @param port The TCP/UDP port that the service uses
 * @param target The host running the service
 */
public
SRVRecord(Name name, int dclass, long ttl, int priority,
	  int weight, int port, Name target)
{
	super(name, Type.SRV, dclass, ttl);
	this.priority = checkU16("priority", priority);
	this.weight = checkU16("weight", weight);
	this.port = checkU16("port", port);
	this.target = checkName("target", target);
}

void
rrFromWire(DNSInput in) throws IOException {
	priority = in.readU16();
	weight = in.readU16();
	port = in.readU16();
	target = new Name(in);
}

void
rdataFromString(Tokenizer st, Name origin) throws IOException {
	priority = st.getUInt16();
	weight = st.getUInt16();
	port = st.getUInt16();
	target = st.getName(origin);
}

/** Converts rdata to a String */
String
rrToString() {
	StringBuilder sb = new StringBuilder();
	sb.append(priority).append(" ");
	sb.append(weight).append(" ");
	sb.append(port).append(" ");
	sb.append(target);
	return sb.toString();
}

/** Returns the priority */
public int
getPriority() {
	return priority;
}

/** Returns the weight */
public int
getWeight() {
	return weight;
}

/** Returns the port that the service runs on */
public int
getPort() {
	return port;
}

/** Returns the host running that the service */
public Name
getTarget() {
	return target;
}

void
rrToWire(DNSOutput out, Compression c, boolean canonical) {
	out.writeU16(priority);
	out.writeU16(weight);
	out.writeU16(port);
	target.toWire(out, null, canonical);
}

public Name
getAdditionalName() {
	return target;
}

}
