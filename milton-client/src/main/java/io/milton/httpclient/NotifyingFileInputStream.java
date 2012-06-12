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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

class NotifyingFileInputStream extends InputStream {
	private InputStream fin;
	private final InputStream wrapped;
    private final ProgressListener listener;
    private final String fileName;
    private long pos;
    private Long totalLength;
    // the system time we last notified the progress listener
    private long timeLastNotify;
    private long bytesSinceLastNotify;

    public NotifyingFileInputStream(File f, ProgressListener listener) throws FileNotFoundException, IOException {
        this.fin = FileUtils.openInputStream(f);
		this.wrapped = new BufferedInputStream(fin);
        this.listener = listener;
        this.totalLength = f.length();
        this.fileName = f.getAbsolutePath();
        this.timeLastNotify = System.currentTimeMillis();
    }
	
	/**
	 * 
	 * @param in - the input stream containing file data
	 * @param length - maybe null if unknown
	 * @param path
	 * @param listener
	 * @throws IOException 
	 */
    public NotifyingFileInputStream(InputStream in, Long length, String path, ProgressListener listener) throws IOException {
        this.fin = in;
		this.wrapped = new BufferedInputStream(fin);
        this.listener = listener;
        this.totalLength = length;
        this.fileName = path;
        this.timeLastNotify = System.currentTimeMillis();
    }	

    @Override
    public int read() throws IOException {
        increment(1);
        return wrapped.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        increment(b.length);
        return wrapped.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        increment(len);
        return wrapped.read(b, off, len);
    }

    private void increment(int len) {
        pos += len;
        notifyListener(len);
    }

    void notifyListener(int numBytes) {
		if( listener == null ) {
			return ;
		}
		listener.onRead(numBytes);
        bytesSinceLastNotify += numBytes;
        if (bytesSinceLastNotify < 1000) {
            //                log.trace( "notifyListener: not enough bytes: " + bytesSinceLastNotify);
            return;
        }
        int timeDiff = (int) (System.currentTimeMillis() - timeLastNotify);
        if (timeDiff > 10) {
            timeLastNotify = System.currentTimeMillis();
			listener.onProgress(pos, totalLength, fileName);
            bytesSinceLastNotify = 0;
        }
    }

	@Override
	public void close() throws IOException {
		IOUtils.closeQuietly(wrapped);
		IOUtils.closeQuietly(fin);
		super.close();
	}
	
	
}
