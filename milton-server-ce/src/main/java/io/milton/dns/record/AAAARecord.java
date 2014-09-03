/*
 * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)
 * 
 * Copied from the DnsJava project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.milton.dns.record;

import io.milton.dns.Address;
import io.milton.dns.Name;

import java.io.*;
import java.net.*;

/**
 * IPv6 Address Record - maps a domain name to an IPv6 address
 *
 * @author Brian Wellington
 */

public class AAAARecord extends Record {

private static final long serialVersionUID = -4588601512069748050L;

private InetAddress address;

AAAARecord() {}

Record
getObject() {
	return new AAAARecord();
}

/**
 * Creates an AAAA Record from the given data
 * @param address The address suffix
 */
public
AAAARecord(Name name, int dclass, long ttl, InetAddress address) {
	super(name, Type.AAAA, dclass, ttl);
	if (Address.familyOf(address) != Address.IPv6)
		throw new IllegalArgumentException("invalid IPv6 address");
	this.address = address;
}

void
rrFromWire(DNSInput in) throws IOException {
	address = InetAddress.getByAddress(in.readByteArray(16));
}

void
rdataFromString(Tokenizer st, Name origin) throws IOException {
	address = st.getAddress(Address.IPv6);
}

/** Converts rdata to a String */
String
rrToString() {
	return address.getHostAddress();
}

/** Returns the address */
public InetAddress
getAddress() {
	return address;
}

void
rrToWire(DNSOutput out, Compression c, boolean canonical) {
	out.writeByteArray(address.getAddress());
}

}
