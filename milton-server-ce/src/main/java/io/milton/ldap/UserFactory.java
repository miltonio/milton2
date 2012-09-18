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
import java.util.List;

/**
 *
 * @author brad
 */
public interface UserFactory {
	

	/**
	 * Used for SASL authentication
	 * 
	 * @param userName
	 * @return 
	 */
	String getUserPassword(String userName);

	LdapPrincipal getUser(String userName, String password);
	
	/**
	 * Search for contacts in the Global Address List
	 * 
	 * @param equalTo
	 * @param convertLdapToContactReturningAttributes
	 * @param sizeLimit
	 * @return 
	 */
	List<LdapContact> galFind(Condition equalTo, int sizeLimit) throws NotAuthorizedException, BadRequestException;	
}
