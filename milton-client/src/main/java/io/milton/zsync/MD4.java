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
   $Id: MD4.java,v 1.9 2003/03/30 15:18:46 rsdio Exp $
  
   This version is derived from the version in GNU Crypto.
  
   MD4: The MD4 message digest algorithm.
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
   combination.  */

package io.milton.zsync;

import java.security.DigestException;
import java.security.MessageDigestSpi;

/**
 * <p>An implementation of Ron Rivest's MD4 message digest algorithm.
 * MD4 was the precursor to the stronger MD5
 * algorithm, and while not considered cryptograpically secure itself,
 * MD4 is in use in various applications. It is slightly faster than
 * MD5.</p>
 *
 * <p>This implementation is derived from the version of MD4 in <a
 * href="http://www.gnu.org/software/gnu-crypto/">GNU Crypto.</p>
 *
 * <p>References:</p>
 *
 * <ol>
 *    <li>The <a href="http://www.ietf.org/rfc/rfc1320.txt">MD4</a> Message-
 *    Digest Algorithm.<br>
 *    R. Rivest.</li>
 * </ol>
 *
 * @version $Revision: 1.9 $
 */
public class MD4 extends MessageDigestSpi implements Cloneable {

   // Constants and variables.
   // -----------------------------------------------------------------
 
   /**
    * An MD4 message digest is always 128-bits long, or 16 bytes.
    */
   public static final int DIGEST_LENGTH = 16;

   /**
    * The MD4 algorithm operates on 512-bit blocks, or 64 bytes.
    */
   public static final int BLOCK_LENGTH = 64;

   protected static final int A = 0x67452301;
   protected static final int B = 0xefcdab89;
   protected static final int C = 0x98badcfe;
   protected static final int D = 0x10325476;

   /* The four chaining variables. */
   protected int a, b, c, d;

   protected long count;

   protected final byte[] buffer;

   /** Word buffer for transforming. */
   private final int[] X = new int[16];

 // Constructors.
   // -----------------------------------------------------------------

   /**
    * Trivial zero-argument constructor.
    */
   public MD4() {
      buffer = new byte[BLOCK_LENGTH];
      engineReset();
   }

   /**
    * Private constructor for cloning.
    */
   private MD4(MD4 that) {
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
      return new MD4(this);
   }

   // SPI instance methods.
   // -----------------------------------------------------------------

   protected int engineGetDigestLength() {
      return DIGEST_LENGTH;
   }

   public void engineUpdate(byte b) {
      // compute number of bytes still unhashed; ie. present in buffer
      int i = (int)(count % BLOCK_LENGTH);
      count++;
      buffer[i] = b;
      if (i == (BLOCK_LENGTH - 1)) {
         transform(buffer, 0);
      }
   }

   protected void engineUpdate(byte[] b, int offset, int len) {
      int n = (int)(count % BLOCK_LENGTH);
      count += len;
      int partLen = BLOCK_LENGTH - n;
      int i = 0;

      if (len >= partLen) {
         System.arraycopy(b, offset, buffer, n, partLen);
         transform(buffer, 0);
         for (i = partLen; i + BLOCK_LENGTH - 1 < len; i+= BLOCK_LENGTH) {
            transform(b, offset + i);
         }
         n = 0;
      }

      if (i < len) {
         System.arraycopy(b, offset + i, buffer, n, len - i);
      }
   }

   /**
    * Pack the four chaining variables into a byte array.
    */
   protected byte[] engineDigest() {
      byte[] tail = padBuffer();
      engineUpdate(tail, 0, tail.length);
      byte[] digest = {
         (byte) a, (byte) (a >>> 8), (byte) (a >>> 16), (byte) (a >>> 24),
         (byte) b, (byte) (b >>> 8), (byte) (b >>> 16), (byte) (b >>> 24),
         (byte) c, (byte) (c >>> 8), (byte) (c >>> 16), (byte) (c >>> 24),
         (byte) d, (byte) (d >>> 8), (byte) (d >>> 16), (byte) (d >>> 24)
      };

      engineReset();

      return digest;
   }

