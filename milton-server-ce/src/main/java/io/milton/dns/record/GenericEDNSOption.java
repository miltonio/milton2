/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */
package io.milton.dns.record;

import io.milton.dns.utils.base16;

import java.io.*;


/**
 * An EDNSOption with no internal structure.
 * 
 * @author Ming Zhou &lt;mizhou@bnivideo.com&gt;, Beaumaris Networks
 * @author Brian Wellington
 */
public class GenericEDNSOption extends EDNSOption {

private byte [] data;

GenericEDNSOption(int code) {
	super(code);
}

/**
 * Construct a generic EDNS option.
 * @param data The contents of the option.
 */
public 
GenericEDNSOption(int code, byte [] data) {
	super(code);
	this.data = Record.checkByteArrayLength("option data", data, 0xFFFF);
}

void 
optionFromWire(DNSInput in) throws IOException {
	data = in.readByteArray();
}

void 
optionToWire(DNSOutput out) {
	out.writeByteArray(data);
}

String 
optionToString() {
	return "<" + base16.toString(data) + ">";
}

}
