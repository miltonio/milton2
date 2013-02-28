/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package io.milton.dns.record;

import io.milton.dns.Name;

/**
 * DNS Name Compression object.
 * @see Message
 * @see Name
 *
 * @author Brian Wellington
 */

public class Compression {

private static class Entry {
	Name name;
	int pos;
	Entry next;
}

private static final int TABLE_SIZE = 17;
private static final int MAX_POINTER = 0x3FFF;
private Entry [] table;
private boolean verbose = Options.check("verbosecompression");

/**
 * Creates a new Compression object.
 */
public
Compression() {
	table = new Entry[TABLE_SIZE];
}

/**
 * Adds a compression entry mapping a name to a position in a message.
 * @param pos The position at which the name is added.
 * @param name The name being added to the message.
 */
public void
add(int pos, Name name) {
	if (pos > MAX_POINTER)
		return;
	int row = (name.hashCode() & 0x7FFFFFFF) % TABLE_SIZE;
	Entry entry = new Entry();
	entry.name = name;
	entry.pos = pos;
	entry.next = table[row];
	table[row] = entry;
	if (verbose)
		System.err.println("Adding " + name + " at " + pos);
}

/**
 * Retrieves the position of the given name, if it has been previously
 * included in the message.
 * @param name The name to find in the compression table.
 * @return The position of the name, or -1 if not found.
 */
public int
get(Name name) {
	int row = (name.hashCode() & 0x7FFFFFFF) % TABLE_SIZE;
	int pos = -1;
	for (Entry entry = table[row]; entry != null; entry = entry.next) {
		if (entry.name.equals(name))
			pos = entry.pos;
	}
	if (verbose)
		System.err.println("Looking for " + name + ", found " + pos);
	return pos;
}

}
