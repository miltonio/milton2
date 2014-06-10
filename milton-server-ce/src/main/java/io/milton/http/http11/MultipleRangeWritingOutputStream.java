/*
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.milton.http.http11;

import io.milton.common.RangeUtils;
import io.milton.common.Utils;
import io.milton.http.Range;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Write the full content of yuor resource to this outputstream, and it will
 * write the requested ranges, including multipart boundaries, to the wrapped
 * outputstream
 *
 * @author brad
 */
public class MultipleRangeWritingOutputStream extends OutputStream {

	private final long totalResourceLength;
	private final OutputStream out;
	private final List<Range> ranges;
	private final String boundary;
	private final String contentType;

	private int currentByte;
	private Range currentRange;
	private int currentRangeNum;

	/**
	 *
	 * @param totalResourceLength
	 * @param out
	 * @param ranges
	 * @param boundary
	 * @param contentType
	 */
	public MultipleRangeWritingOutputStream(long totalResourceLength, OutputStream out, List<Range> ranges, String boundary, String contentType) {
		this.out = out;
		this.ranges = ranges;
		this.boundary = boundary;
		this.contentType = contentType;
		this.totalResourceLength = totalResourceLength;
	}

	@Override
	public void write(int b) throws IOException {
		Range range = getCurrentRange();
		if (range != null) {
			out.write(b);
		}
		currentByte++;
	}

	private boolean isValid(Range r) {
		if (r != null) {
			if (r.getStart() == null || r.getStart() <= currentByte) {
				if (r.getFinish() == null || r.getFinish() >= currentByte) {
					return true;
				}

			}
		}
		return false;
	}

	private Range getCurrentRange() throws IOException {
		if (currentRange != null) {
			if (isValid(currentRange)) {
				return currentRange;
			}
		}
		currentRange = null;

		while (currentRangeNum < ranges.size()) {
			Range r = ranges.get(currentRangeNum++);
			if (isValid(r)) {
				writeRangeHeader(r);
				return r;
			}
		}
		return null;
	}

	private void writeRangeHeader(Range r) throws IOException {
//--3d6b6a416f9b5
//Content-Type: text/html
//Content-Range: bytes 100-200/1270
		out.write(("--" + boundary + "\n").getBytes(Utils.UTF8));
		if (contentType != null) {
			out.write(("Content-Type: " + contentType + "\n").getBytes(Utils.UTF8));
		}
		out.write(("Content-Range: " + RangeUtils.toRangeString(currentByte, r.getFinish(), totalResourceLength) + "\n").getBytes(Utils.UTF8));
	}

}
