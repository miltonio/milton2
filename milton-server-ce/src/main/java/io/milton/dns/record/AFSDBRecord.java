/*
 * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)
 * 
 * Copied from the DnsJava project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.milton.dns.record;

import io.milton.dns.Name;

/**
 * AFS Data Base Record - maps a domain name to the name of an AFS cell
 * database server.
 *
 *
 * @author Brian Wellington
 */

public class AFSDBRecord extends U16NameBase {

private static final long serialVersionUID = 3034379930729102437L;

AFSDBRecord() {}

Record
getObject() {
	return new AFSDBRecord();
}

/**
 * Creates an AFSDB Record from the given data.
 * @param subtype Indicates the type of service provided by the host.
 * @param host The host providing the service.
 */
public
AFSDBRecord(Name name, int dclass, long ttl, int subtype, Name host) {
	super(name, Type.AFSDB, dclass, ttl, subtype, "subtype", host, "host");
}

/** Gets the subtype indicating the service provided by the host. */
public int
getSubtype() {
	return getU16Field();
}

/** Gets the host providing service for the domain. */
public Name
getHost() {
	return getNameField();
}

}
