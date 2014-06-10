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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 

/**
 * An output stream which will buffer data, initially using memory up to
 * maxMemorySize, and then overflowing to a temporary file.
 *
 * To use this class you will write to it, and then close it, and then
 * call getInputStream to read the data.
 *
 * The temporary file, if it was created, will be deleted when the inputstream
 * is closed.
 *
 * @author brad
 */
public class BufferingOutputStream extends OutputStream {

    private static Logger log = LoggerFactory.getLogger( BufferingOutputStream.class );
    private ByteArrayOutputStream tempMemoryBuffer = new ByteArrayOutputStream();
    private int maxMemorySize;
    private File tempFile;
    private FileOutputStream fout;
    private BufferedOutputStream bufOut;
    private Runnable runnable;
    private long size;
    private boolean closed;

    public BufferingOutputStream( int maxMemorySize ) {
        this.maxMemorySize = maxMemorySize;
    }

    public InputStream getInputStream() {
        if( !closed )
            throw new IllegalStateException( "this output stream is not yet closed" );
        if( tempMemoryBuffer == null ) {
            FileDeletingInputStream fin;
            try {
                fin = new FileDeletingInputStream( tempFile );
            } catch( FileNotFoundException ex ) {
                throw new RuntimeException( tempFile.getAbsolutePath(), ex );
            }
            BufferedInputStream bufIn = new BufferedInputStream( fin );
            return bufIn;
        } else {
            return new ByteArrayInputStream( tempMemoryBuffer.toByteArray() );
        }
    }

    @Override
    public void write( byte[] b ) throws IOException {
        size += b.length;
        if( tempMemoryBuffer != null ) {
            tempMemoryBuffer.write( b );
        } else {
            bufOut.write( b );
        }
        checkSize();
    }

    @Override
    public void write( int b ) throws IOException {
        size++;
        if( tempMemoryBuffer != null ) {
            tempMemoryBuffer.write( b );
        } else {
            bufOut.write( b );
        }
        checkSize();
    }

    @Override
    public void write( byte[] b, int off, int len ) throws IOException {
        size += len;
        if( tempMemoryBuffer != null ) {
            tempMemoryBuffer.write( b, off, len );
        } else {
            bufOut.write( b, off, len );
        }
        checkSize();
    }

    private void checkSize() throws IOException {
        if( log.isTraceEnabled() ) {
            log.trace( "checkSize: " + size );
        }
        if( tempMemoryBuffer == null ) return;

        if( tempMemoryBuffer.size() < maxMemorySize ) return;

        tempFile = File.createTempFile( "" + System.currentTimeMillis(), ".buffer" );
        fout = new FileOutputStream( tempFile );
        bufOut = new BufferedOutputStream( fout );
        bufOut.write( tempMemoryBuffer.toByteArray() );
        tempMemoryBuffer = null;
    }

    @Override
    public void flush() throws IOException {
        if( tempMemoryBuffer != null ) {
            tempMemoryBuffer.flush();
        } else {
            bufOut.flush();
            fout.flush();
        }
    }

    @Override
    public void close() throws IOException {
        if( tempMemoryBuffer != null ) {
            tempMemoryBuffer.close();
        } else {
            bufOut.close();
            fout.close();
        }
        closed = true;
        if( runnable != null ) {
            runnable.run();
        }
    }

    public long getSize() {
        return size;
    }

    File getTempFile() {
        return tempFile;
    }

    ByteArrayOutputStream getTempMemoryBuffer() {
        return tempMemoryBuffer;
    }

    public void setOnClose( Runnable r ) {
        this.runnable = r;
    }

    /**
     * returns true if the data is completely held in memory
     *
     * @return
     */
    public boolean isCompleteInMemory() {
        return tempFile == null;
    }

    /**
     * Gets the data currently held in memory
     * 
     * @return
     */
    public byte[] getInMemoryData() {
        return this.tempMemoryBuffer.toByteArray();
    }

    @Override
    protected void finalize() throws Throwable {
        deleteTempFileIfExists();
        super.finalize();
    }

    public void deleteTempFileIfExists() {
        if( bufOut != null ) {
            IOUtils.closeQuietly(bufOut);
        }
        if( fout != null ) {
            IOUtils.closeQuietly(fout);
        }

        if( tempFile != null && tempFile.exists() ) {
            log.error( "temporary file was not deleted. Was close called on the inputstream? Will attempt to delete" );
            if( !tempFile.delete() ) {
                log.error( "Still couldnt delete temporary file: " + tempFile.getAbsolutePath() );
            }
        }

    }
}
