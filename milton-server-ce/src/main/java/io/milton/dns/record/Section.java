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

private static final Mnemonic sections = new Mnemonic("Message Section", Mnemonic.CASE_LOWER);
private static final String[] longSections = new String[4];
private static final String[] updateSections = new String[4];

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
