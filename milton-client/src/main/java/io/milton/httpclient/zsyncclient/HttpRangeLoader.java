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
import io.milton.httpclient.File;
import io.milton.httpclient.HttpException;
import io.milton.httpclient.ProgressListener;
import io.milton.httpclient.Utils.CancelledException;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author HP
 */
public class HttpRangeLoader implements RangeLoader {

	private static final Logger log = LoggerFactory.getLogger(HttpRangeLoader.class);
	private final File file;
	private final ProgressListener listener;
	private long numBytes;

	public HttpRangeLoader(File file, final ProgressListener listener) {
		this.file = file;
		this.listener = listener;
	}

	@Override
	public byte[] get(List<Range> rangeList) {
		log.info("get: rangelist: " + rangeList.size());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			file.download(out, listener, rangeList);
		} catch (HttpException ex) {
			
		} catch (CancelledException ex) {
			throw new RuntimeException("Cancelled, which is odd because no progress listener was provided");
		}

		byte[] bytes = out.toByteArray();
		int expectedLength = calcExpectedLength(rangeList);
//		if( expectedLength != bytes.length) {
//			log.warn("Got an unexpected data size!!");
//		}
		numBytes += bytes.length;
		return bytes;
	}

	public static int calcExpectedLength(List<Range> rangeList) {
		int l = 0;
		for (Range r : rangeList) {
			l += (r.getFinish() - r.getStart());
		}
		return l;
	}

	public long getBytesDownloaded() {
		return numBytes;
	}
}
