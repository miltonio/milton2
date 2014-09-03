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
