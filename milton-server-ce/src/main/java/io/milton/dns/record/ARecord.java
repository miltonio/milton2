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

import io.milton.dns.Address;
import io.milton.dns.Name;

import java.net.*;
import java.io.*;

/**
 * Address Record - maps a domain name to an Internet address
 *
 * @author Brian Wellington
 */
public class ARecord extends Record {

    private static final long serialVersionUID = -2172609200849142323L;
    private int addr;

    public ARecord() {
    }

    Record getObject() {
        return new ARecord();
    }

    private static final int fromArray(byte[] array) {
        return (((array[0] & 0xFF) << 24)
                | ((array[1] & 0xFF) << 16)
                | ((array[2] & 0xFF) << 8)
                | (array[3] & 0xFF));
    }

    private static final byte[] toArray(int addr) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) ((addr >>> 24) & 0xFF);
        bytes[1] = (byte) ((addr >>> 16) & 0xFF);
        bytes[2] = (byte) ((addr >>> 8) & 0xFF);
        bytes[3] = (byte) (addr & 0xFF);
        return bytes;
    }

    /**
     * Creates an A Record from the given data
     *
     * @param address The address that the name refers to
     */
    public ARecord(Name name, int dclass, long ttl, InetAddress address) {
        super(name, Type.A, dclass, ttl);
        if (Address.familyOf(address) != Address.IPv4) {
            throw new IllegalArgumentException("invalid IPv4 address");
        }
        addr = fromArray(address.getAddress());
    }

    @Override
    void rrFromWire(DNSInput in) throws IOException {
        addr = fromArray(in.readByteArray(4));
    }

    void rdataFromString(Tokenizer st, Name origin) throws IOException {
        InetAddress address = st.getAddress(Address.IPv4);
        addr = fromArray(address.getAddress());
    }

    /**
     * Converts rdata to a String
     */
    String rrToString() {
        return (Address.toDottedQuad(toArray(addr)));
    }

    /**
     * Returns the Internet address
     */
    public InetAddress getAddress() {
        try {
            return InetAddress.getByAddress(toArray(addr));
        } catch (UnknownHostException e) {
            return null;
        }
    }

    void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeU32(((long) addr) & 0xFFFFFFFFL);
    }
}
