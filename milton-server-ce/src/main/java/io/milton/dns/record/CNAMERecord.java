/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package io.milton.dns.record;

import io.milton.dns.Name;

/**
 * CNAME Record  - maps an alias to its real name
 *
 * @author Brian Wellington
 */

public class CNAMERecord extends SingleCompressedNameBase {

private static final long serialVersionUID = -4020373886892538580L;

CNAMERecord() {}

Record
getObject() {
	return new CNAMERecord();
}

/**
 * Creates a new CNAMERecord with the given data
 * @param alias The name to which the CNAME alias points
 */
public
CNAMERecord(Name name, int dclass, long ttl, Name alias) {
	super(name, Type.CNAME, dclass, ttl, alias, "alias");
}

/**
 * Gets the target of the CNAME Record
 */
public Name
getTarget() {
	return getSingleName();
}

/** Gets the alias specified by the CNAME Record */
public Name
getAlias() {
	return getSingleName();
}

}
