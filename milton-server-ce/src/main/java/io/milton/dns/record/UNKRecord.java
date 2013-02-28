/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package io.milton.dns.record;

import io.milton.dns.Name;

import java.io.*;

/**
 * A class implementing Records of unknown and/or unimplemented types.  This
 * class can only be initialized using static Record initializers.
 *
 * @author Brian Wellington
 */

public class UNKRecord extends Record {

private static final long serialVersionUID = -4193583311594626915L;

private byte [] data;

UNKRecord() {}

Record
getObject() {
	return new UNKRecord();
}

void
rrFromWire(DNSInput in) throws IOException {
	data = in.readByteArray();
}

void
rdataFromString(Tokenizer st, Name origin) throws IOException {
	throw st.exception("invalid unknown RR encoding");
}

/** Converts this Record to the String "unknown format" */
String
rrToString() {
	return unknownToString(data);
}

/** Returns the contents of this record. */
public byte []
getData() { 
	return data;
}

void
rrToWire(DNSOutput out, Compression c, boolean canonical) {
	out.writeByteArray(data);
}

}
