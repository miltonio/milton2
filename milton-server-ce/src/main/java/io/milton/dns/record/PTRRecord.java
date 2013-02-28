/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package io.milton.dns.record;

import io.milton.dns.Name;

/**
 * Pointer Record  - maps a domain name representing an Internet Address to
 * a hostname.
 *
 * @author Brian Wellington
 */

public class PTRRecord extends SingleCompressedNameBase {

private static final long serialVersionUID = -8321636610425434192L;

PTRRecord() {}

Record
getObject() {
	return new PTRRecord();
}

/** 
 * Creates a new PTR Record with the given data
 * @param target The name of the machine with this address
 */
public
PTRRecord(Name name, int dclass, long ttl, Name target) {
	super(name, Type.PTR, dclass, ttl, target, "target");
}

/** Gets the target of the PTR Record */
public Name
getTarget() {
	return getSingleName();
}

}
