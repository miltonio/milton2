/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package io.milton.dns.record;

import io.milton.dns.Name;

/**
 * NSAP Pointer Record  - maps a domain name representing an NSAP Address to
 * a hostname.
 *
 * @author Brian Wellington
 */

public class NSAP_PTRRecord extends SingleNameBase {

private static final long serialVersionUID = 2386284746382064904L;

NSAP_PTRRecord() {}

Record
getObject() {
	return new NSAP_PTRRecord();
}

/** 
 * Creates a new NSAP_PTR Record with the given data
 * @param target The name of the host with this address
 */
public
NSAP_PTRRecord(Name name, int dclass, long ttl, Name target) {
	super(name, Type.NSAP_PTR, dclass, ttl, target, "target");
}

/** Gets the target of the NSAP_PTR Record */
public Name
getTarget() {
	return getSingleName();
}

}
