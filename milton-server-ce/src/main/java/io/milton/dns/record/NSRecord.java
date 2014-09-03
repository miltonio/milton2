/*  * Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)  *   * Copied from the DnsJava project  *  * This program is free software: you can redistribute it and/or modify  * it under the terms of the GNU Affero General Public License as published by  * the Free Software Foundation, either version 3 of the License, or  * (at your option) any later version.  * This program is distributed in the hope that it will be useful,  * but WITHOUT ANY WARRANTY; without even the implied warranty of  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  * GNU General Public License for more details.  * You should have received a copy of the GNU General Public License  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  */
package io.milton.dns.record;

import io.milton.dns.Name;

/**
 * Name Server Record - contains the name server serving the named zone
 *
 * @author Brian Wellington
 */
public class NSRecord extends SingleCompressedNameBase {

	private static final long serialVersionUID = 487170758138268838L;

	NSRecord() {
	}

	Record getObject() {
		return new NSRecord();
	}

	/**
	 * Creates a new NS Record with the given data
	 *
	 * @param target The name server for the given domain
	 */
	public NSRecord(Name name, int dclass, long ttl, Name target) {
		super(name, Type.NS, dclass, ttl, target, "target");
	}

	/**
	 * Gets the target of the NS Record
	 */
	public Name getTarget() {
		return getSingleName();
	}

	public Name getAdditionalName() {
		return getSingleName();
	}
}
