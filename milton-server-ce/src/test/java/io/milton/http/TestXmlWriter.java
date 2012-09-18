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

import io.milton.http.XmlWriter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import junit.framework.TestCase;

public class TestXmlWriter extends TestCase {
    public TestXmlWriter() {
    }

    public void test() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XmlWriter w = new XmlWriter(out);
        XmlWriter.Element el = w.begin("a").writeAtt("att","val");
        el.open();
        el.writeText("abc");
        el.close();
        w.flush();
        String s = out.toString();
        System.out.println("actual..");
        System.out.println(s);
        String expected = "<a att=\"val\">\nabc</a>\n";
        System.out.println("expected..");
        System.out.println(expected);
//        assertEquals(expected,s);
    }

    public void testNested() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XmlWriter w = new XmlWriter(out);
        w.begin("a")
            .begin("b")
                .prop("b1", "b1_val")
//                .prop("b2", "b2_val")
            .close()
        .close();
//            .prop("a1","a1_val");
        w.flush();
        String s = out.toString();
        System.out.println("actual: \n" + s);
    }
}
