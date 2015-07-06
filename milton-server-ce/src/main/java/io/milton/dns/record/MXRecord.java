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

/**
 * Mail Exchange - specifies where mail to a domain is sent
 *
 * @author Brian Wellington
 */
public class MXRecord extends U16NameBase {

    private static final long serialVersionUID = 2914841027584208546L;

    MXRecord() {
    }

    @Override
    Record getObject() {
        return new MXRecord();
    }

    /**
     * Creates an MX Record from the given data
     *
     * @param priority The priority of this MX. Records with lower priority are
     * preferred.
     * @param target The host that mail is sent to
     */
    public MXRecord(Name name, int dclass, long ttl, int priority, Name target) {
        super(name, Type.MX, dclass, ttl, priority, "priority", target, "target");
    }

    /**
     * Returns the target of the MX record
     */
    public Name getTarget() {
        return getNameField();
    }

    /**
     * Returns the priority of this MX record
     */
    public int getPriority() {
        return getU16Field();
    }

    @Override
    void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeU16(u16Field);
        nameField.toWire(out, c, canonical);
    }

    @Override
    public Name getAdditionalName() {
        return getNameField();
    }
}
