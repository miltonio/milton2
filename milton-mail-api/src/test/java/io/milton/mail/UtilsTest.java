/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package io.milton.mail;

import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class UtilsTest extends TestCase {
    
    public UtilsTest(String testName) {
        super(testName);
    }

    public void testClose_OutputStream() {
    }

    public void testClose_InputStream() {
    }

    public void testParseContentId() {
        String s = "<abc>";
        s = Utils.parseContentId(s);
        assertEquals("abc", s);
    }

}
