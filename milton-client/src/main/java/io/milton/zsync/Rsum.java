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

/* Rsum.java

   Rsum: A simple, "rolling" checksum based on Adler32
   Copyright (C) 2011 Tomas Hlavnicka <hlavntom@fel.cvut.cz>

   This file is a part of Jazsync.

   Jazsync is free software; you can redistribute it and/or modify it
   under the terms of the GNU General Public License as published by the
   Free Software Foundation; either version 2 of the License, or (at
   your option) any later version.

   Jazsync is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Jazsync; if not, write to the

      Free Software Foundation, Inc.,
      59 Temple Place, Suite 330,
      Boston, MA  02111-1307
      USA
 */

package io.milton.zsync;

/**
 * Implementation of rolling checksum for zsync purposes
 * @author Tomáš Hlavnička
 */
public class Rsum implements RollingChecksum, Cloneable, java.io.Serializable {
    private short a;
    private short b;
    private int oldByte;
    private int blockLength;
    private byte[] buffer;

    /**
     * Constructor of rolling checksum
     */
    public Rsum(){
        a = b = 0;
        oldByte  = 0;
    }

    /**
     * Return the value of the currently computed checksum.
     *
     * @return The currently computed checksum.
     */
    @Override
    public int getValue() {
        return ((a & 0xffff) | (b << 16));
    }

    /**
     * Reset the checksum
     */
    @Override
    public void reset() {
        a = b = 0;
        oldByte = 0;
    }

    /**
     * Rolling checksum that takes single byte and compute checksum
     * of block from file in offset that equals offset of newByte 
     * minus length of block
     * 
     * @param newByte New byte that will actualize a checksum
     */
    @Override
    public void roll(byte newByte) {
        short oldUnsignedB=unsignedByte(buffer[oldByte]);
        a -= oldUnsignedB;
        b -= blockLength * oldUnsignedB;
        a += unsignedByte(newByte);
        b += a;
        buffer[oldByte]=newByte;
        oldByte++;
        if(oldByte==blockLength){
            oldByte=0;
        }
    }

    /**
     * Update the checksum with an entirely different block, and
     * potentially a different block length.
     *
     * @param buf The byte array that holds the new block.
     * @param offset From whence to begin reading.
     * @param length The length of the block to read.
     */
    @Override
    public void check(byte[] buf, int offset, int length) {
        reset();
        int index=offset;
        short unsignedB;
        for(int i=length;i>0;i--){
            unsignedB=unsignedByte(buf[index]);
            a+=unsignedB;
            b+=i*unsignedB;
            index++;
        }
    }

    /**
     * Update the checksum with an entirely different block, and
     * potentially a different block length. This method is only used to
     * initialize rolling checksum.
     *
     * @param buf The byte array that holds the new block.
     * @param offset From whence to begin reading.
     * @param length The length of the block to read.
     */
    @Override
    public void first(byte[] buf, int offset, int length) {
        reset();
        int index=offset;
        short unsignedB;
        for(int i=length;i>0;i--){
            unsignedB=unsignedByte(buf[index]);
            a+=unsignedB;
            b+=i*unsignedB;
            index++;
        }
        blockLength=length;
        buffer = new byte[blockLength];
        System.arraycopy(buf, 0, buffer, 0, length);
    }

    /**
     * Returns "unsigned" value of byte
     *
     * @param b Byte to convert
     * @return Unsigned value of byte <code>b</code>
     */
    private short unsignedByte(byte b){
        if(b<0) {
            return (short)(b+256);
        }
        return b;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new Error();
        }
    }

    @Override
    public boolean equals(Object o) {
        return ((Rsum) o).a == a && ((Rsum) o).b == b;
    }
}
