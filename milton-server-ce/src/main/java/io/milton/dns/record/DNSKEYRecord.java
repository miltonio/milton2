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
import io.milton.dns.record.DNSSEC.Algorithm;
import io.milton.dns.record.DNSSEC.DNSSECException;

import java.io.*;
import java.security.PublicKey;

/**
 * Key - contains a cryptographic public key for use by DNS.
 * The data can be converted to objects implementing
 * java.security.interfaces.PublicKey
 * @see DNSSEC
 *
 * @author Brian Wellington
 */

public class DNSKEYRecord extends KEYBase {

public static class Protocol {
	private Protocol() {}

	/** Key will be used for DNSSEC */
	public static final int DNSSEC = 3;
}

public static class Flags {
	private Flags() {}

	/** Key is a zone key */
	public static final int ZONE_KEY = 0x100;

	/** Key is a secure entry point key */
	public static final int SEP_KEY = 0x1;

	/** Key has been revoked */
	public static final int REVOKE = 0x80;
}

private static final long serialVersionUID = -8679800040426675002L;

DNSKEYRecord() {}

Record
getObject() {
	return new DNSKEYRecord();
}

/**
 * Creates a DNSKEY Record from the given data
 * @param flags Flags describing the key's properties
 * @param proto The protocol that the key was created for
 * @param alg The key's algorithm
 * @param key Binary representation of the key
 */
public
DNSKEYRecord(Name name, int dclass, long ttl, int flags, int proto, int alg,
	     byte [] key)
{
	super(name, Type.DNSKEY, dclass, ttl, flags, proto, alg, key);
}

/**
 * Creates a DNSKEY Record from the given data
 * @param flags Flags describing the key's properties
 * @param proto The protocol that the key was created for
 * @param alg The key's algorithm
 * @param key The key as a PublicKey
 * @throws DNSSEC.DNSSECException The PublicKey could not be converted into DNS
 * format.
 */
public
DNSKEYRecord(Name name, int dclass, long ttl, int flags, int proto, int alg,
	     PublicKey key) throws DNSSEC.DNSSECException
{
	super(name, Type.DNSKEY, dclass, ttl, flags, proto, alg,
	      DNSSEC.fromPublicKey(key, alg));
	publicKey = key;
}

void
rdataFromString(Tokenizer st, Name origin) throws IOException {
	flags = st.getUInt16();
	proto = st.getUInt8();
	String algString = st.getString();
	alg = DNSSEC.Algorithm.value(algString);
	if (alg < 0)
		throw st.exception("Invalid algorithm: " + algString);
	key = st.getBase64();
}

}
