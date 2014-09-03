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

package io.milton.zsync;

import io.milton.common.BufferingOutputStream;
import io.milton.common.RangeUtils;
import io.milton.http.Range;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;


/**
 * An object consisting of a Range and a stream of bytes. If the number of bytes
 * is beyond a certain threshold, the data is stored in a temporary file;
 * otherwise, it is just stored in an array.
 *
 * @author Nick
 *
 */
public class DataRange {

    /**
     * Once more than
     * <code>threshold</code> bytes are written to data, the backing store is
     * switched from a byte array to a temporary file
     */
    public static final int threshold = 1024 * 1024;
    private Range range;
    private BufferingOutputStream data;

    /**
     * Sets the range equal to the specified Range and pulls a number of bytes
     * from the InputStream equal to the size of the range
     *
     * @param range A Range object specifying the target location of the byte
     * stream
     * @param instream A stream from which the data portion will be pulled
     * @throws IOException
     */
    public DataRange(Range range, InputStream instream) throws IOException {

        this.range = range;
        this.data = new BufferingOutputStream(threshold);

        long length = range.getFinish() - range.getStart();
        RangeUtils.sendBytes(instream, data, length);

        this.data.close();

    }

    /**
     * Sets the range to the specified Range and seeks to the beginning of the
     * range in the RandomAccessFile before copying bytes
     *
     * @param range
     * @param randAccess
     * @throws IOException
     */
    public DataRange(Range range, RandomAccessFile randAccess) throws IOException {

        this.range = range;
        this.data = new BufferingOutputStream(threshold);

        randAccess.seek(range.getStart());
        long length = range.getFinish() - range.getStart();

        byte[] buffer = new byte[2048];
        int bytesLeft = (int) length;
        int bytesRead = Math.min(bytesLeft, buffer.length);

        while (bytesLeft > 0) {
            randAccess.read(buffer, 0, bytesRead);
            data.write(buffer, 0, bytesRead);
            bytesLeft -= bytesRead;
            bytesRead = Math.min(bytesLeft, buffer.length);
        }

        data.close();

    }

    @Override
    public String toString() {
        return range.toString();
    }

    public Range getRange() {

        return range;
    }

    /**
     * Returns the stream of bytes. If a temporary file is used to store data,
     * it will be deleted when the returned stream is closed.
     *
     * @return The byte stream corresponding to
     * <code>range</code>
     */
    public InputStream getInputStream() {

        return data.getInputStream();
    }

    /**
     * Returns the length of the Range (and the number of bytes in the byte
     * stream)
     *
     * @return
     */
    public long getLength() {

        return range.getFinish() - range.getStart();
    }
}
