/*
 * Copyright (C) 2012 McEvoy Software Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.milton.http.http11;

import io.milton.http.Request;
import io.milton.resource.Resource;
import junit.framework.TestCase;

import static org.easymock.classextension.EasyMock.*;

/**
 *
 * @author brad
 */
public class MatchHelperTest extends TestCase {

	MatchHelper matchHelper;
	JustCopyTheUniqueIdETagGenerator eTagGenerator;
	Request request;
	Resource resource;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		eTagGenerator = new JustCopyTheUniqueIdETagGenerator();
		matchHelper = new MatchHelper(eTagGenerator);
		resource = createMock(Resource.class);
		request = createMock(Request.class);
	}

	/**
	 * Test of checkIfMatch method, of class MatchHelper.
	 */
	public void testCheckIfMatch_DoesMatch_SingleValue() {
		expect(resource.getUniqueId()).andReturn("X");
		expect(request.getIfMatchHeader()).andReturn("X");
		replay(resource, request);
		boolean result = matchHelper.checkIfMatch(resource, request);
		verify(resource, request);
		assertTrue(result);
	}

	public void testCheckIfMatch_DoesMatch_Star() {
		expect(resource.getUniqueId()).andReturn("X");
		expect(request.getIfMatchHeader()).andReturn("*");
		replay(resource, request);
		boolean result = matchHelper.checkIfMatch(resource, request);
		verify(resource, request);
		assertTrue(result);
	}

	public void testCheckIfMatch_DoesMatch_Star_NullResource() {
		expect(request.getIfMatchHeader()).andReturn("*");
		replay(resource, request);
		boolean result = matchHelper.checkIfMatch(null, request);
		verify(resource, request);
		assertFalse(result);
	}
	
	
	public void testCheckIfMatch_DoesMatch_MultiValues() {
		expect(resource.getUniqueId()).andReturn("X");
		expect(request.getIfMatchHeader()).andReturn("X, Y");
		replay(resource, request);
		boolean result = matchHelper.checkIfMatch(resource, request);
		verify(resource, request);
		assertTrue(result);
	}

	public void testCheckIfMatch_DoesNotMatch() {
		expect(resource.getUniqueId()).andReturn("X");
		expect(request.getIfMatchHeader()).andReturn("Y");
		replay(resource, request);
		boolean result = matchHelper.checkIfMatch(resource, request);
		verify(resource, request);
		assertFalse(result);
	}

	public void testCheckIfMatch_NullRequest() {
		expect(request.getIfMatchHeader()).andReturn(null);
		replay(request);
		boolean result = matchHelper.checkIfMatch(resource, request);
		verify(request);
		assertTrue(result); // should be true because we want processing to continue
	}

	public void testCheckIfMatch_NullUniqueId() {
		expect(resource.getUniqueId()).andReturn(null);
		expect(request.getIfMatchHeader()).andReturn("X");
		replay(resource, request);
		boolean result = matchHelper.checkIfMatch(resource, request);
		verify(resource, request);
		assertFalse(result);
	}

//********	
	public void testCheckIfNoneMatch_DoesNotMatch_SingleValue() {
		expect(resource.getUniqueId()).andReturn("X");
		expect(request.getIfNoneMatchHeader()).andReturn("Y");
		replay(resource, request);
		boolean result = matchHelper.checkIfNoneMatch(resource, request);
		verify(resource, request);
		assertFalse(result);
	}

	/**
	 * If-none-match with a star is intended to ensure that there is no resource
	 * at the given url. If there is any resource the the process should fail,
	 * ie
	 *
	 */
	public void testCheckIfNoneMatch_DoesNotMatch_Star_TrueIfResource() {
		expect(request.getIfNoneMatchHeader()).andReturn("*");
		replay(request);
		boolean result = matchHelper.checkIfNoneMatch(resource, request);
		verify(request);
		assertTrue(result);
	}

	public void testCheckIfNoneMatch_DoesNotMatch_Star_FalseIfNoResource() {
		expect(request.getIfNoneMatchHeader()).andReturn("*");
		replay(request);
		boolean result = matchHelper.checkIfNoneMatch(null, request);
		verify(request);
		assertFalse(result);
	}

	public void testCheckIfNoneMatch_DoesNotMatch_MultiValues() {
		expect(resource.getUniqueId()).andReturn("X");
		expect(request.getIfNoneMatchHeader()).andReturn("Y, Z");
		replay(resource, request);
		boolean result = matchHelper.checkIfNoneMatch(resource, request);
		verify(resource, request);
		assertFalse(result);
	}

	public void testCheckIfNoneMatch_DoesMatch() {
		expect(resource.getUniqueId()).andReturn("X");
		expect(request.getIfNoneMatchHeader()).andReturn("X");
		replay(resource, request);
		boolean result = matchHelper.checkIfNoneMatch(resource, request);
		verify(resource, request);
		assertTrue(result);
	}

	public void testCheckIfNoneMatch_NullRequest() {
		expect(request.getIfNoneMatchHeader()).andReturn(null);
		replay(request);
		boolean result = matchHelper.checkIfNoneMatch(resource, request);
		verify(request);
		assertFalse(result);
	}

	public void testCheckIfNoneMatch_NullUniqueId() {
		expect(resource.getUniqueId()).andReturn(null);
		expect(request.getIfNoneMatchHeader()).andReturn("X");
		replay(resource, request);
		boolean result = matchHelper.checkIfNoneMatch(resource, request);
		verify(resource, request);
		assertFalse(result);
	}
	
	
	public void test_CheckIfRange_NoHeader() {	
		expect(request.getIfRangeHeader() ).andReturn(null);
		replay(resource, request);
		boolean result = matchHelper.checkIfRange(resource, request);
		verify(resource, request);
		assertTrue(result);
	}	
	
	public void test_CheckIfRange_Matches() {
		expect(resource.getUniqueId()).andReturn("X");
		expect(request.getIfRangeHeader() ).andReturn("X");
		replay(resource, request);
		boolean result = matchHelper.checkIfRange(resource, request);
		verify(resource, request);
		assertTrue(result);
	}		
	
	public void test_CheckIfRange_NotMatches() {
		expect(resource.getUniqueId()).andReturn("X");
		expect(request.getIfRangeHeader() ).andReturn("Y");
		replay(resource, request);
		boolean result = matchHelper.checkIfRange(resource, request);
		verify(resource, request);
		assertFalse(result);
	}		
}
