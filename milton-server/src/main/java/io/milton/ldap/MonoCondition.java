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
	public boolean isMatch(LdapContact contact) {
		String actualValue = propertyMapper.getLdapPropertyValue(attributeName, contact);
		return (operator == Operator.IsNull && actualValue == null)
				|| (operator == Operator.IsFalse && "false".equals(actualValue))
				|| (operator == Operator.IsTrue && "true".equals(actualValue));
	}
}