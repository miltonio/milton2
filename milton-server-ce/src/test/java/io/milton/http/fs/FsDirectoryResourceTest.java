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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.milton.http.fs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author brad
 */
public class FsDirectoryResourceTest {
	
	

	@Test
	public void testCreateCollection() {
	}

	@Test
	public void testChild() {
	}

	@Test
	public void testGetChildren() {
	}

	@Test
	public void testCheckRedirect() {
	}

	@Test
	public void testCreateNew() throws Exception {
	}

	@Test
	public void testCreateAndLock() throws Exception {
	}

	@Test
	public void testSendContent() throws Exception {
	}

	@Test
	public void testGetMaxAgeSeconds() {
	}

	@Test
	public void testGetContentType() {
	}

	@Test
	public void testGetContentLength() {
	}

	@Test
	public void testInsertSsoPrefix() {
		String s = FsDirectoryResource.insertSsoPrefix("http://test.com/folder/file", "xxxyyy");
		System.out.println(s);
		assertEquals("http://test.com/xxxyyy/folder/file", s);
	}
}
