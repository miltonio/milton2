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

import io.milton.common.RangeUtils;
import io.milton.common.StreamUtils;
import io.milton.http.Range;
import io.milton.httpclient.zsyncclient.RangeLoader;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author brad
 */
public class LocalFileRangeLoader implements RangeLoader {

    private File file;
    private long bytesDownloaded;

    public LocalFileRangeLoader(File file) {
        this.file = file;
    }

    @Override
    public byte[] get(List<Range> rangeList) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        for (Range r : rangeList) {
            writeRange(r, bout);
        }
        //int expectedLength = calcExpectedLength(rangeList);
        byte[] bytes = bout.toByteArray();
        return bytes;
    }

    private void writeRange(Range r, ByteArrayOutputStream bout) {
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            BufferedInputStream bufIn = new BufferedInputStream(fin);
            bytesDownloaded += (r.getFinish() - r.getStart());
            RangeUtils.writeRange(bufIn, r, bout);
            //StreamUtils.readTo(bufIn, bout, true, false, r.getStart(), r.getFinish());						
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (IOException e) {
            throw new RuntimeException(e);
//		} catch(ReadingException e) {
//			throw new RuntimeException(e);
//		} catch(WritingException e) {
//			throw new RuntimeException(e);
        } finally {
            StreamUtils.close(fin);
        }
    }

    public long getBytesDownloaded() {
        return bytesDownloaded;
    }

    public static int calcExpectedLength(List<Range> rangeList) {
        int l = 0;
        for (Range r : rangeList) {
            l += (r.getFinish() - r.getStart());
        }
        return l;
    }
}
