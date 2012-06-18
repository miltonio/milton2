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

package io.milton.zsync;

import io.milton.http.Range;
import java.io.InputStream;


/**
 * A simple container for a Range and a reference to an InputStream. 
 * 
 * @author Administrator
 */
public class ByteRange {

	private Range range;
	private InputStream dataQueue;
	
	/**
	 * Constructs a ByteRange with the specified Range and InputStream. The dataQueue field
	 * will simply reference the specified InputStream rather than copying from it.
	 * 
	 * @param range 
	 * @param queue 
	 */
	public ByteRange( Range range, InputStream queue ) {
		this.range = range;
		this.dataQueue = queue;
	}

	public Range getRange() {
		return range;
	}

	public InputStream getDataQueue() {
		return dataQueue;
	}
}
