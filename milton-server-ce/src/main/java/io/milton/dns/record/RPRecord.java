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
	StringBuilder sb = new StringBuilder();
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
