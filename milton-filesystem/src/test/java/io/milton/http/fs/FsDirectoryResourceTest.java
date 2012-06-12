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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.milton.http.fs;

import io.milton.http.fs.FsDirectoryResource;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class FsDirectoryResourceTest extends TestCase {
	
	public FsDirectoryResourceTest(String testName) {
		super(testName);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testCreateCollection() {
	}

	public void testChild() {
	}

	public void testGetChildren() {
	}

	public void testCheckRedirect() {
	}

	public void testCreateNew() throws Exception {
	}

	public void testCreateAndLock() throws Exception {
	}

	public void testSendContent() throws Exception {
	}

	public void testGetMaxAgeSeconds() {
	}

	public void testGetContentType() {
	}

	public void testGetContentLength() {
	}

	public void testInsertSsoPrefix() {
		String s = FsDirectoryResource.insertSsoPrefix("http://test.com/folder/file", "xxxyyy");
		System.out.println(s);
		assertEquals("http://test.com/xxxyyy/folder/file", s);
	}
}
