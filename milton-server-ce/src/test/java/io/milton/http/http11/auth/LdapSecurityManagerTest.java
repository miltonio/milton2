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
