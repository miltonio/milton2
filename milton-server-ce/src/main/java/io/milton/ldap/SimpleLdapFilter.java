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

import io.milton.resource.LdapContact;
import io.milton.common.LogUtils;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
class SimpleLdapFilter implements LdapFilter {
	private static final Logger log = LoggerFactory.getLogger(SimpleLdapFilter.class);
	
	private final UserFactory userFactory;
	private final LdapPropertyMapper propertyMapper;
	private final Conditions conditions;
	
	static final String STAR = "*";
	final String attributeName;
	final String value;
	final int mode;
	final int operator;
	final boolean canIgnore;

	SimpleLdapFilter(LdapPropertyMapper propertyMapper, UserFactory userFactory, String attributeName) {
		this.userFactory = userFactory;
		this.propertyMapper = propertyMapper;
		this.conditions = new Conditions(propertyMapper);
		this.attributeName = attributeName;
		this.value = SimpleLdapFilter.STAR;
		this.operator = Ldap.LDAP_FILTER_SUBSTRINGS;
		this.mode = 0;
		this.canIgnore = checkIgnore();
	}

	SimpleLdapFilter(LdapPropertyMapper propertyMapper, UserFactory userFactory, String attributeName, String value, int ldapFilterOperator, int ldapFilterMode) {
		this.userFactory = userFactory;
		this.propertyMapper = propertyMapper;
		this.conditions = new Conditions(propertyMapper);
		this.attributeName = attributeName;
		this.value = value;
		this.operator = ldapFilterOperator;
		this.mode = ldapFilterMode;
		this.canIgnore = checkIgnore();
	}

	private boolean checkIgnore() {
		if ("objectclass".equals(attributeName) && STAR.equals(value)) {
			// ignore cases where any object class can match
			return true;
//		} else if (LdapConnection.CRITERIA_MAP.get(attributeName) == null && LdapUtils.getContactAttributeName(attributeName) == null) {
//			log.debug("LOG_LDAP_UNSUPPORTED_FILTER_ATTRIBUTE", attributeName, value);
//			return true;
		}
		return false;
	}

	@Override
	public boolean isFullSearch() {
		// only (objectclass=*) is a full search
		return "objectclass".equals(attributeName) && STAR.equals(value);
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		buffer.append(attributeName);
		buffer.append('=');
		if (SimpleLdapFilter.STAR.equals(value)) {
			buffer.append(SimpleLdapFilter.STAR);
		} else if (operator == Ldap.LDAP_FILTER_SUBSTRINGS) {
			if (mode == Ldap.LDAP_SUBSTRING_FINAL || mode == Ldap.LDAP_SUBSTRING_ANY) {
				buffer.append(SimpleLdapFilter.STAR);
			}
			buffer.append(value);
			if (mode == Ldap.LDAP_SUBSTRING_INITIAL || mode == Ldap.LDAP_SUBSTRING_ANY) {
				buffer.append(SimpleLdapFilter.STAR);
			}
		} else {
			buffer.append(value);
		}
		buffer.append(')');
		return buffer.toString();
	}

	@Override
	public Condition getContactSearchFilter() {
		String contactAttributeName = attributeName;
		if (canIgnore || (contactAttributeName == null)) {
			return null;
		}
		Condition condition = null;
		if (operator == Ldap.LDAP_FILTER_EQUALITY) {
			LogUtils.debug(log, "getContactSearchFilter: equality", value);
			condition = conditions.isEqualTo(contactAttributeName, value);
		} else if ("*".equals(value)) {
			LogUtils.debug(log, "getContactSearchFilter: *");
			condition = conditions.not(conditions.isNull(contactAttributeName));
			// do not allow substring search on integer field imapUid
		} else if (!"imapUid".equals(contactAttributeName)) {
			// endsWith not supported by exchange, convert to contains
			if (mode == Ldap.LDAP_SUBSTRING_FINAL || mode == Ldap.LDAP_SUBSTRING_ANY) {
				LogUtils.debug(log, "getContactSearchFilter: contains", value);
				condition = conditions.contains(contactAttributeName, value);
			} else {
				LogUtils.debug(log, "getContactSearchFilter: startswith", value);
				condition = conditions.startsWith(contactAttributeName, value);
			}
		}
		return condition;
	}

	@Override
	public boolean isMatch(LdapContact person) throws NotAuthorizedException, BadRequestException {
		if (canIgnore) {
			// Ignore this filter
			return true;
		}
		
		String propValue = propertyMapper.getLdapPropertyValue(attributeName, person);
		if (propValue == null) {
			// No value to allow for filter match
			return false;
		} else if (propValue == null) {
			// This is a presence filter: found
			return true;
		} else if ((operator == Ldap.LDAP_FILTER_EQUALITY) && propValue.equalsIgnoreCase(value)) {
			// Found an exact match
			return true;
		} else if ((operator == Ldap.LDAP_FILTER_SUBSTRINGS) && (propValue.toLowerCase().indexOf(value.toLowerCase()) >= 0)) {
			// Found a substring match
			return true;
		}
		return false;
	}

	@Override
	public List<LdapContact> findInGAL(LdapPrincipal user, Set<String> returningAttributes, int sizeLimit) throws IOException, NotAuthorizedException, BadRequestException {
		if (canIgnore) {
			return null;
		}
		String contactAttributeName = attributeName;
		if (contactAttributeName != null) {
			// quick fix for cn=* filter
			List<LdapContact> galPersons = userFactory.galFind(conditions.startsWith(contactAttributeName, "*".equals(value) ? "A" : value), sizeLimit);
			if (operator == Ldap.LDAP_FILTER_EQUALITY) {
				// Make sure only exact matches are returned
				List<LdapContact> list = new ArrayList<LdapContact>();
				for (LdapContact person : galPersons) {
					if (isMatch(person)) {
						// Found an exact match
						list.add(person);
					}
				}
				return list;
			} else {
				return galPersons;
			}
		}
		return null;
	}

	@Override
	public void add(LdapFilter filter) {
		// Should never be called
		log.error("LOG_LDAP_UNSUPPORTED_FILTER", "nested simple filters");
	}
	
}
