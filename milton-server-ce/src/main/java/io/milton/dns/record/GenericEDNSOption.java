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

import io.milton.dns.utils.base16;

import java.io.*;


/**
 * An EDNSOption with no internal structure.
 * 
 * @author Ming Zhou &lt;mizhou@bnivideo.com&gt;, Beaumaris Networks
 * @author Brian Wellington
 */
public class GenericEDNSOption extends EDNSOption {

private byte [] data;

GenericEDNSOption(int code) {
	super(code);
}

/**
 * Construct a generic EDNS option.
 * @param data The contents of the option.
 */
public 
GenericEDNSOption(int code, byte [] data) {
	super(code);
	this.data = Record.checkByteArrayLength("option data", data, 0xFFFF);
}

void 
optionFromWire(DNSInput in) throws IOException {
	data = in.readByteArray();
}

void 
optionToWire(DNSOutput out) {
	out.writeByteArray(data);
}

String 
optionToString() {
	return "<" + base16.toString(data) + ">";
}

}
