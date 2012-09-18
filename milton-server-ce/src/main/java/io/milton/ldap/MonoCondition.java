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
import io.milton.resource.LdapContact;

/**
 *
 * @author brad
 */
public class MonoCondition implements Condition {
	private LdapPropertyMapper propertyMapper;
	private final String attributeName;
	private final Operator operator;

	protected MonoCondition(LdapPropertyMapper propertyMapper, String attributeName, Operator operator) {
		this.attributeName = attributeName;
		this.operator = operator;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean isMatch(LdapContact contact) throws BadRequestException {
		String actualValue = propertyMapper.getLdapPropertyValue(attributeName, contact);
		return (operator == Operator.IsNull && actualValue == null)
				|| (operator == Operator.IsFalse && "false".equals(actualValue))
				|| (operator == Operator.IsTrue && "true".equals(actualValue));
	}
}