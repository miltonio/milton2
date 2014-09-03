/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package io.milton.dns.record;


/**
 * Constants and functions relating to DNS message sections
 *
 * @author Brian Wellington
 */

public final class Section {

/** The question (first) section */
public static final int QUESTION	= 0;

/** The answer (second) section */
public static final int ANSWER		= 1;

/** The authority (third) section */
public static final int AUTHORITY	= 2;

/** The additional (fourth) section */
public static final int ADDITIONAL	= 3;

/* Aliases for dynamic update */
/** The zone (first) section of a dynamic update message */
public static final int ZONE		= 0;

/** The prerequisite (second) section of a dynamic update message */
public static final int PREREQ		= 1;

/** The update (third) section of a dynamic update message */
public static final int UPDATE		= 2;

private static Mnemonic sections = new Mnemonic("Message Section",
						Mnemonic.CASE_LOWER);
private static String [] longSections = new String[4];
private static String [] updateSections = new String[4];

static {
	sections.setMaximum(3);
	sections.setNumericAllowed(true);

	sections.add(QUESTION, "qd");
	sections.add(ANSWER, "an");
	sections.add(AUTHORITY, "au");
	sections.add(ADDITIONAL, "ad");

	longSections[QUESTION]		= "QUESTIONS";
	longSections[ANSWER]		= "ANSWERS";
	longSections[AUTHORITY]		= "AUTHORITY RECORDS";
	longSections[ADDITIONAL]	= "ADDITIONAL RECORDS";

	updateSections[ZONE]		= "ZONE";
	updateSections[PREREQ]		= "PREREQUISITES";
	updateSections[UPDATE]		= "UPDATE RECORDS";
	updateSections[ADDITIONAL]	= "ADDITIONAL RECORDS";
}

private
Section() {}

/** Converts a numeric Section into an abbreviation String */
public static String
string(int i) {
	return sections.getText(i);
}

/** Converts a numeric Section into a full description String */
public static String
longString(int i) {
	sections.check(i);
	return longSections[i];
}

/**
 * Converts a numeric Section into a full description String for an update
 * Message.
 */
public static String
updString(int i) {
	sections.check(i);
	return updateSections[i];
}

/** Converts a String representation of a Section into its numeric value */
public static int
value(String s) {
	return sections.getValue(s);
}

}
