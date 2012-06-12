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

import com.sun.jndi.ldap.BerDecoder;
import io.milton.common.LogUtils;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class LdapParser {

	private static final Logger log = LoggerFactory.getLogger(LdapParser.class);
	
	private final LdapPropertyMapper propertyMapper;
	private final LdapResponseHandler helper;
	private final UserFactory userFactory;
	private final Conditions conditions;

	public LdapParser(LdapPropertyMapper propertyMapper, LdapResponseHandler helper, UserFactory userFactory) {
		this.propertyMapper = propertyMapper;
		this.helper = helper;
		this.userFactory = userFactory;
		this.conditions = new Conditions(propertyMapper);
	}
	
	
	public LdapFilter parseFilter(BerDecoder reqBer, LdapPrincipal user, String userName) throws IOException {
		LdapFilter ldapFilter;
		if (reqBer.peekByte() == Ldap.LDAP_FILTER_PRESENT) {
			String attributeName = reqBer.parseStringWithTag(Ldap.LDAP_FILTER_PRESENT, helper.isLdapV3(), null).toLowerCase();
			ldapFilter = new SimpleLdapFilter( propertyMapper, userFactory, attributeName);
		} else {
			int[] seqSize = new int[1];
			int ldapFilterType = reqBer.parseSeq(seqSize);
			int end = reqBer.getParsePosition() + seqSize[0];

			ldapFilter = parseNestedFilter(reqBer, ldapFilterType, end, user, userName);
		}

		return ldapFilter;
	}

	private LdapFilter parseNestedFilter(BerDecoder reqBer, int ldapFilterType, int end, LdapPrincipal user, String userName) throws IOException {
		LdapFilter nestedFilter;

		if ((ldapFilterType == Ldap.LDAP_FILTER_OR) || (ldapFilterType == Ldap.LDAP_FILTER_AND)
				|| ldapFilterType == Ldap.LDAP_FILTER_NOT) {
			nestedFilter = new CompoundLdapFilter(conditions, ldapFilterType);

			while (reqBer.getParsePosition() < end && reqBer.bytesLeft() > 0) {
				if (reqBer.peekByte() == Ldap.LDAP_FILTER_PRESENT) {
					String attributeName = reqBer.parseStringWithTag(Ldap.LDAP_FILTER_PRESENT, helper.isLdapV3(), null).toLowerCase();
					nestedFilter.add(new SimpleLdapFilter(propertyMapper, userFactory, attributeName));
				} else {
					int[] seqSize = new int[1];
					int ldapFilterOperator = reqBer.parseSeq(seqSize);
					int subEnd = reqBer.getParsePosition() + seqSize[0];
					LdapFilter f2 = parseNestedFilter(reqBer, ldapFilterOperator, subEnd, user, userName);
					nestedFilter.add(f2);
				}
			}
		} else {
			// simple filter
			nestedFilter = parseSimpleFilter(reqBer, ldapFilterType, user, userName);
		}

		return nestedFilter;
	}

	private LdapFilter parseSimpleFilter(BerDecoder reqBer, int ldapFilterOperator, LdapPrincipal user, String userName) throws IOException {
		String attributeName = reqBer.parseString(helper.isLdapV3()).toLowerCase();
		int ldapFilterMode = 0;

		StringBuilder value = new StringBuilder();
		if (ldapFilterOperator == Ldap.LDAP_FILTER_SUBSTRINGS) {
			// Thunderbird sends values with space as separate strings, rebuild value
			int[] seqSize = new int[1];
			/*LBER_SEQUENCE*/
			reqBer.parseSeq(seqSize);
			int end = reqBer.getParsePosition() + seqSize[0];
			while (reqBer.getParsePosition() < end && reqBer.bytesLeft() > 0) {
				ldapFilterMode = reqBer.peekByte();
				if (value.length() > 0) {
					value.append(' ');
				}
				value.append(reqBer.parseStringWithTag(ldapFilterMode, helper.isLdapV3(), null));
			}
		} else if (ldapFilterOperator == Ldap.LDAP_FILTER_EQUALITY) {
			value.append(reqBer.parseString(helper.isLdapV3()));
		} else {
			log.warn("LOG_LDAP_UNSUPPORTED_FILTER_VALUE");
		}

		String sValue = value.toString();

		if ("uid".equalsIgnoreCase(attributeName) && sValue.equals(userName)) {
			// replace with actual alias instead of login name search, only in Dav mode
			if (sValue.equals(userName)) {
				sValue = user.getName();
				LogUtils.debug(log, "LOG_LDAP_REPLACED_UID_FILTER", userName, sValue);
			}
		}

		return new SimpleLdapFilter(propertyMapper, userFactory, attributeName, sValue, ldapFilterOperator, ldapFilterMode);
	}

	public Set<String> parseReturningAttributes(BerDecoder reqBer) throws IOException {
		Set<String> returningAttributes = new HashSet<String>();
		int[] seqSize = new int[1];
		reqBer.parseSeq(seqSize);
		int end = reqBer.getParsePosition() + seqSize[0];
		while (reqBer.getParsePosition() < end && reqBer.bytesLeft() > 0) {
			returningAttributes.add(reqBer.parseString(helper.isLdapV3()).toLowerCase());
		}
		return returningAttributes;
	}

}