   protected
   int engineDigest(byte[] out, int off, int len) throws DigestException {
      if (off < 0 || off + len >= out.length) {
         throw new DigestException();
      }
      System.arraycopy(engineDigest(), 0, out, off,
         Math.min(len, DIGEST_LENGTH));
      return Math.min(len, DIGEST_LENGTH);
   }

   /** Reset the four chaining variables. */
   protected void engineReset() {
      a = A; b = B;
      c = C; d = D;
      count = 0;
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
      long bits = count << 3;
      pad[padding++] = (byte)  bits;
      pad[padding++] = (byte) (bits >>>  8);
      pad[padding++] = (byte) (bits >>> 16);
      pad[padding++] = (byte) (bits >>> 24);
      pad[padding++] = (byte) (bits >>> 32);
      pad[padding++] = (byte) (bits >>> 40);
      pad[padding++] = (byte) (bits >>> 48);
      pad[padding  ] = (byte) (bits >>> 56);

      return pad;
   }

   /** Transform a 64-byte block. */
   protected void transform(byte[] in, int offset) {
      int aa, bb, cc, dd;

      for (int i = 0, n = 0; i < 16; i++) {
         X[i] = (in[offset++] & 0xff)       |
                (in[offset++] & 0xff) <<  8 |
                (in[offset++] & 0xff) << 16 |
                (in[offset++] & 0xff) << 24;
      }

      aa = a;  bb = b;  cc = c;  dd = d;

      // Round 1
      a += ((b & c) | ((~b) & d)) + X[ 0];
      a = a <<  3 | a >>> (32 -  3);
      d += ((a & b) | ((~a) & c)) + X[ 1];
      d = d <<  7 | d >>> (32 -  7);
      c += ((d & a) | ((~d) & b)) + X[ 2];
      c = c << 11 | c >>> (32 - 11);
      b += ((c & d) | ((~c) & a)) + X[ 3];
      b = b << 19 | b >>> (32 - 19);
      a += ((b & c) | ((~b) & d)) + X[ 4];
      a = a <<  3 | a >>> (32 -  3);
      d += ((a & b) | ((~a) & c)) + X[ 5];
      d = d <<  7 | d >>> (32 -  7);
      c += ((d & a) | ((~d) & b)) + X[ 6];
      c = c << 11 | c >>> (32 - 11);
      b += ((c & d) | ((~c) & a)) + X[ 7];
      b = b << 19 | b >>> (32 - 19);
      a += ((b & c) | ((~b) & d)) + X[ 8];
      a = a <<  3 | a >>> (32 -  3);
      d += ((a & b) | ((~a) & c)) + X[ 9];
      d = d <<  7 | d >>> (32 -  7);
      c += ((d & a) | ((~d) & b)) + X[10];
      c = c << 11 | c >>> (32 - 11);
      b += ((c & d) | ((~c) & a)) + X[11];
      b = b << 19 | b >>> (32 - 19);
      a += ((b & c) | ((~b) & d)) + X[12];
      a = a <<  3 | a >>> (32 -  3);
      d += ((a & b) | ((~a) & c)) + X[13];
      d = d <<  7 | d >>> (32 -  7);
      c += ((d & a) | ((~d) & b)) + X[14];
      c = c << 11 | c >>> (32 - 11);
      b += ((c & d) | ((~c) & a)) + X[15];
      b = b << 19 | b >>> (32 - 19);

      // Round 2.
      a += ((b & (c | d)) | (c & d)) + X[ 0] + 0x5a827999;
      a = a <<  3 | a >>> (32 -  3);
      d += ((a & (b | c)) | (b & c)) + X[ 4] + 0x5a827999;
      d = d <<  5 | d >>> (32 -  5);
      c += ((d & (a | b)) | (a & b)) + X[ 8] + 0x5a827999;
      c = c <<  9 | c >>> (32 -  9);
      b += ((c & (d | a)) | (d & a)) + X[12] + 0x5a827999;
      b = b << 13 | b >>> (32 - 13);
      a += ((b & (c | d)) | (c & d)) + X[ 1] + 0x5a827999;
      a = a <<  3 | a >>> (32 -  3);
      d += ((a & (b | c)) | (b & c)) + X[ 5] + 0x5a827999;
      d = d <<  5 | d >>> (32 -  5);
      c += ((d & (a | b)) | (a & b)) + X[ 9] + 0x5a827999;
      c = c <<  9 | c >>> (32 -  9);
      b += ((c & (d | a)) | (d & a)) + X[13] + 0x5a827999;
      b = b << 13 | b >>> (32 - 13);
      a += ((b & (c | d)) | (c & d)) + X[ 2] + 0x5a827999;
      a = a <<  3 | a >>> (32 -  3);
      d += ((a & (b | c)) | (b & c)) + X[ 6] + 0x5a827999;
      d = d <<  5 | d >>> (32 -  5);
      c += ((d & (a | b)) | (a & b)) + X[10] + 0x5a827999;
      c = c <<  9 | c >>> (32 -  9);
      b += ((c & (d | a)) | (d & a)) + X[14] + 0x5a827999;
      b = b << 13 | b >>> (32 - 13);
      a += ((b & (c | d)) | (c & d)) + X[ 3] + 0x5a827999;
      a = a <<  3 | a >>> (32 -  3);
      d += ((a & (b | c)) | (b & c)) + X[ 7] + 0x5a827999;
      d = d <<  5 | d >>> (32 -  5);
      c += ((d & (a | b)) | (a & b)) + X[11] + 0x5a827999;
      c = c <<  9 | c >>> (32 -  9);
      b += ((c & (d | a)) | (d & a)) + X[15] + 0x5a827999;
      b = b << 13 | b >>> (32 - 13);

      // Round 3.
      a += (b ^ c ^ d) + X[ 0] + 0x6ed9eba1;
      a = a <<  3 | a >>> (32 -  3);
      d += (a ^ b ^ c) + X[ 8] + 0x6ed9eba1;
      d = d <<  9 | d >>> (32 -  9);
      c += (d ^ a ^ b) + X[ 4] + 0x6ed9eba1;
      c = c << 11 | c >>> (32 - 11);
      b += (c ^ d ^ a) + X[12] + 0x6ed9eba1;
      b = b << 15 | b >>> (32 - 15);
      a += (b ^ c ^ d) + X[ 2] + 0x6ed9eba1;
      a = a <<  3 | a >>> (32 -  3);
      d += (a ^ b ^ c) + X[10] + 0x6ed9eba1;
      d = d <<  9 | d >>> (32 -  9);
      c += (d ^ a ^ b) + X[ 6] + 0x6ed9eba1;
      c = c << 11 | c >>> (32 - 11);
      b += (c ^ d ^ a) + X[14] + 0x6ed9eba1;
      b = b << 15 | b >>> (32 - 15);
      a += (b ^ c ^ d) + X[ 1] + 0x6ed9eba1;
      a = a <<  3 | a >>> (32 -  3);
      d += (a ^ b ^ c) + X[ 9] + 0x6ed9eba1;
      d = d <<  9 | d >>> (32 -  9);
      c += (d ^ a ^ b) + X[ 5] + 0x6ed9eba1;
      c = c << 11 | c >>> (32 - 11);
      b += (c ^ d ^ a) + X[13] + 0x6ed9eba1;
      b = b << 15 | b >>> (32 - 15);
      a += (b ^ c ^ d) + X[ 3] + 0x6ed9eba1;
      a = a <<  3 | a >>> (32 -  3);
      d += (a ^ b ^ c) + X[11] + 0x6ed9eba1;
      d = d <<  9 | d >>> (32 -  9);
      c += (d ^ a ^ b) + X[ 7] + 0x6ed9eba1;
      c = c << 11 | c >>> (32 - 11);
      b += (c ^ d ^ a) + X[15] + 0x6ed9eba1;
      b = b << 15 | b >>> (32 - 15);

      a += aa; b += bb; c += cc; d += dd;
   }

} 
