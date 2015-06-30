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

import io.milton.common.StreamUtils;
import io.milton.http.Range;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class RangeUtils {

    private static final Logger log = LoggerFactory.getLogger(RangeUtils.class);

    public static String toRangeString(long start, long finish, Long totalLength) {
        String l = totalLength == null ? "*" : totalLength.toString();

        String s = null;
        if (finish > -1) {
            s = "bytes " + start + "-" + finish + "/" + l;
        } else {
            long wrotetill = totalLength == null ? 0 : totalLength - 1;
            //The end position starts counting at zero. So subtract 1
            s = "bytes " + start + "-" + wrotetill + "/" + l;
        }
        return s;
    }
    
    public static void writeRanges(InputStream in, List<Range> ranges, OutputStream responseOut) throws IOException {
        try {
            InputStream bufIn = in; //new BufferedInputStream(in);
            long pos = 0;
            for (Range r : ranges) {
                long skip = r.getStart() - pos;
                bufIn.skip(skip);
                Long length = r.getLength();
                if (length == null) { // will return null if cant calculate
                    throw new IOException("Unable to write range because either start or finish index are not provided: " + r);
                }
                sendBytes(bufIn, responseOut, length);
                pos = r.getFinish();
            }
        } finally {
            StreamUtils.close(in);
        }
    }

    public static void sendBytes(InputStream in, OutputStream out, long length) throws IOException {
        long numRead = 0;
        byte[] b = new byte[1024];
        while (numRead < length) {
            long remainingBytes = length - numRead;
            int maxLength = remainingBytes > 1024 ? 1024 : (int) remainingBytes;
            int s = in.read(b, 0, maxLength);
            if (s < 0) {
                break;
            }
            numRead += s;
            out.write(b, 0, s);
        }

    }

    public static void writeRange(InputStream in, Range r, OutputStream responseOut) throws IOException {
        if (r != null) {
            if( r.getStart() != null ) {
                long skip = r.getStart();
                in.skip(skip);
            }
            if (r.getFinish() != null) {
                long length = r.getFinish() - r.getStart() + 1;
                sendBytes(in, responseOut, length);
            } else {
                IOUtils.copy(in, responseOut);
            }
        } else {
            IOUtils.copy(in, responseOut);
        }
    }

}
