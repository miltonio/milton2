/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package io.milton.dns.record;

import io.milton.dns.Name;

import java.io.*;

/**
 * Responsible Person Record - lists the mail address of a responsible person
 * and a domain where TXT records are available.
 *
 * @author Tom Scola <tscola@research.att.com>
 * @author Brian Wellington
 */

public class RPRecord extends Record {

private static final long serialVersionUID = 8124584364211337460L;

private Name mailbox;
private Name textDomain;

RPRecord() {}

Record
getObject() {
	return new RPRecord();
}

/**
 * Creates an RP Record from the given data
 * @param mailbox The responsible person
 * @param textDomain The address where TXT records can be found
 */
public
RPRecord(Name name, int dclass, long ttl, Name mailbox, Name textDomain) {
	super(name, Type.RP, dclass, ttl);

	this.mailbox = checkName("mailbox", mailbox);
	this.textDomain = checkName("textDomain", textDomain);
}

void
rrFromWire(DNSInput in) throws IOException {
	mailbox = new Name(in);
	textDomain = new Name(in);
}

void
rdataFromString(Tokenizer st, Name origin) throws IOException {
	mailbox = st.getName(origin);
	textDomain = st.getName(origin);
}

/** Converts the RP Record to a String */
String
rrToString() {
	StringBuffer sb = new StringBuffer();
	sb.append(mailbox);
	sb.append(" ");
	sb.append(textDomain);
	return sb.toString();
}

/** Gets the mailbox address of the RP Record */
public Name
getMailbox() {
	return mailbox;
}

/** Gets the text domain info of the RP Record */
public Name
getTextDomain() {
	return textDomain;
}

void
rrToWire(DNSOutput out, Compression c, boolean canonical) {
	mailbox.toWire(out, null, canonical);
	textDomain.toWire(out, null, canonical);
}

}
