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
package io.milton.http.annotated;

import static junit.framework.Assert.assertEquals;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class AnnotationResourceFactoryTest extends TestCase {
	
	public AnnotationResourceFactoryTest(String testName) {
		super(testName);
	}

	public void testStripContext_null() {
		AnnotationResourceFactory arf = new AnnotationResourceFactory();
		arf.setContextPath(null);
		String s = arf.stripContext("/something/somewhere.doc");
		System.out.println("s1: " + s);
		assertEquals("/something/somewhere.doc", s);
	}	

	public void testStripContext_empty() {
		AnnotationResourceFactory arf = new AnnotationResourceFactory();
		arf.setContextPath(null);
		String s = arf.stripContext("/something/somewhere.doc");
		System.out.println("s1: " + s);
		assertEquals("/something/somewhere.doc", s);
	}	
	
	public void testStripContext_root() {
		AnnotationResourceFactory arf = new AnnotationResourceFactory();
		arf.setContextPath("/");
		String s = arf.stripContext("/something/somewhere.doc");
		System.out.println("s1: " + s);
		assertEquals("/something/somewhere.doc", s);
	}
	
	public void testStripContext_nonroot_path() {
		AnnotationResourceFactory arf = new AnnotationResourceFactory();
		arf.setContextPath("/something");
		String s = arf.stripContext("/something/somewhere.doc");
		System.out.println("s2: " + s);
		assertEquals("/somewhere.doc", s);
	}	
	
	public void testStripContext_nonroot_name() {
		AnnotationResourceFactory arf = new AnnotationResourceFactory();
		arf.setContextPath("something");
		String s = arf.stripContext("/something/somewhere.doc");
		System.out.println("s3: " + s);
		assertEquals("/somewhere.doc", s);
	}		
}
