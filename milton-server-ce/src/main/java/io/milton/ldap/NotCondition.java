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

/**
 *
 * @author brad
 */
public class NotCondition implements Condition {

	protected final Condition condition;

	protected NotCondition(Condition condition) {
		this.condition = condition;
	}

	@Override
	public boolean isEmpty() {
		return condition.isEmpty();
	}

	@Override
	public boolean isMatch(LdapContact contact) throws NotAuthorizedException, BadRequestException {
		return !condition.isMatch(contact);
	}

//	public void appendTo(StringBuilder buffer) {
//		buffer.append("(Not ");
//		condition.appendTo(buffer);
//		buffer.append(')');
//	}
}