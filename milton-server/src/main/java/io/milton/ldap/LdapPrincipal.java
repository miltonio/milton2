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

import io.milton.resource.LdapContact;
import java.util.List;

/**
 * An LDAP principal (ie a user) is simply a contact which can contain other contacts, since
 * we often allow users to maintain their own private address books as well as
 * accessing the global contact list
 * 
 * Note that we imply certain meaning to properties defined elsewhere. The name
 * of the Resource is assumed to be the username of the principal, so is mapped
 * onto the "uid" ldap attribute.
 *
 * @author brad
 */
public interface LdapPrincipal extends LdapContact {


	/**
	 * Search for contacts in this user's private contact list. Generally these contacts
	 * will not be User accounts
	 * 
	 * @param contactReturningAttributes
	 * @param condition
	 * @param maxCount
	 * @return 
	 */
	List<LdapContact> searchContacts(Condition condition, int maxCount);
}
