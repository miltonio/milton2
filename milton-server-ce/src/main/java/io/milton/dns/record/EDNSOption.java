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


import java.io.*;
import java.util.Arrays;

/**
 * DNS extension options, as described in RFC 2671.  The rdata of an OPT record
 * is defined as a list of options; this represents a single option.
 * 
 * @author Brian Wellington
 * @author Ming Zhou &lt;mizhou@bnivideo.com&gt;, Beaumaris Networks
 */
public abstract class EDNSOption {

public static class Code {
	private Code() {}

	/** Name Server Identifier, RFC 5001 */
	public final static int NSID = 3;

	/** Client Subnet, defined in draft-vandergaast-edns-client-subnet-00 */
	public final static int CLIENT_SUBNET = 20730;

	private static final Mnemonic codes = new Mnemonic("EDNS Option Codes",	Mnemonic.CASE_UPPER);

	static {
		codes.setMaximum(0xFFFF);
		codes.setPrefix("CODE");
		codes.setNumericAllowed(true);

		codes.add(NSID, "NSID");
		codes.add(CLIENT_SUBNET, "CLIENT_SUBNET");
	}

	/**
	 * Converts an EDNS Option Code into its textual representation
	 */
	public static String
	string(int code) {
		return codes.getText(code);
	}

	/**
	 * Converts a textual representation of an EDNS Option Code into its
	 * numeric value.
	 * @param s The textual representation of the option code
	 * @return The option code, or -1 on error.
	 */
	public static int
	value(String s) {
		return codes.getValue(s);
	}
}

private final int code;

/**
 * 
 * Creates an option with the given option code and data.
 */
public 
EDNSOption(int code) {
	this.code = Record.checkU16("code", code);
}

public String 
toString() {
	StringBuilder sb = new StringBuilder();

	sb.append("{");
	sb.append(EDNSOption.Code.string(code));
	sb.append(": ");
	sb.append(optionToString());
	sb.append("}");

	return sb.toString();
}

/**
 * Returns the EDNS Option's code.
 *
 * @return the option code
 */
public int 
getCode() {
	return code;
}

/**
 * Returns the EDNS Option's data, as a byte array.
 * 
 * @return the option data
 */
byte [] 
getData() {
	DNSOutput out = new DNSOutput();
	optionToWire(out);
	return out.toByteArray();
}

/**
 * Converts the wire format of an EDNS Option (the option data only) into the
 * type-specific format.
 * @param in The input Stream.
 */
abstract void 
optionFromWire(DNSInput in) throws IOException;

/**
 * Converts the wire format of an EDNS Option (including code and length) into
 * the type-specific format.
 * @param out The input stream.
 */
public static EDNSOption
fromWire(DNSInput in) throws IOException {
	int code, length;

	code = in.readU16();
	length = in.readU16();
	if (in.remaining() < length)
		throw new WireParseException("truncated option");
	int save = in.saveActive();
	in.setActive(length);
	EDNSOption option;
	switch (code) {
	case Code.NSID:
		option = new NSIDOption();
		break;
	case Code.CLIENT_SUBNET:
		option = new ClientSubnetOption();
		break;
	default:
		option = new GenericEDNSOption(code);
		break;
	}
	option.optionFromWire(in);
	in.restoreActive(save);

	return option;
}

/**
 * Converts the wire format of an EDNS Option (including code and length) into
 * the type-specific format.
 * @return The option, in wire format.
 */
public static EDNSOption
fromWire(byte [] b) throws IOException {
	return fromWire(new DNSInput(b));
}

/**
 * Converts an EDNS Option (the type-specific option data only) into wire format.
 * @param out The output stream.
 */
abstract void 
optionToWire(DNSOutput out);

/**
 * Converts an EDNS Option (including code and length) into wire format.
 * @param out The output stream.
 */
public void
toWire(DNSOutput out) {
	out.writeU16(code);
	int lengthPosition = out.current();
	out.writeU16(0); /* until we know better */
	optionToWire(out);
	int length = out.current() - lengthPosition - 2;
	out.writeU16At(length, lengthPosition);
}

/**
 * Converts an EDNS Option (including code and length) into wire format.
 * @return The option, in wire format.
 */
public byte []
toWire() throws IOException {
	DNSOutput out = new DNSOutput();
	toWire(out);
	return out.toByteArray();
}

/**
 * Determines if two EDNS Options are identical.
 * @param arg The option to compare to
 * @return true if the options are equal, false otherwise.
 */
public boolean
equals(Object arg) {
	if (arg == null || !(arg instanceof EDNSOption))
		return false;
	EDNSOption opt = (EDNSOption) arg;
	if (code != opt.code)
		return false;
	return Arrays.equals(getData(), opt.getData());
}

/**
 * Generates a hash code based on the EDNS Option's data.
 */
public int
hashCode() {
	byte [] array = getData();
	int hashval = 0;
	for (int i = 0; i < array.length; i++)
		hashval += ((hashval << 3) + (array[i] & 0xFF));
	return hashval;
}

abstract String optionToString();

}
