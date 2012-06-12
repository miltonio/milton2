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

package io.milton.http.http11;

import io.milton.http.http11.Http11ResponseHandler;
import io.milton.http.http11.PartialGetHelper;
import io.milton.http.Range;
import io.milton.http.entity.PartialEntity;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import static org.easymock.classextension.EasyMock.*;

/**
 *
 * @author brad
 */
public class PartialGetHelperTest extends TestCase {
	
	PartialGetHelper partialGetHelper;
	Http11ResponseHandler responseHandler;
	
	public PartialGetHelperTest(String testName) {
		super(testName);
	}
	
	@Override
	protected void setUp() throws Exception {
		responseHandler = createMock(Http11ResponseHandler.class);
		partialGetHelper = new PartialGetHelper(responseHandler);
	}

	public void testGetRange_null() {
		List<Range> ranges = partialGetHelper.getRanges(null);
		assertNull(ranges);
		ranges = partialGetHelper.getRanges("");
		assertNull(ranges);
	}
	
	public void testGetRange_single() {
		List<Range> ranges = partialGetHelper.getRanges("bytes=0-499");
		assertNotNull(ranges);
		assertEquals(1, ranges.size());
		Range r = ranges.get(0);
		assertEquals(0, r.getStart());
		assertEquals(499, r.getFinish());
	}
	
	public void testGetRange_multi() {
		List<Range> ranges = partialGetHelper.getRanges("bytes=0-499,1000-1500,2000-2500");
		assertNotNull(ranges);
		assertEquals(3, ranges.size());
		assertEquals(0, ranges.get(0).getStart());
		assertEquals(499, ranges.get(0).getFinish());
		assertEquals(1000, ranges.get(1).getStart());
		assertEquals(1500, ranges.get(1).getFinish());
		assertEquals(2000, ranges.get(2).getStart());
		assertEquals(2500, ranges.get(2).getFinish());

		
	}

	public void testGetRanges() {
	}

	public void testSendPartialContent() throws Exception {
	}

	public void testGetMaxMemorySize() {
	}

	public void testSetMaxMemorySize() {
	}

	
}
