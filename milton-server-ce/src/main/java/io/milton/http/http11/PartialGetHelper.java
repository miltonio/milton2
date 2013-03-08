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

package io.milton.http.http11;

import io.milton.resource.GetableResource;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.Response;
import io.milton.http.entity.PartialEntity;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.common.StreamUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class PartialGetHelper {

	private static final Logger log = LoggerFactory.getLogger(PartialGetHelper.class);
	private final Http11ResponseHandler responseHandler;
	private int maxMemorySize = 100000;

	public PartialGetHelper(Http11ResponseHandler responseHandler) {
		this.responseHandler = responseHandler;
	}

	public List<Range> getRanges(String rangeHeader) {
		if (rangeHeader == null || rangeHeader.length() == 0) {
			log.trace("getRanges: no range header");
			return null;
		}
		if (rangeHeader.startsWith("bytes=")) {
			rangeHeader = rangeHeader.substring(6);
			String[] arr = rangeHeader.split(",");
			List<Range> list = new ArrayList<Range>();
			for (String s : arr) {
				Range r = Range.parse(s);
				list.add(r);
			}
			if (log.isTraceEnabled()) {
				log.trace("getRanges: header: " + rangeHeader + " parsed ranges: " + list.size());
			}
			return list;

		} else {
			return null;
		}
	}

	public void sendPartialContent(GetableResource resource, Request request, Response response, List<Range> ranges, Map<String, String> params) throws NotAuthorizedException, BadRequestException, IOException, NotFoundException {
		log.trace("sendPartialContent");
		if (ranges.size() == 1) {
			log.trace("partial get, single range");
			Range r = ranges.get(0);
			responseHandler.respondPartialContent(resource, response, request, params, r);
		} else {
			log.trace("partial get, multiple ranges");
			File temp = File.createTempFile("milton_partial_get", null);
			FileOutputStream fout = null;
			try {
				fout = new FileOutputStream(temp);
				BufferedOutputStream bufOut = new BufferedOutputStream(fout);
				resource.sendContent(bufOut, null, params, request.getContentTypeHeader());
				bufOut.flush();
				fout.flush();
			} finally {
				StreamUtils.close(fout);
			}
            response.setEntity(
               new PartialEntity(ranges, temp)
            );
		}
	}

	public int getMaxMemorySize() {
		return maxMemorySize;
	}

	public void setMaxMemorySize(int maxMemorySize) {
		this.maxMemorySize = maxMemorySize;
	}

}
