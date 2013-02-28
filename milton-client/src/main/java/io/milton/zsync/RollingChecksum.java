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
   $Id: RollingChecksum.java,v 1.8 2003/05/17 09:48:58 rsdio Exp $

   RollingChecksum: interface to a "rolling" checksum.
   Copyright (C) 2003  Casey Marshall <rsdio@metastatic.org>
   Copyright (C) 2011  Tomas Hlavnicka <hlavntom@fel.cvut.cz>

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
 * A general interface for 32-bit checksums that have the "rolling"
 * property.
 *
 * @author Casey Marshall
 * @version $Revision: 1.8 $
 */
public interface RollingChecksum extends Cloneable, java.io.Serializable {

   // Methods.
   // -----------------------------------------------------------------------

   /**
    * Returns the currently-computed 32-bit checksum.
    *
    * @return The checksum.
    */
   int getValue();

   /**
    * Resets the internal state of the checksum, so it may be re-used
    * later.
    */
   void reset();

   /**
    * Update the checksum with a single byte. This is where the
    * "rolling" method is used.
    *
    * @param b The next byte.
    */
   void roll(byte b);

   /**
    * Replaces the current internal state with entirely new data.
    *
    * @param buf    The bytes to checksum.
    * @param offset The offset into <code>buf</code> to start reading.
    * @param length The number of bytes to update.
    */
   void check(byte[] buf, int offset, int length);

   /**
    * Replaces the current internal state with entirely new data.
    * This method is only used to initialize rolling checksum.
    *
    * @param buf    The bytes to checksum.
    * @param offset The offset into <code>buf</code> to start reading.
    * @param length The number of bytes to update.
    */
   void first(byte[] buf, int offset, int length);

   /**
    * Copies this checksum instance into a new instance. This method
    * should be optional, and only implemented if the class implements
    * the {@link java.lang.Cloneable} interface.
    *
    * @return A clone of this instance.
    */
   Object clone();

   /**
    * Tests if a particular checksum is equal to this checksum. This
    * means that the other object is an instance of this class, and its
    * internal state equals this checksum's internal state.
    *
    * @param o The object to test.
    * @return <code>true</code> if this checksum equals the other
    *         checksum.
    */
   boolean equals(Object o);

}
