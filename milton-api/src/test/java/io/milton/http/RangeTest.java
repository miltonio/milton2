/*
 *
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
package io.milton.http;

import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class RangeTest extends TestCase {
    
    public RangeTest(String testName) {
        super(testName);
    }

    public void testRangeParseWithSpace() {
        Range r = Range.parse(" 123-456");
        assertEquals(123, r.getStart().longValue());
        assertEquals(456, r.getFinish().longValue());
    }
}
