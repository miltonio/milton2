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
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 *
 * @author brad
 */
public interface LdapFilter {

	Condition getContactSearchFilter();

	List<LdapContact> findInGAL(LdapPrincipal user, Set<String> returningAttributes, int sizeLimit) throws IOException, NotAuthorizedException, BadRequestException;

	void add(LdapFilter filter);

	boolean isFullSearch();

	boolean isMatch(LdapContact person) throws NotAuthorizedException, BadRequestException;

}
