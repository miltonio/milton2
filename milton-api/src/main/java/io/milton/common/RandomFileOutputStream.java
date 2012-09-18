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
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * A positionable file output stream.
 * <p>
 * Threading Design : [x] Single Threaded  [ ] Threadsafe  [ ] Immutable  [ ] Isolated
 *
 * From http://stackoverflow.com/questions/825732/how-can-i-implement-an-outputstream-that-i-can-rewind
 */
public class RandomFileOutputStream extends OutputStream {

// *****************************************************************************
// INSTANCE PROPERTIES
// *****************************************************************************
    protected RandomAccessFile randomFile;                             // the random file to write to
    protected boolean sync;                                   // whether to synchronize every write

    public RandomFileOutputStream(String fnm) throws IOException {
        this(fnm, false);
    }

    public RandomFileOutputStream(String fnm, boolean syn) throws IOException {
        this(new File(fnm), syn);
    }

    public RandomFileOutputStream(File fil) throws IOException {
        this(fil, false);
    }

    public RandomFileOutputStream(File fil, boolean syn) throws IOException {
        super();
        randomFile = new RandomAccessFile(fil, "rw");
        sync = syn;
    }

// *****************************************************************************
// INSTANCE METHODS - OUTPUT STREAM IMPLEMENTATION
// *****************************************************************************
    public void write(int val) throws IOException {
        randomFile.write(val);
        if (sync) {
            randomFile.getFD().sync();
        }
    }

    @Override
    public void write(byte[] val) throws IOException {
        randomFile.write(val);
        if (sync) {
            randomFile.getFD().sync();
        }
    }

    @Override
    public void write(byte[] val, int off, int len) throws IOException {
        randomFile.write(val, off, len);
        if (sync) {
            randomFile.getFD().sync();
        }
    }

    @Override
    public void flush() throws IOException {
        if (sync) {
            randomFile.getFD().sync();
        }
    }

    @Override
    public void close() throws IOException {
        randomFile.close();
    }

// *****************************************************************************
// INSTANCE METHODS - RANDOM ACCESS EXTENSIONS
// *****************************************************************************
    public long getFilePointer() throws IOException {
        return randomFile.getFilePointer();
    }

    public void setFilePointer(long pos) throws IOException {
        randomFile.seek(pos);
    }

    public long getFileSize() throws IOException {
        return randomFile.length();
    }

    public void setFileSize(long len) throws IOException {
        randomFile.setLength(len);
    }

    public FileDescriptor getFD() throws IOException {
        return randomFile.getFD();
    }
}
