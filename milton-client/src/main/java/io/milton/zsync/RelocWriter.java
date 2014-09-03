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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.milton.zsync;

import io.milton.common.BufferingOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * An object used to create the relocStream portion of an Upload. <p/>
 *
 * The relocStream portion consists of a comma separated sequence of RelocateRanges,
 * terminated by the LF character.
 *
 * @author Nick
 */
/**
 *
 * @author HP
 */
public class RelocWriter {
	private BufferingOutputStream relocOut;
	private boolean first;

	public RelocWriter(int buffersize) {
		this.relocOut = new BufferingOutputStream(buffersize);
		this.first = true;
	}

	public void add(RelocateRange reloc) throws UnsupportedEncodingException, IOException {
		String relocString = reloc.getRelocation();
		if (!first) {
			relocString = ", " + relocString;
		}
		first = false;
		relocOut.write(relocString.getBytes(Upload.CHARSET));
	}

	public InputStream getInputStream() throws IOException {
		relocOut.write(Character.toString(Upload.LF).getBytes(Upload.CHARSET)[0]);
		relocOut.close();
		return relocOut.getInputStream();
	}
	
}
