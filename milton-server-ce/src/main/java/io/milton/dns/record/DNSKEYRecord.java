/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

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
