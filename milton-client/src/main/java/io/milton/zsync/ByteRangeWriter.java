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

import io.milton.common.BufferingOutputStream;
import io.milton.http.Range;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

/**
 * An object used to write the dataStream portion of an Upload.<p/>
 *
 * This data portion consists of a sequence of chunks of binary data, each preceded by a Range: start-finish
 * key value String indicating the target location of the chunk. The chunk of data contains exactly
 * finish - start bytes.
 *
 * @author Nick
 */
/**
 *
 * @author HP
 */
public class ByteRangeWriter {
	private BufferingOutputStream dataOut;
	private byte[] copyBuffer;
	private boolean first;

	public ByteRangeWriter(int buffersize) {
		this.dataOut = new BufferingOutputStream(buffersize);
		this.copyBuffer = new byte[2048];
		this.first = true;
	}

	public void add(Range range, RandomAccessFile randAccess) throws UnsupportedEncodingException, IOException {
		/*
		 * Write the Range Key:Value pair to dataOut
		 */
		String rangeKV = Upload.paramString(Upload.RANGE, range.getRange());
		if (!first) {
			rangeKV = Upload.LF + rangeKV;
		}
		dataOut.write(rangeKV.getBytes(Upload.CHARSET));
		first = false;
		/*
		 * Write the actual bytes to dataOut
		 */
		long bytesLeft = range.getFinish() - range.getStart();
		int bytesRead = 0;
		int bytesToRead = (int) Math.min(bytesLeft, copyBuffer.length);
		randAccess.seek(range.getStart());
		while (bytesLeft > 0) {
			bytesRead = randAccess.read(copyBuffer, 0, bytesToRead);
			dataOut.write(copyBuffer, 0, bytesRead);
			bytesLeft -= bytesRead;
			bytesToRead = (int) Math.min(bytesLeft, copyBuffer.length);
		}
	}

	public InputStream getInputStream() throws IOException {
		dataOut.close();
		return dataOut.getInputStream();
	}
	
}
