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

package io.milton.http.http11;

import io.milton.http.Range;
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
	
	public void testGetRange_first_half() {
		List<Range> ranges = partialGetHelper.getRanges("bytes=100-");
		assertNotNull(ranges);
		assertEquals(1, ranges.size());
		Range r = ranges.get(0);
		assertEquals(100, r.getStart().intValue());
		assertNull(r.getFinish());
	}	

	public void testGetRange_second_half() {
		List<Range> ranges = partialGetHelper.getRanges("bytes=-100");
		assertNotNull(ranges);
		assertEquals(1, ranges.size());
		Range r = ranges.get(0);
		assertNull(r.getStart());
		assertEquals(100, r.getFinish().intValue());		
	}	
	
	
	public void testGetRange_single() {
		List<Range> ranges = partialGetHelper.getRanges("bytes=0-499");
		assertNotNull(ranges);
		assertEquals(1, ranges.size());
		Range r = ranges.get(0);
		assertEquals(0l, r.getStart().longValue());
		assertEquals(499l, r.getFinish().longValue());
	}
	
	public void testGetRange_multi() {
		List<Range> ranges = partialGetHelper.getRanges("bytes=0-499,1000-1500,2000-2500");
		assertNotNull(ranges);
		assertEquals(3, ranges.size());
		assertEquals(0, ranges.get(0).getStart().intValue());
		assertEquals(499, ranges.get(0).getFinish().intValue());
		assertEquals(1000, ranges.get(1).getStart().intValue());
		assertEquals(1500, ranges.get(1).getFinish().intValue());
		assertEquals(2000, ranges.get(2).getStart().intValue());
		assertEquals(2500, ranges.get(2).getFinish().intValue());	
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
