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
