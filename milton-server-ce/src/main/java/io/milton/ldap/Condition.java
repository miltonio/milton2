/*
 * Copyright 2012 McEvoy Software Ltd.
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

package io.milton.ldap;

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.LdapContact;

public interface Condition {

    @SuppressWarnings({"JavaDoc"})
    public enum Operator {
        Or, And, Not, IsEqualTo,
        IsGreaterThan, IsGreaterThanOrEqualTo,
        IsLessThan, IsLessThanOrEqualTo,
        IsNull, IsTrue, IsFalse,
        Like, StartsWith, Contains
    }	
	
	/**
	 * Append condition to buffer.
	 *
	 * @param buffer search filter buffer
	 */
	//void appendTo(StringBuilder buffer);

	/**
	 * True if condition is empty.
	 *
	 * @return true if condition is empty
	 */
	boolean isEmpty();

	/**
	 * Test if the contact matches current condition.
	 *
	 * @param contact Exchange Contact
	 * @return true if contact matches condition
	 */
	boolean isMatch(LdapContact contact) throws NotAuthorizedException, BadRequestException;
}