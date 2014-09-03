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

/*
SHA1: SHA1 message digest algorithm.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class for SHA-1 sum
 * @author Tomáš Hlavnička
 */
public class SHA1 {

	private File file;
	private FileInputStream fis;
	private MessageDigest sha1;

	/**
	 * Constructor SHA1
	 * @param filename Name and path to a file
	 */
	public SHA1(String filename) {
		this.file = new File(filename);
	}

	/**
	 * Constructor SHA1
	 * @param file File for calculation
	 */
	public SHA1(File file) {
		this.file = file;
	}

	/**
	 * Calculates SHA1
	 * @return String with hash value
	 */
	public String SHA1sum() {
		try {
			sha1 = MessageDigest.getInstance("SHA1");
			fis = new FileInputStream(file);
			byte[] dataBytes = new byte[1024];

			int read = 0;

			while ((read = fis.read(dataBytes)) != -1) {
				sha1.update(dataBytes, 0, read);
			}

			return toString(sha1);

		} catch (IOException ex) {
			throw new RuntimeException("Can't read file to count SHA-1 hash, check your permissions", ex);
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException("Problem with SHA-1 hash", ex);
		}
	}

	public static String toString(MessageDigest sha1) {
		byte[] mdbytes = sha1.digest();

		//prevede byte do hex formatu
		StringBuilder sb = new StringBuilder("");
		for (int i = 0; i < mdbytes.length; i++) {
			sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}
}
