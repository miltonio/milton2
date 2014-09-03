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
