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
