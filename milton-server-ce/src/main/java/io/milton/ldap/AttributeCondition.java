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
import io.milton.common.LogUtils;
import io.milton.http.exceptions.BadRequestException;
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
	public boolean isMatch(LdapContact contact) throws BadRequestException {
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
