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
