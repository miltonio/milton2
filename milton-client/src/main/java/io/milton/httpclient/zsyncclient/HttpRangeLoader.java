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
