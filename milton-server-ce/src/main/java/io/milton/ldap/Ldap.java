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

import com.sun.jndi.ldap.BerDecoder;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author brad
 */
public class Ldap {
	/**
	 * Davmail base context
	 */
	static final String BASE_CONTEXT = "ou=people";
	static final String MSLIVE_BASE_CONTEXT = "c=US";
	/**
	 * OSX server (OpenDirectory) base context
	 */
	static final String OD_BASE_CONTEXT = "o=od";
	static final String OD_USER_CONTEXT = "cn=users, o=od";
	static final String OD_CONFIG_CONTEXT = "cn=config, o=od";
	static final String COMPUTER_CONTEXT = "cn=computers, o=od";
	static final String OD_GROUP_CONTEXT = "cn=groups, o=od";
	// TODO: adjust Directory Utility settings
	static final String COMPUTER_CONTEXT_LION = "cn=computers,o=od";
	static final String OD_USER_CONTEXT_LION = "cn=users, ou=people";
	/**
	 * Root DSE naming contexts (default and OpenDirectory)
	 */
	static final List<String> NAMING_CONTEXTS = new ArrayList<String>();

	static {
		NAMING_CONTEXTS.add(BASE_CONTEXT);
		NAMING_CONTEXTS.add(OD_BASE_CONTEXT);
	}
	static final List<String> PERSON_OBJECT_CLASSES = new ArrayList<String>();

	static {
		PERSON_OBJECT_CLASSES.add("top");
		PERSON_OBJECT_CLASSES.add("person");
		PERSON_OBJECT_CLASSES.add("organizationalPerson");
		PERSON_OBJECT_CLASSES.add("inetOrgPerson");
		// OpenDirectory class for iCal
		PERSON_OBJECT_CLASSES.add("apple-user");
	}

	/**
	 * OSX constant computer guid (used by iCal attendee completion)
	 */
	static final String COMPUTER_GUID = "52486C30-F0AB-48E3-9C37-37E9B28CDD7B";
	/**
	 * OSX constant virtual host guid (used by iCal attendee completion)
	 */
	static final String VIRTUALHOST_GUID = "D6DD8A10-1098-11DE-8C30-0800200C9A66";
	/**
	 * OSX constant value for attribute apple-serviceslocator
	 */
	static final HashMap<String, String> STATIC_ATTRIBUTE_MAP = new HashMap<String, String>();

	static {
		STATIC_ATTRIBUTE_MAP.put("apple-serviceslocator", COMPUTER_GUID + ':' + VIRTUALHOST_GUID + ":calendar");
	}

	// LDAP version
	// static final int LDAP_VERSION2 = 0x02;
	static final int LDAP_VERSION3 = 0x03;
	// LDAP request operations
	static final int LDAP_REQ_BIND = 0x60;
	static final int LDAP_REQ_SEARCH = 0x63;
	static final int LDAP_REQ_UNBIND = 0x42;
	static final int LDAP_REQ_ABANDON = 0x50;
	// LDAP response operations
	static final int LDAP_REP_BIND = 0x61;
	static final int LDAP_REP_SEARCH = 0x64;
	static final int LDAP_REP_RESULT = 0x65;
	static final int LDAP_SASL_BIND_IN_PROGRESS = 0x0E;
	// LDAP return codes
	static final int LDAP_OTHER = 80;
	static final int LDAP_SUCCESS = 0;
	static final int LDAP_SIZE_LIMIT_EXCEEDED = 4;
	static final int LDAP_INVALID_CREDENTIALS = 49;
	// LDAP filter code
	static final int LDAP_FILTER_AND = 0xa0;
	static final int LDAP_FILTER_OR = 0xa1;
	static final int LDAP_FILTER_NOT = 0xa2;
	// LDAP filter operators
	static final int LDAP_FILTER_SUBSTRINGS = 0xa4;
	//static final int LDAP_FILTER_GE = 0xa5;
	//static final int LDAP_FILTER_LE = 0xa6;
	static final int LDAP_FILTER_PRESENT = 0x87;
	//static final int LDAP_FILTER_APPROX = 0xa8;
	static final int LDAP_FILTER_EQUALITY = 0xa3;
	// LDAP filter mode
	static final int LDAP_SUBSTRING_INITIAL = 0x80;
	static final int LDAP_SUBSTRING_ANY = 0x81;
	static final int LDAP_SUBSTRING_FINAL = 0x82;
	// BER data types
	static final int LBER_ENUMERATED = 0x0a;
	static final int LBER_SET = 0x31;
	static final int LBER_SEQUENCE = 0x30;
	// LDAP search scope
	static final int SCOPE_BASE_OBJECT = 0;
	//static final int SCOPE_ONE_LEVEL = 1;
	//static final int SCOPE_SUBTREE = 2;
	/**
	 * For some unknown reason parseIntWithTag is private !
	 */
	static final Method PARSE_INT_WITH_TAG_METHOD;

	static {
		try {
			PARSE_INT_WITH_TAG_METHOD = BerDecoder.class.getDeclaredMethod("parseIntWithTag", int.class);
			PARSE_INT_WITH_TAG_METHOD.setAccessible(true);
		} catch (NoSuchMethodException e) {
			//log.error("LOG_UNABLE_TO_GET_PARSEINTWITHTAG", e);
			throw new RuntimeException(e);
		}
	}
}
