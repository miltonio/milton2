/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

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

private static Mnemonic classes = new DClassMnemonic();

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
