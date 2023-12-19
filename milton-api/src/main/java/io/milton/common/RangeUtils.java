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
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Helper class for Content-Range header.
 * @author brad
 */
public class RangeUtils {

    private RangeUtils() {
    }

    /**
     * Converts start/finish/length to http supported range format.
     * @param start Start of the range.
     * @param finish End of the range.
     * @param totalLength Total length or *
     * @return range in format 'bytes 0-200/5'
     */
    public static String toRangeString(long start, long finish, Long totalLength) {
        String l = totalLength == null ? "*" : totalLength.toString();

        String s;
        if (finish > -1) {
            s = "bytes " + start + "-" + finish + "/" + l;
        } else {
            long wrotetill = totalLength == null ? 0 : totalLength - 1;
            //The end position starts counting at zero. So subtract 1
            s = "bytes " + start + "-" + wrotetill + "/" + l;
        }
        return s;
    }

    /**
     * Writes ranges from InputStream to OutputStream.
     * @param in InputStream
     * @param ranges collection of ranges to write from input to output.
     * @param responseOut OutputStream to write to.
     * @throws IOException in case of IO exception.
     */
    public static void writeRanges(InputStream in, List<Range> ranges, OutputStream responseOut) throws IOException {
        try {
            long pos = 0;
            for (Range r : ranges) {
                long skip = r.getStart() - pos;
                in.skip(skip);
                Long length = r.getLength();
                if (length == null) { // will return null if cant calculate
                    throw new IOException("Unable to write range because either start or finish index are not provided: " + r);
                }
                sendBytes(in, responseOut, length);
                pos = r.getFinish();
            }
        } finally {
            StreamUtils.close(in);
        }
    }

    /**
     * Sends bytes from InputStream to OutputStream.
     * @param in InputStream to send bytes from.
     * @param out OutputStream to send bytes to.
     * @param length Length of the bytes to send.
     * @throws IOException in case of IO exception.
     */
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

    /**
     * Writes range from InputStream to OutputStream.
     * @param in InputStream
     * @param range Range to write from input to output.
     * @param responseOut OutputStream to write to.
     * @throws IOException in case of IO exception.
     */
    public static void writeRange(InputStream in, Range range, OutputStream responseOut) throws IOException {
        if (range != null) {
            if (range.getStart() != null) {
                long skip = range.getStart();
                in.skip(skip);
            }
            if (range.getFinish() != null) {
                long length = range.getFinish() - range.getStart() + 1;
                sendBytes(in, responseOut, length);
            } else {
                IOUtils.copy(in, responseOut);
            }
        } else {
            IOUtils.copy(in, responseOut);
        }
    }

}
