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

package io.milton.httpclient.zsyncclient;

import io.milton.http.Range;
import java.util.List;

/**
 * Used to load selected range data to satisfy the zsync process
 *
 * @author brad
 */
public interface RangeLoader {

	/**
	 * Fetch a set of ranges, usually over HTTP
	 * 
	 * @param rangeList
	 * @return
	 * @throws Exception 
	 */
	public byte[] get(List<Range> rangeList) throws Exception;
	
}
