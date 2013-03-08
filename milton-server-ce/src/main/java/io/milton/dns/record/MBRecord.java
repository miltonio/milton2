/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package io.milton.dns.record;

import io.milton.dns.Name;

/**
 * Mailbox Record  - specifies a host containing a mailbox.
 *
 * @author Brian Wellington
 */

public class MBRecord extends SingleNameBase {

private static final long serialVersionUID = 532349543479150419L;

MBRecord() {}

Record
getObject() {
	return new MBRecord();
}

/** 
 * Creates a new MB Record with the given data
 * @param mailbox The host containing the mailbox for the domain.
 */
public
MBRecord(Name name, int dclass, long ttl, Name mailbox) {
	super(name, Type.MB, dclass, ttl, mailbox, "mailbox");
}

/** Gets the mailbox for the domain */
public Name
getMailbox() {
	return getSingleName();
}

public Name
getAdditionalName() {
	return getSingleName();
}

}
