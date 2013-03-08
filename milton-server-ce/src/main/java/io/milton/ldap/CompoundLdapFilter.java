/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.milton.ldap;

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.LdapContact;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author brad
 */
public class CompoundLdapFilter implements LdapFilter {
	private final Conditions conditions;
	private final Set<LdapFilter> criteria = new HashSet<LdapFilter>();
	private final int type;

	CompoundLdapFilter(Conditions conditions, int filterType) {
		this.conditions = conditions;
		type = filterType;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		if (type == Ldap.LDAP_FILTER_OR) {
			buffer.append("(|");
		} else if (type == Ldap.LDAP_FILTER_AND) {
			buffer.append("(&");
		} else {
			buffer.append("(!");
		}
		for (LdapFilter child : criteria) {
			buffer.append(child.toString());
		}
		buffer.append(')');
		return buffer.toString();
	}

	/**
	 * Add child filter
	 *
	 * @param filter inner filter
	 */
	@Override
	public void add(LdapFilter filter) {
		criteria.add(filter);
	}

	/**
	 * This is only a full search if every child
	 * is also a full search
	 *
	 * @return true if full search filter
	 */
	@Override
	public boolean isFullSearch() {
		for (LdapFilter child : criteria) {
			if (!child.isFullSearch()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Build search filter for Contacts folder search.
	 * Use Exchange SEARCH syntax
	 *
	 * @return contact search filter
	 */
	@Override
	public Condition getContactSearchFilter() {
		MultiCondition condition;
		if (type == Ldap.LDAP_FILTER_OR) {
			condition = conditions.or();
		} else {
			condition = conditions.and();
		}
		for (LdapFilter child : criteria) {
			condition.add(child.getContactSearchFilter());
		}
		return condition;
	}

	/**
	 * Test if person matches the current filter.
	 *
	 * @param person person attributes map
	 * @return true if filter match
	 */
	@Override
	public boolean isMatch(LdapContact person) throws NotAuthorizedException, BadRequestException {
		if (type == Ldap.LDAP_FILTER_OR) {
			for (LdapFilter child : criteria) {
				if (!child.isFullSearch()) {
					if (child.isMatch(person)) {
						// We've found a match
						return true;
					}
				}
			}
			// No subconditions are met
			return false;
		} else if (type == Ldap.LDAP_FILTER_AND) {
			for (LdapFilter child : criteria) {
				if (!child.isFullSearch()) {
					if (!child.isMatch(person)) {
						// We've found a miss
						return false;
					}
				}
			}
			// All subconditions are met
			return true;
		}
		return false;
	}

	/**
	 * Find persons in Exchange GAL matching filter.
	 * Iterate over child filters to build results.
	 *
	 * @param user Exchange session
	 * @return persons map
	 * @throws IOException on error
	 */
	@Override
	public List<LdapContact> findInGAL(LdapPrincipal user, Set<String> returningAttributes, int sizeLimit) throws IOException, NotAuthorizedException, BadRequestException {
		List<LdapContact> persons = null;
		for (LdapFilter child : criteria) {
			int currentSizeLimit = sizeLimit;
			if (persons != null) {
				currentSizeLimit -= persons.size();
			}
			List<LdapContact> childFind = child.findInGAL(user, returningAttributes, currentSizeLimit);
			if (childFind != null) {
				if (persons == null) {
					persons = childFind;
				} else if (type == Ldap.LDAP_FILTER_OR) {
					// Create the union of the existing results and the child found results
					persons.addAll(childFind);
				} else if (type == Ldap.LDAP_FILTER_AND) {
					// Append current child filter results that match all child filters to persons.
					// The hard part is that, due to the 100-item-returned galFind limit
					// we may catch new items that match all child filters in each child search.
					// Thus, instead of building the intersection, we check each result against
					// all filters.
					for (LdapContact result : childFind) {
						if (isMatch(result)) {
							// This item from the child result set matches all sub-criteria, add it
							persons.add(result);
						}
					}
				}
			}
		}
		if ((persons == null) && !isFullSearch()) {
			// return an empty map (indicating no results were found)
			return new ArrayList<LdapContact>();
		}
		return persons;
	}
	
}
