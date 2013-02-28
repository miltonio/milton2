/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

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
	StringBuffer sb = new StringBuffer();
	sb.append(byteArrayToString(cpu, true));
	sb.append(" ");
	sb.append(byteArrayToString(os, true));
	return sb.toString();
}

}
