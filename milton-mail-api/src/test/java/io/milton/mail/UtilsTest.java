/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package io.milton.mail;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author brad
 */
public class UtilsTest {
    

    @Test
    public void testClose_OutputStream() {
    }

    @Test
    public void testClose_InputStream() {
    }

    @Test
    public void testParseContentId() {
        String s = "<abc>";
        s = Utils.parseContentId(s);
        assertEquals("abc", s);
    }

}
