/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

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
	StringBuffer sb = new StringBuffer();
	sb.append(priority + " ");
	sb.append(weight + " ");
	sb.append(port + " ");
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
