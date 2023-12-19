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

import java.io.*;

/**
 * A positionable file output stream.
 * <p>
 * Threading Design : [x] Single Threaded  [ ] Threadsafe  [ ] Immutable  [ ] Isolated
 * <p>
 * From <a href="https://stackoverflow.com/questions/825732/how-can-i-implement-an-outputstream-that-i-can-rewind">
 *     https://stackoverflow.com/questions/825732/how-can-i-implement-an-outputstream-that-i-can-rewind</a>
 */
public class RandomFileOutputStream extends OutputStream {

    // *****************************************************************************
    // INSTANCE PROPERTIES
    // *****************************************************************************
    protected RandomAccessFile randomFile;                             // the random file to write to
    protected boolean sync;                                   // whether to synchronize every write

    /**
     * Initializes new {@link RandomFileOutputStream} with file name.
     * @param fileName File name.
     * @throws IOException in case of exception.
     */
    public RandomFileOutputStream(String fileName) throws IOException {
        this(fileName, false);
    }

    /**
     * Initializes new {@link RandomFileOutputStream} with file name.
     * @param fileName File name.
     * @param sync Whether to synchronize every write.
     * @throws IOException in case of exception.
     */
    public RandomFileOutputStream(String fileName, boolean sync) throws IOException {
        this(new File(fileName), sync);
    }

    /**
     * Initializes new {@link RandomFileOutputStream}
     * @param file File.
     * @throws IOException in case of exception.
     */
    public RandomFileOutputStream(File file) throws IOException {
        this(file, false);
    }

    /**
     * Initializes new {@link RandomFileOutputStream} with file name and sync possibility.
     * @param file File.
     * @param sync Whether to synchronize every write.
     * @throws IOException in case of exception.
     */
    public RandomFileOutputStream(File file, boolean sync) throws IOException {
        super();
        randomFile = new RandomAccessFile(file, "rw");
        this.sync = sync;
    }

    // *****************************************************************************
    // INSTANCE METHODS - OUTPUT STREAM IMPLEMENTATION
    // *****************************************************************************

    /**
     * Write int value into stream.
     * @param val   int value..
     * @throws IOException in case of write errors.
     */
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

    /**
     * Returns current offset when last read was done.
     * @return Current offset.
     * @throws IOException in case of IO exception.
     */
    public long getFilePointer() throws IOException {
        return randomFile.getFilePointer();
    }

    /**
     * Sets the position to the new offset.
     * @param pos new position.
     * @throws IOException in case of IO exception.
     */
    public void setFilePointer(long pos) throws IOException {
        randomFile.seek(pos);
    }

    /**
     * Returns size of underlying file.
     * @return file size.
     * @throws IOException in case of IO exception.
     */
    public long getFileSize() throws IOException {
        return randomFile.length();
    }

    /**
     * Sets new underlying file size.
     * @param length new length.
     * @throws IOException in case of IO exception.
     */
    public void setFileSize(long length) throws IOException {
        randomFile.setLength(length);
    }

    /**
     * Returns underlying file descriptor.
     * @return file descriptor.
     * @throws IOException in case if IO eexception.
     */
    public FileDescriptor getFD() throws IOException {
        return randomFile.getFD();
    }
}
