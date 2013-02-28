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
