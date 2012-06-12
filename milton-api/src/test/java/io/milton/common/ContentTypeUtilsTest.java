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

package io.milton.common;

import io.milton.common.ContentTypeUtils;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class ContentTypeUtilsTest extends TestCase {
    
    public ContentTypeUtilsTest(String testName) {
        super(testName);
    }

    public void testFindContentTypes_Doc() {
        assertEquals( "application/msword", ContentTypeUtils.findContentTypes( "abc.doc"));
    }

    public void testFindContentTypes_Html() {
        assertEquals( "text/html", ContentTypeUtils.findContentTypes( "abc.html"));
    }

    public void testFindContentTypes_Txt() {
        assertEquals( "text/plain", ContentTypeUtils.findContentTypes( "abc.txt"));
    }


    public void testFindContentTypes_File() {
    }

    public void testFindAcceptableContentType() {
    }

}
