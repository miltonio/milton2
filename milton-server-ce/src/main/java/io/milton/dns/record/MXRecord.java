/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */
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
