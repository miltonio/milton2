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
