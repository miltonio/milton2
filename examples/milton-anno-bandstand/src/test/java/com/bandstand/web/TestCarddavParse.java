/*
 * Copyright 2013 McEvoy Software Ltd.
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
package com.bandstand.web;


import java.io.IOException;
import junit.framework.TestCase;
import net.sourceforge.cardme.engine.VCardEngine;
import net.sourceforge.cardme.io.VCardWriter;
import net.sourceforge.cardme.vcard.VCard;

/**
 *
 * @author brad
 */
public class TestCarddavParse extends TestCase {

    public TestCarddavParse(String testName) {
        super(testName);
    }

    public void test() throws IOException {
        String s = "BEGIN:VCARD\n"
                + "VERSION:3.0\n"
                + "PRODID:-//Inverse inc.//SOGo Connector 1.0//EN\n"
                + "UID:7\n"
                + "N:New;New\n"
                + "FN:New New\n"
                + "TEL;TYPE=work:12345123123\n"
                + "X-MOZILLA-HTML:FALSE\n"
                + "END:VCARD\n";
            VCardEngine cardEngine = new VCardEngine();
            VCard vcard = cardEngine.parse(s);
            
            VCardWriter writer = new VCardWriter();
            writer.setVCard(vcard);
            System.out.println( writer.buildVCardString() );
            
            
            assertNotNull(vcard.getName());
        
    }
}
