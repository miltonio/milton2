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
   $Id: Configuration.java,v 1.9 2003/05/17 09:48:58 rsdio Exp $

   Configuration -- Wrapper around configuration data.
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

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A Configuration is a mere collection of objects and values that
 * compose a particular configuration for the algorithm, for example the
 * message digest that computes the strong checksum.
 *
 * <p>Usage of a Configuration involves setting the member fields of
 * this object to thier appropriate values; thus, it is up to the
 * programmer to specify the {@link #strongSum}, {@link #weakSum},
 * {@link #blockLength} and {@link #strongSumLength} to be used. The
 * other fields are optional.</p>
 *
 * @author Casey Marshall
 * @version $Revision: 1.9 $
 */
public class Configuration implements Cloneable, java.io.Serializable {

   // Constants and variables.
   // ------------------------------------------------------------------------

   /**
    * The default block size.
    */
   public static final int BLOCK_LENGTH = 1024;

   /**
    * The default chunk size.
    */
   public static final int CHUNK_SIZE = 32768;

   /**
    * The message digest that computes the stronger checksum.
    */
   public transient MessageDigest strongSum;

   /**
    * The rolling checksum.
    */
   public transient RollingChecksum weakSum;

   /**
    * The length of blocks to checksum.
    */
   public int blockLength;

   /**
    * The effective length of the strong sum.
    */
   public int strongSumLength;

   /**
    * Whether or not to do run-length encoding when making Deltas.
    */
   public boolean doRunLength;

   /**
    * The seed for the checksum, to perturb the strong checksum and help
    * avoid collisions in plain rsync (or in similar applicaitons).
    */
   public byte[] checksumSeed;

   /**
    * The maximum size of byte arrays to create, when they are needed.
    * This vale defaults to 32 kilobytes.
    */
   public int chunkSize;

   // Constructors.
   // ------------------------------------------------------------------------

   public Configuration() {
      blockLength = BLOCK_LENGTH;
      chunkSize = CHUNK_SIZE;
   }

   /**
    * Private copying constructor.
    */
   private Configuration(Configuration that)
   {
      try {
         this.strongSum = (MessageDigest) (that.strongSum != null
            ? that.strongSum.clone()
            : null);
      } catch (CloneNotSupportedException cnse) {
         try {
            this.strongSum = MessageDigest.getInstance(
               that.strongSum.getAlgorithm());
         } catch (NoSuchAlgorithmException nsae) {
            // Fucked up situation. We die now.
            throw new Error(nsae);
         }
      }
      this.weakSum = (RollingChecksum) (that.weakSum != null
         ? that.weakSum.clone()
         : null);
      this.blockLength = that.blockLength;
      this.doRunLength = that.doRunLength;
      this.strongSumLength = that.strongSumLength;
      this.checksumSeed = (byte[]) (that.checksumSeed != null
         ? that.checksumSeed.clone()
         : null);
      this.chunkSize = that.chunkSize;
   }

   public int getBlock(){
       return blockLength;
   }

   // Instance methods.
   // -----------------------------------------------------------------------

    @Override
   public Object clone() {
      return new Configuration(this);
   }

   // Serialization methods.
   // -----------------------------------------------------------------------

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.defaultWriteObject();
      out.writeUTF(strongSum != null ? strongSum.getAlgorithm() : "NONE");
   }

   private void readObject(ObjectInputStream in)
      throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      String s = in.readUTF();
      if (!s.equals("NONE")) {
         try {
            strongSum = MessageDigest.getInstance(s);
         } catch (NoSuchAlgorithmException nsae) {
            throw new java.io.InvalidObjectException(nsae.getMessage());
         }
      }
   }
}
