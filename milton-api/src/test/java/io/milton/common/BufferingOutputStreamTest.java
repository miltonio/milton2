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
