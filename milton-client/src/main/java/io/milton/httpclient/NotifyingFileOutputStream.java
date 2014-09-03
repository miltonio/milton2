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

package io.milton.httpclient;

import io.milton.httpclient.Utils.CancelledException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author brad
 */
public class NotifyingFileOutputStream extends FileOutputStream {

    private final ProgressListener listener;
    private final String fileName;
    private final Long length;
    long pos;
    // the system time we last notified the progress listener
    long timeLastNotify = System.currentTimeMillis();
    long bytesSinceLastNotify;

    public NotifyingFileOutputStream(java.io.File f, ProgressListener listener, Long length) throws FileNotFoundException {
        super(f);
        this.length = length;
        this.listener = listener;
        this.fileName = f.getAbsolutePath();
    }

    public NotifyingFileOutputStream(java.io.File f, boolean append, ProgressListener listener, Long length) throws FileNotFoundException {
        super(f, append);
        this.length = length;
        this.listener = listener;
        this.fileName = f.getAbsolutePath();
    }


    @Override
    public void write(int b) throws IOException {
        increment(1);
        super.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        increment(b.length);
        super.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        increment(len);
        super.write(b, off, len);
    }

    private void increment(int len) throws IOException {
        pos += len;
        if (listener != null) {
            notifyListener(len);
        }
    }

    void notifyListener(int numBytes) throws IOException{
		listener.onRead(numBytes);
        if( listener.isCancelled() ) {
            throw new CancelledException();
        }
        bytesSinceLastNotify += numBytes;
        if (bytesSinceLastNotify < 1000) {
            return;
        }
        int timeDiff = (int) (System.currentTimeMillis() - timeLastNotify);
        if (timeDiff > 10) {
            timeLastNotify = System.currentTimeMillis();
			listener.onProgress(numBytes, length , fileName);
            bytesSinceLastNotify = 0;
        }
    }
}
