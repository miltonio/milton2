
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
package io.milton.common;

import io.milton.http.Range;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author brad
 */
public class RangeUtilsTest extends TestCase {

    public RangeUtilsTest(String testName) {
        super(testName);
    }

    public void xtestSendBytes_Under1k() throws Exception {
        long length = 500;
        byte[] buf = new byte[1000];
        Arrays.fill(buf, (byte) 3);
        ByteArrayInputStream in = new ByteArrayInputStream(buf);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        RangeUtils.sendBytes(in, out, length);

        assertEquals(500, out.toByteArray().length);

    }

    public void xtestSendBytes_Over1k() throws Exception {
        long length = 5000;
        byte[] buf = new byte[10000];
        Arrays.fill(buf, (byte) 3);
        ByteArrayInputStream in = new ByteArrayInputStream(buf);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        RangeUtils.sendBytes(in, out, length);

        assertEquals(5000, out.toByteArray().length);

    }

    public void xtestWriteRanges() throws IOException {
        long length = 5000;
        byte[] buf = new byte[10000];
        for (int i = 0; i < 5; i++) {
            char ch = (char) (65 + i);
            Arrays.fill(buf, i * 1000, (i + 1) * 1000, (byte) ch);
        }
        ByteArrayInputStream in = new ByteArrayInputStream(buf);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        List<Range> ranges = new ArrayList<Range>();
        ranges.add(new Range(501l, 1000l));
        ranges.add(new Range(2001l, 2500l));
        ranges.add(new Range(3001l, 3500l));

        RangeUtils.writeRanges(in, ranges, out);

        assertEquals(1500, out.toByteArray().length);

    }
    
    public void testWrite_BeyondEndOfFile() throws IOException {
        InputStream in = this.getClass().getResourceAsStream("/jquery-ui-1.8.20.custom.min.js");
        if( in == null ) {
            throw new RuntimeException("Couldnt find test file");
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOUtils.copy(in, bout);        
        System.out.println("input file length=" + bout.size());
        in = new ByteArrayInputStream(bout.toByteArray());
        
        Range r = Range.parse("30357-71179"); // one past index of last byte
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        RangeUtils.writeRange(in, r, out);
        System.out.println("testWrite_OpenRange wrote: " + out.toByteArray().length + " bytes");
        assertEquals(40822, out.toByteArray().length);
    }    
    
    
    public void testWrite_ToEndOfFile() throws IOException {
        InputStream in = this.getClass().getResourceAsStream("/jquery-ui-1.8.20.custom.min.js");
        if( in == null ) {
            throw new RuntimeException("Couldnt find test file");
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOUtils.copy(in, bout);        
        System.out.println("input file length=" + bout.size());
        in = new ByteArrayInputStream(bout.toByteArray());
        
        Range r = Range.parse("30356-71178"); // exactly end of file
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        RangeUtils.writeRange(in, r, out);
        System.out.println("testWrite_OpenRange2 wrote: " + out.toByteArray().length + " bytes");
        assertEquals(40823, out.toByteArray().length);
    }       
}
