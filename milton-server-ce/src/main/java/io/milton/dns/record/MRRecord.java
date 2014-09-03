/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package io.milton.dns.record;

import io.milton.dns.Name;

/**
 * Mailbox Rename Record  - specifies a rename of a mailbox.
 *
 * @author Brian Wellington
 */

public class MRRecord extends SingleNameBase {

private static final long serialVersionUID = -5617939094209927533L;

MRRecord() {}

Record
getObject() {
	return new MRRecord();
}

/** 
 * Creates a new MR Record with the given data
 * @param newName The new name of the mailbox specified by the domain.
 * domain.
 */
public
MRRecord(Name name, int dclass, long ttl, Name newName) {
	super(name, Type.MR, dclass, ttl, newName, "new name");
}

/** Gets the new name of the mailbox specified by the domain */
public Name
getNewName() {
	return getSingleName();
}

}
