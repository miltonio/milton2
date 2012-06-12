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
