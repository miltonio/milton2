/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package io.milton.dns.record;

import io.milton.dns.Name;

/**
 * Mail Destination Record  - specifies a mail agent which delivers mail
 * for a domain (obsolete)
 *
 * @author Brian Wellington
 */

public class MDRecord extends SingleNameBase {

private static final long serialVersionUID = 5268878603762942202L;

MDRecord() {}

Record
getObject() {
	return new MDRecord();
}

/** 
 * Creates a new MD Record with the given data
 * @param mailAgent The mail agent that delivers mail for the domain.
 */
public
MDRecord(Name name, int dclass, long ttl, Name mailAgent) {
	super(name, Type.MD, dclass, ttl, mailAgent, "mail agent");
}

/** Gets the mail agent for the domain */
public Name
getMailAgent() {
	return getSingleName();
}

public Name
getAdditionalName() {
	return getSingleName();
}

}
