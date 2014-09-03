/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package io.milton.dns.record;


/**
 * Constants and functions relating to DNS opcodes
 *
 * @author Brian Wellington
 */

public final class Opcode {

/** A standard query */
public static final int QUERY		= 0;

/** An inverse query (deprecated) */
public static final int IQUERY		= 1;

/** A server status request (not used) */
public static final int STATUS		= 2;

/**
 * A message from a primary to a secondary server to initiate a zone transfer
 */
public static final int NOTIFY		= 4;

/** A dynamic update message */
public static final int UPDATE		= 5;

private static Mnemonic opcodes = new Mnemonic("DNS Opcode",
					       Mnemonic.CASE_UPPER);

static {
	opcodes.setMaximum(0xF);
	opcodes.setPrefix("RESERVED");
	opcodes.setNumericAllowed(true);

	opcodes.add(QUERY, "QUERY");
	opcodes.add(IQUERY, "IQUERY");
	opcodes.add(STATUS, "STATUS");
	opcodes.add(NOTIFY, "NOTIFY");
	opcodes.add(UPDATE, "UPDATE");
}

private
Opcode() {}

/** Converts a numeric Opcode into a String */
public static String
string(int i) {
	return opcodes.getText(i);
}

/** Converts a String representation of an Opcode into its numeric value */
public static int
value(String s) {
	return opcodes.getValue(s);
}

}
