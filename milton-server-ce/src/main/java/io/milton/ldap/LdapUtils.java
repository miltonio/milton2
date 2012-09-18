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

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.SimpleTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class LdapUtils {

	private static final Logger log = LoggerFactory.getLogger(LdapUtils.class);
	protected static final String YYYY_MM_DD_HH_MM_SS = "yyyy/MM/dd HH:mm:ss";
	private static final String YYYYMMDD_T_HHMMSS_Z = "yyyyMMdd'T'HHmmss'Z'";
	protected static final String YYYY_MM_DD_T_HHMMSS_Z = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private static final String YYYY_MM_DD = "yyyy-MM-dd";
	public static final SimpleTimeZone GMT_TIMEZONE = new SimpleTimeZone(0, "GMT");

	public static SimpleDateFormat getZuluDateFormat() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(YYYYMMDD_T_HHMMSS_Z, Locale.ENGLISH);
		dateFormat.setTimeZone(GMT_TIMEZONE);
		return dateFormat;
	}

	public static Set<String> convertLdapToContactReturningAttributes(Set<String> returningAttributes) {
		Set<String> contactReturningAttributes;
		if (returningAttributes != null && !returningAttributes.isEmpty()) {
			contactReturningAttributes = new HashSet<String>();
			// always return uid
			contactReturningAttributes.add("imapUid");
			for (String attribute : returningAttributes) {
				contactReturningAttributes.add(attribute);
			}
		} else {
			contactReturningAttributes = ContactAttributes.CONTACT_ATTRIBUTES;
		}
		return contactReturningAttributes;
	}
}
