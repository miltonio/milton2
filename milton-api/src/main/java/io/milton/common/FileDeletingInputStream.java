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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An inputstream to read a file, and to delete the file when this stream is closed
 *
 * This is useful for situations where you are using a local file to buffer the contents
 * of remote data, and want to ensure that the temporary local file is deleted when
 * it is no longer being used
 *
 * @author brad
 */
public class FileDeletingInputStream extends InputStream{

    private static Logger log = LoggerFactory.getLogger(FileDeletingInputStream.class);

    private File tempFile;
    private InputStream wrapped;

    public FileDeletingInputStream( File tempFile ) throws FileNotFoundException {
        this.tempFile = tempFile;
        wrapped = new FileInputStream( tempFile );
    }

    @Override
    public int read() throws IOException {
        return wrapped.read();
    }

    @Override
    public int read( byte[] b ) throws IOException {
        return wrapped.read( b );
    }

    @Override
    public int read( byte[] b, int off, int len ) throws IOException {
        return wrapped.read( b, off, len );
    }

    @Override
    public synchronized void reset() throws IOException {
        wrapped.reset();
    }

    @Override
    public void close() throws IOException {
        try{
            wrapped.close();
        } finally {
            if(!tempFile.delete()) {
                log.error("Failed to delete: " + tempFile.getAbsolutePath());
            } else {
                tempFile = null;
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if( tempFile != null && tempFile.exists() ) {
            log.error("temporary file was not deleted. Was close called on the inputstream? Will attempt to delete: " + tempFile.getAbsolutePath());
            if( !tempFile.delete()) {
                log.error("Still couldnt delete temporary file: " + tempFile.getAbsolutePath());
            }
        }
        super.finalize();
    }


}
