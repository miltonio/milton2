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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class MultiCondition implements Condition {

	protected final Operator operator;
	protected final List<Condition> conditions;

	protected MultiCondition(Operator operator, Condition... conditions) {
		this.operator = operator;
		this.conditions = new ArrayList<Condition>();
		for (Condition condition : conditions) {
			if (condition != null) {
				this.conditions.add(condition);
			}
		}
	}

//	@Override
//	public void appendTo(StringBuilder buffer) {
//		boolean first = true;
//
//		for (Condition condition : conditions) {
//			if (condition != null && !condition.isEmpty()) {
//				if (first) {
//					buffer.append('(');
//					first = false;
//				} else {
//					buffer.append(' ').append(operator).append(' ');
//				}
//				condition.appendTo(buffer);
//			}
//		}
//		// at least one non empty condition
//		if (!first) {
//			buffer.append(')');
//		}
//	}

	/**
	 * Conditions list.
	 *
	 * @return conditions
	 */
	public List<Condition> getConditions() {
		return conditions;
	}

	/**
	 * Condition operator.
	 *
	 * @return operator
	 */
	public Operator getOperator() {
		return operator;
	}

	/**
	 * Add a new condition.
	 *
	 * @param condition single condition
	 */
	public void add(Condition condition) {
		if (condition != null) {
			conditions.add(condition);
		}
	}

	@Override
	public boolean isEmpty() {
		boolean isEmpty = true;
		for (Condition condition : conditions) {
			if (!condition.isEmpty()) {
				isEmpty = false;
				break;
			}
		}
		return isEmpty;
	}

	@Override
	public boolean isMatch(LdapContact contact) throws NotAuthorizedException, BadRequestException {
		if (operator == Operator.And) {
			for (Condition condition : conditions) {
				if (!condition.isMatch(contact)) {
					return false;
				}
			}
			return true;
		} else if (operator == Operator.Or) {
			for (Condition condition : conditions) {
				if (condition.isMatch(contact)) {
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}
}
