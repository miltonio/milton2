/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package io.milton.dns.record;

import io.milton.dns.Name;

/**
 * Mail Group Record  - specifies a mailbox which is a member of a mail group.
 *
 * @author Brian Wellington
 */

public class MGRecord extends SingleNameBase {

private static final long serialVersionUID = -3980055550863644582L;

MGRecord() {}

Record
getObject() {
	return new MGRecord();
}

/** 
 * Creates a new MG Record with the given data
 * @param mailbox The mailbox that is a member of the group specified by the
 * domain.
 */
public
MGRecord(Name name, int dclass, long ttl, Name mailbox) {
	super(name, Type.MG, dclass, ttl, mailbox, "mailbox");
}

/** Gets the mailbox in the mail group specified by the domain */
public Name
getMailbox() {
	return getSingleName();
}

}
