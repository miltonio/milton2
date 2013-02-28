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

/* vim:set softtabstop=3 shiftwidth=3 tabstop=3 expandtab tw=72:
   $Id: BrokenMD4.java,v 1.7 2003/07/20 04:26:13 rsdio Exp $
  
   This version is derived from the version in GNU Crypto.

   BrokenMD4: rsync-compatible MD4.
   Copyright (C) 2002 The Free Software Foundation, Inc.
   Copyright (C) 2003  Casey Marshall <rsdio@metastatic.org>
  
   This file is a part of Jarsync.
  
   Jarsync is free software; you can redistribute it and/or modify it
   under the terms of the GNU General Public License as published by the
   Free Software Foundation; either version 2 of the License, or (at
   your option) any later version.
  
   Jarsync is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
  
   You should have received a copy of the GNU General Public License
   along with Jarsync; if not, write to the
  
      Free Software Foundation, Inc.,
      59 Temple Place, Suite 330,
      Boston, MA  02111-1307
      USA
  
   Linking Jarsync statically or dynamically with other modules is
   making a combined work based on Jarsync.  Thus, the terms and
   conditions of the GNU General Public License cover the whole
   combination. */

package io.milton.zsync;

/**
 * <p>An implementation of Ron Rivest's MD4 message digest algorithm.
 * MD4 was the precursor to the stronger MD5
 * algorithm, and while not considered cryptograpically secure itself,
 * MD4 is in use in various applications. It is slightly faster than
 * MD5.</p>
 *
 * <p>This implementation is provided for compatibility with the "MD4"
 * implementation in the C version of rsync -- which differs in that the
 * input is NOT padded when the input is divisible by 64, and that only
 * the lower 32 bits of the length is used in the padding (the real MD4
 * always pads the input and uses the entire 8 bytes in the padding).
 *
 * <p><strong>DO NOT USE THIS IMPLEMENTATION IN NEW
 * PROGRAMS.</strong></p>
 *
 * <p>References:</p>
 *
 * <ol>
 *    <li>The <a href="http://www.ietf.org/rfc/rfc1320.txt">MD4</a> Message-
 *    Digest Algorithm.<br>
 *    R. Rivest.</li>
 * </ol>
 *
 * @version $Revision: 1.7 $
 */
public final class BrokenMD4 extends MD4 implements Cloneable {

   // Constants and variables.
   // -----------------------------------------------------------------
 
 // Constructors.
   // -----------------------------------------------------------------

   /**
    * Trivial zero-argument constructor.
    */
   public BrokenMD4() {
      super();
   }

   /**
    * Private constructor for cloning.
    */
   private BrokenMD4(BrokenMD4 that) {
      this();

      this.a = that.a;
      this.b = that.b;
      this.c = that.c;
      this.d = that.d;
      this.count = that.count;
      System.arraycopy(that.buffer, 0, this.buffer, 0, BLOCK_LENGTH);
   }

   // java.lang.Cloneable interface implementation --------------------

   public Object clone() {
      return new BrokenMD4(this);
   }

   // SPI instance methods.
   // -----------------------------------------------------------------

   /**
    * Pack the four chaining variables into a byte array.
    */
   protected byte[] engineDigest() {
      if (count % BLOCK_LENGTH != 0) {
         byte[] tail = padBuffer();
         engineUpdate(tail, 0, tail.length);
      }
      byte[] digest = {
         (byte) a, (byte) (a >>> 8), (byte) (a >>> 16), (byte) (a >>> 24),
         (byte) b, (byte) (b >>> 8), (byte) (b >>> 16), (byte) (b >>> 24),
         (byte) c, (byte) (c >>> 8), (byte) (c >>> 16), (byte) (c >>> 24),
         (byte) d, (byte) (d >>> 8), (byte) (d >>> 16), (byte) (d >>> 24)
      };

      engineReset();

      return digest;
   }

   /**
    * Pad the buffer by appending the byte 0x80, then as many zero bytes
    * to fill the buffer 8 bytes shy of being a multiple of 64 bytes, then
    * append the length of the buffer, in bits, before padding.
    */
   protected byte[] padBuffer() {
      int n = (int) (count % BLOCK_LENGTH);
      int padding = (n < 56) ? (56 - n) : (120 - n);
      byte[] pad = new byte[padding + 8];

      pad[0] = (byte) 0x80;
      int bits = (int)(count << 3);
      pad[padding++] = (byte)  bits;
      pad[padding++] = (byte) (bits >>>  8);
      pad[padding++] = (byte) (bits >>> 16);
      pad[padding++] = (byte) (bits >>> 24);
      //pad[padding++] = (byte) (bits >>> 32);
      //pad[padding++] = (byte) (bits >>> 40);
      //pad[padding++] = (byte) (bits >>> 48);
      //pad[padding  ] = (byte) (bits >>> 56);

      return pad;
   }
} 
