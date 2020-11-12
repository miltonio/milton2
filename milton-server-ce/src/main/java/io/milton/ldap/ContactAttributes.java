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

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author brad
 */
public class ContactAttributes {

	public static final Set<String> CONTACT_ATTRIBUTES = new HashSet<>();

	static {
		CONTACT_ATTRIBUTES.add("imapUid");
		CONTACT_ATTRIBUTES.add("etag");
		CONTACT_ATTRIBUTES.add("urlcompname");

		CONTACT_ATTRIBUTES.add("extensionattribute1");
		CONTACT_ATTRIBUTES.add("extensionattribute2");
		CONTACT_ATTRIBUTES.add("extensionattribute3");
		CONTACT_ATTRIBUTES.add("extensionattribute4");
		CONTACT_ATTRIBUTES.add("bday");
		CONTACT_ATTRIBUTES.add("anniversary");
		CONTACT_ATTRIBUTES.add("businesshomepage");
		CONTACT_ATTRIBUTES.add("personalHomePage");
		CONTACT_ATTRIBUTES.add("cn");
		CONTACT_ATTRIBUTES.add("co");
		CONTACT_ATTRIBUTES.add("department");
		CONTACT_ATTRIBUTES.add("smtpemail1");
		CONTACT_ATTRIBUTES.add("smtpemail2");
		CONTACT_ATTRIBUTES.add("smtpemail3");
		CONTACT_ATTRIBUTES.add("facsimiletelephonenumber");
		CONTACT_ATTRIBUTES.add("givenName");
		CONTACT_ATTRIBUTES.add("homeCity");
		CONTACT_ATTRIBUTES.add("homeCountry");
		CONTACT_ATTRIBUTES.add("homePhone");
		CONTACT_ATTRIBUTES.add("homePostalCode");
		CONTACT_ATTRIBUTES.add("homeState");
		CONTACT_ATTRIBUTES.add("homeStreet");
		CONTACT_ATTRIBUTES.add("homepostofficebox");
		CONTACT_ATTRIBUTES.add("l");
		CONTACT_ATTRIBUTES.add("manager");
		CONTACT_ATTRIBUTES.add("mobile");
		CONTACT_ATTRIBUTES.add("namesuffix");
		CONTACT_ATTRIBUTES.add("nickname");
		CONTACT_ATTRIBUTES.add("o");
		CONTACT_ATTRIBUTES.add("pager");
		CONTACT_ATTRIBUTES.add("personaltitle");
		CONTACT_ATTRIBUTES.add("postalcode");
		CONTACT_ATTRIBUTES.add("postofficebox");
		CONTACT_ATTRIBUTES.add("profession");
		CONTACT_ATTRIBUTES.add("roomnumber");
		CONTACT_ATTRIBUTES.add("secretarycn");
		CONTACT_ATTRIBUTES.add("sn");
		CONTACT_ATTRIBUTES.add("spousecn");
		CONTACT_ATTRIBUTES.add("st");
		CONTACT_ATTRIBUTES.add("street");
		CONTACT_ATTRIBUTES.add("telephoneNumber");
		CONTACT_ATTRIBUTES.add("title");
		CONTACT_ATTRIBUTES.add("description");
		CONTACT_ATTRIBUTES.add("im");
		CONTACT_ATTRIBUTES.add("middlename");
		CONTACT_ATTRIBUTES.add("lastmodified");
		CONTACT_ATTRIBUTES.add("otherstreet");
		CONTACT_ATTRIBUTES.add("otherstate");
		CONTACT_ATTRIBUTES.add("otherpostofficebox");
		CONTACT_ATTRIBUTES.add("otherpostalcode");
		CONTACT_ATTRIBUTES.add("othercountry");
		CONTACT_ATTRIBUTES.add("othercity");
		CONTACT_ATTRIBUTES.add("haspicture");
		CONTACT_ATTRIBUTES.add("keywords");
		CONTACT_ATTRIBUTES.add("othermobile");
		CONTACT_ATTRIBUTES.add("otherTelephone");
		CONTACT_ATTRIBUTES.add("gender");
		CONTACT_ATTRIBUTES.add("private");
		CONTACT_ATTRIBUTES.add("sensitivity");
		CONTACT_ATTRIBUTES.add("fburl");
	}
}
