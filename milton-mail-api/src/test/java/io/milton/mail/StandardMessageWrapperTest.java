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
public class StandardMessageWrapperTest extends TestCase {
    
    public StandardMessageWrapperTest(String testName) {
        super(testName);
    }

    public void test() {
        StandardMessageImpl sm = new StandardMessageImpl();
        sm.setText("text");
        sm.setHtml("html");
        StandardMessageWrapper wrapper = new StandardMessageWrapper(sm);
        assertEquals("text", wrapper.getText());
        assertEquals("html", wrapper.getHtml());
        wrapper.setText("text2");
        wrapper.setHtml("html2");
        assertEquals("text2", wrapper.getText());
        assertEquals("html2", wrapper.getHtml());

    }


}

