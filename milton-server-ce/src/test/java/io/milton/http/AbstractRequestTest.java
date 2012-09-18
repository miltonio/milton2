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


package io.milton.http;

import io.milton.http.AbstractRequest;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class AbstractRequestTest extends TestCase {
    
    public AbstractRequestTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testStripToPath() {
        String s = AbstractRequest.stripToPath("http://abc:80/my/path");
        assertEquals("/my/path", s);
    }

    public void testStripToPathWithQueryString() {
        String s = AbstractRequest.stripToPath("http://abc:80/my/path?x=y");
        assertEquals("/my/path", s);
    }

}
