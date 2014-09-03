/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package io.milton.dns.record;

import io.milton.dns.Name;

import java.io.*;

/**
 * A class implementing Records with no data; that is, records used in
 * the question section of messages and meta-records in dynamic update.
 *
 * @author Brian Wellington
 */

class EmptyRecord extends Record {

private static final long serialVersionUID = 3601852050646429582L;

EmptyRecord() {}

Record
getObject() {
	return new EmptyRecord();
}

void
rrFromWire(DNSInput in) throws IOException {
}

void
rdataFromString(Tokenizer st, Name origin) throws IOException {
}

String
rrToString() {
	return "";
}

void
rrToWire(DNSOutput out, Compression c, boolean canonical) {
}

}
