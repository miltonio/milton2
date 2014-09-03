/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.milton.http.http11.auth;

import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class HmacUtilsTest extends TestCase {

	public HmacUtilsTest(String testName) {
		super(testName);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test of calcShaHash method, of class HmacUtils.
	 */
	public void testCalcShaHash() {
		String data = "SampleIntegrationOwner2008-11-18T19:14:40.293Z";
		String key = "xBy/2CLudnBJOxOtDhDRnsDYq9HTuDVr2uCs3FMzoxXEA/Od9tOuwSC70+mIfpjeG68ZGm/PrxFf/s/CzwxF4Q==";
		String result = HmacUtils.calcShaHash(data, key);
		System.out.println(result);
		
		assertEquals(result, "_UhwvT_kY9HxiXaOjpIc_BarBkc");
		//assertEquals(result, "/UhwvT/kY9HxiXaOjpIc/BarBkc="); // this with unsafe base64
	}
}
