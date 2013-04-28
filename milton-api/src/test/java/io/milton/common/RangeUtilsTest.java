
/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This file is part of io.milton_milton-api_jar_2.0.0-SNAPSHOT.
 * io.milton_milton-api_jar_2.0.0-SNAPSHOT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * io.milton_milton-api_jar_2.0.0-SNAPSHOT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
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
import static junit.framework.Assert.assertEquals;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class RangeUtilsTest extends TestCase {

    public RangeUtilsTest(String testName) {
        super(testName);
    }

    public void testSendBytes_Under1k() throws Exception {
        long length = 500;
        byte[] buf = new byte[1000];
        Arrays.fill(buf, (byte) 3);
        ByteArrayInputStream in = new ByteArrayInputStream(buf);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        RangeUtils.sendBytes(in, out, length);

        assertEquals(500, out.toByteArray().length);

    }

    public void testSendBytes_Over1k() throws Exception {
        long length = 5000;
        byte[] buf = new byte[10000];
        Arrays.fill(buf, (byte) 3);
        ByteArrayInputStream in = new ByteArrayInputStream(buf);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        RangeUtils.sendBytes(in, out, length);

        assertEquals(5000, out.toByteArray().length);

    }

    public void testWriteRanges() throws IOException {
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
    
    public void testWrite_OpenRange() throws IOException {
        InputStream in = this.getClass().getResourceAsStream("/jquery-ui-1.8.20.custom.min.js");
        if( in == null ) {
            throw new RuntimeException("Couldnt find test file");
        }
        Range r = Range.parse("30357-71179");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RangeUtils.writeRange(in, r, out);
        assertEquals(40822, out.toByteArray().length);

    }    
}
