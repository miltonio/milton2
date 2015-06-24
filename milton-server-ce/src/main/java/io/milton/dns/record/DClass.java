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


/**
 * Constants and functions relating to DNS classes.  This is called DClass
 * to avoid confusion with Class.
 *
 * @author Brian Wellington
 */

public final class DClass {

/** Internet */
public static final int IN		= 1;

/** Chaos network (MIT) */
public static final int CH		= 3;

/** Chaos network (MIT, alternate name) */
public static final int CHAOS		= 3;

/** Hesiod name server (MIT) */
public static final int HS		= 4;

/** Hesiod name server (MIT, alternate name) */
public static final int HESIOD		= 4;

/** Special value used in dynamic update messages */
public static final int NONE		= 254;

/** Matches any class */
public static final int ANY		= 255;

private static class DClassMnemonic extends Mnemonic {
	public
	DClassMnemonic() {
		super("DClass", CASE_UPPER);
		setPrefix("CLASS");
	}

	public void
	check(int val) {
		DClass.check(val);
	}
}

private static final Mnemonic classes = new DClassMnemonic();

static {
	classes.add(IN, "IN");
	classes.add(CH, "CH");
	classes.addAlias(CH, "CHAOS");
	classes.add(HS, "HS");
	classes.addAlias(HS, "HESIOD");
	classes.add(NONE, "NONE");
	classes.add(ANY, "ANY");
}

private
DClass() {}

/**
 * Checks that a numeric DClass is valid.
 * @throws InvalidDClassException The class is out of range.
 */
public static void
check(int i) {
	if (i < 0 || i > 0xFFFF)
		throw new InvalidDClassException(i);
}

/**
 * Converts a numeric DClass into a String
 * @return The canonical string representation of the class
 * @throws InvalidDClassException The class is out of range.
 */
public static String
string(int i) {
	return classes.getText(i);
}

/**
 * Converts a String representation of a DClass into its numeric value
 * @return The class code, or -1 on error.
 */
public static int
value(String s) {
	return classes.getValue(s);
}

}
