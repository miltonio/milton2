/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

package io.milton.dns.record;

import io.milton.dns.Name;

/**
 * Key Exchange - delegation of authority
 *
 * @author Brian Wellington
 */

public class KXRecord extends U16NameBase {

private static final long serialVersionUID = 7448568832769757809L;

KXRecord() {}

Record
getObject() {
	return new KXRecord();
}

/**
 * Creates a KX Record from the given data
 * @param preference The preference of this KX.  Records with lower priority
 * are preferred.
 * @param target The host that authority is delegated to
 */
public
KXRecord(Name name, int dclass, long ttl, int preference, Name target) {
	super(name, Type.KX, dclass, ttl, preference, "preference",
	      target, "target");
}

/** Returns the target of the KX record */
public Name
getTarget() {
	return getNameField();
}

/** Returns the preference of this KX record */
public int
getPreference() {
	return getU16Field();
}

public Name
getAdditionalName() {
	return getNameField();
}

}
