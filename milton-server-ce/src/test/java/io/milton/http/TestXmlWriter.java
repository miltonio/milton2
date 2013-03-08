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
