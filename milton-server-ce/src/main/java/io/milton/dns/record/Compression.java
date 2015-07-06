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

private final Entry[] table;
private final boolean verbose = Options.check("verbosecompression");

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
