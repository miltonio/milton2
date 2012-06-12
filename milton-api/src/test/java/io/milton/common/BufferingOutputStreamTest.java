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

import io.milton.common.BufferingOutputStream;
import io.milton.common.StreamUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class BufferingOutputStreamTest extends TestCase {
    
    public BufferingOutputStreamTest(String testName) {
        super(testName);
    }

    public void testWriteByte() throws Exception {
        BufferingOutputStream out = new BufferingOutputStream( 10);
        assertNull(out.getTempFile());
        out.write( 1);
        assertNull(out.getTempFile());
        assertEquals( 1, out.getTempMemoryBuffer().size());
    }

    public void test_WriteArray() throws Exception {
        BufferingOutputStream out = new BufferingOutputStream( 10);
        out.write( new byte[5]);
        assertEquals( 5, out.getTempMemoryBuffer().size());

        out.write( new byte[5],1,2);
        assertEquals( 7, out.getTempMemoryBuffer().size());
    }

    public void test_MemoryRead() throws Exception {
        BufferingOutputStream out = new BufferingOutputStream( 10);
        out.write( new byte[9]);
        assertNotNull( out.getTempMemoryBuffer());
        assertNull( out.getTempFile());

        out.close();

        InputStream in = out.getInputStream();
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        StreamUtils.readTo( in, out2 );
        byte[] arr = out2.toByteArray();
        assertEquals( 9, arr.length);

    }

    public void test_TransitionToFile() throws Exception {
        BufferingOutputStream out = new BufferingOutputStream( 10);
        out.write( new byte[10]);
        assertNull( out.getTempMemoryBuffer());
        assertNotNull( out.getTempFile());
        // check we can still write to the stream
        out.write( new byte[10]);

        // now get the file and ensure it exists
        File f = out.getTempFile();
        assertTrue( f.exists());

        out.close();
        InputStream in = out.getInputStream();
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        StreamUtils.readTo( in, out2 );
        byte[] arr = out2.toByteArray();
        in.close();
        assertEquals( 20, arr.length);
        assertFalse( f.exists());
    }

}
