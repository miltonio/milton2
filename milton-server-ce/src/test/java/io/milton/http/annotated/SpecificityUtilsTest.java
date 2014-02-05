/*
 * Copyright 2014 McEvoy Software Ltd.
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

package io.milton.http.annotated;

import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class SpecificityUtilsTest extends TestCase {
	
	public SpecificityUtilsTest(String testName) {
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
	 * Test of sourceSpecifityIndex method, of class SpecificityUtils.
	 */
	public void testSourceSpecifityIndex() {
		int scoreLower = SpecificityUtils.sourceSpecifityIndex(BaseClass.class, EndClass.class);
		int scoreHigher = SpecificityUtils.sourceSpecifityIndex(EndClass.class, EndClass.class);
		System.out.println("scores: " + scoreLower + " < " +  scoreHigher);
		assertTrue(scoreLower < scoreHigher);
	}
	
	public static class BaseClass {
		
	}
	
	public static class MiddleClass extends BaseClass {
		
	}
	
	public static class EndClass extends MiddleClass {
		
	}
}
