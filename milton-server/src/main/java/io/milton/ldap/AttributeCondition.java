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
import io.milton.common.LogUtils;
import io.milton.ldap.Condition.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class AttributeCondition implements Condition {

	private static final Logger log = LoggerFactory.getLogger(AttributeCondition.class);
	
	private final LdapPropertyMapper propertyMapper;
	private final String attributeName;
	private final Operator operator;
	private final String value;
	private boolean isIntValue;

	public AttributeCondition(LdapPropertyMapper propertyMapper, String attributeName, Operator operator, String value) {
		this.propertyMapper = propertyMapper;
		this.attributeName = attributeName;
		this.operator = operator;
		this.value = value;
	}

	public AttributeCondition(LdapPropertyMapper propertyMapper, String attributeName, Operator operator, int value) {
		this(propertyMapper, attributeName, operator, String.valueOf(value));
		isIntValue = true;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	/**
	 * Get attribute name.
	 *
	 * @return attribute name
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * Condition value.
	 *
	 * @return value
	 */
	public String getValue() {
		return value;
	}
//
//	public void appendTo(StringBuilder buffer) {
//		Field field = Field.get(attributeName);
//		buffer.append('"').append(field.getUri()).append('"');
//		buffer.append(Conditions.OPERATOR_MAP.get(operator));
//		//noinspection VariableNotUsedInsideIf
//		if (field.cast != null) {
//			buffer.append("CAST (\"");
//		} else if (!isIntValue && !field.isIntValue()) {
//			buffer.append('\'');
//		}
//		if (Operator.Like == operator) {
//			buffer.append('%');
//		}
//		if ("urlcompname".equals(field.alias)) {
//			buffer.append(StringUtil.encodeUrlcompname(StringUtil.davSearchEncode(value)));
//		} else if (field.isIntValue()) {
//			// check value
//			try {
//				Integer.parseInt(value);
//				buffer.append(value);
//			} catch (NumberFormatException e) {
//				// invalid value, replace with 0
//				buffer.append('0');
//			}
//		} else {
//			buffer.append(StringUtil.davSearchEncode(value));
//		}
//		if (Operator.Like == operator || Operator.StartsWith == operator) {
//			buffer.append('%');
//		}
//		if (field.cast != null) {
//			buffer.append("\" as '").append(field.cast).append("')");
//		} else if (!isIntValue && !field.isIntValue()) {
//			buffer.append('\'');
//		}
//	}

	@Override
	public boolean isMatch(LdapContact contact) {
		String lowerCaseValue = value.toLowerCase();
		String actualValue = propertyMapper.getLdapPropertyValue(attributeName, contact);
		Operator actualOperator = operator;
		// patch for iCal or Lightning search without galLookup
		if (actualValue == null && ("givenName".equals(attributeName) || "sn".equals(attributeName))) {
			actualValue = propertyMapper.getLdapPropertyValue("cn", contact);
			actualOperator = Operator.Like;
		}
		if (actualValue == null) {
			return false;
		}
		actualValue = actualValue.toLowerCase();
		boolean b = (actualOperator == Operator.IsEqualTo && actualValue.equals(lowerCaseValue))
				|| (actualOperator == Operator.Like && actualValue.contains(lowerCaseValue))
				|| (actualOperator == Operator.StartsWith && actualValue.startsWith(lowerCaseValue));
		LogUtils.trace(log, "isMatch: result:", b, "attributeName:",attributeName, "operator:", actualOperator, "test value", actualValue, "query value", lowerCaseValue);
		return b;
	}
}
