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

    public static void writeRanges(InputStream in, List<Range> ranges, OutputStream responseOut) throws IOException {
        try {
            InputStream bufIn = in; //new BufferedInputStream(in);
            long pos = 0;
            for (Range r : ranges) {
                long skip = r.getStart() - pos;
                bufIn.skip(skip);
                long length = r.getFinish() - r.getStart();
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
            long skip = r.getStart();
            in.skip(skip);
            long length = r.getFinish() - r.getStart();
            sendBytes(in, responseOut, length);
        } else {
            IOUtils.copy(in, responseOut);
        }
    }
}
