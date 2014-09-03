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

package io.milton.http.http11.auth;

import io.milton.http.http11.auth.LdapSecurityManager;
import junit.framework.TestCase;

/**
 *
 * @author bradm
 */
public class LdapSecurityManagerTest extends TestCase {
	
	LdapSecurityManager securityManager;
	
	public LdapSecurityManagerTest(String testName) {
		super(testName);
	}
	
	@Override
	protected void setUp() throws Exception {
		securityManager = new LdapSecurityManager();
	}



	public void testAuthoriseBasic() {
		System.out.println("testAuthoriseBasic ---------------------");
		Object result = securityManager.authenticate("brad", "xxxxx");
		System.out.println(result);
		System.out.println("testAuthoriseBasic - ooooooooooooooooooooooooooooo");
	}

	public void testAuthoriseDigest() {
	}
	
}
