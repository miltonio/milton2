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

public interface Condition {

    @SuppressWarnings({"JavaDoc"})
    enum Operator {
        Or, And, Not, IsEqualTo,
        IsGreaterThan, IsGreaterThanOrEqualTo,
        IsLessThan, IsLessThanOrEqualTo,
        IsNull, IsTrue, IsFalse,
        Like, StartsWith, Contains
    }	
	
	/**
	 * Append condition to buffer.
	 *
	 * @param buffer search filter buffer
	 */
	//void appendTo(StringBuilder buffer);

	/**
	 * True if condition is empty.
	 *
	 * @return true if condition is empty
	 */
	boolean isEmpty();

	/**
	 * Test if the contact matches current condition.
	 *
	 * @param contact Exchange Contact
	 * @return true if contact matches condition
	 */
	boolean isMatch(LdapContact contact) throws NotAuthorizedException, BadRequestException;
}