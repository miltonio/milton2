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
package io.milton.http.entity;

import io.milton.common.BufferingOutputStream;
import io.milton.http.Range;
import io.milton.http.Response;
import io.milton.http.http11.MultipleRangeWritingOutputStream;
import io.milton.resource.GetableResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;

public class PartialEntity implements Response.Entity {

	private static final Logger log = LoggerFactory.getLogger(PartialEntity.class);
	private final GetableResource resource;
	private final List<Range> ranges;
	private final Map<String, String> params;
	private final String contentType;
	private final String multipartBoundary;

	public PartialEntity(GetableResource resource, List<Range> ranges, Map<String, String> params, String contentType, String multipartBoundary) {
		this.resource = resource;
		this.ranges = ranges;
		this.params = params;
		this.contentType = contentType;
		this.multipartBoundary = multipartBoundary;
	}
		

	
	public GetableResource getResource() {
		return resource;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public String getContentType() {
		return contentType;
	}

	@Override
	public void write(Response response, OutputStream outputStream) throws Exception {

		Long contentLength = resource.getContentLength();
		if (contentLength == null) {
			log.warn("Couldnt calculate range end position because the resource is not reporting a content length, and no end position was requested by the client: " + resource.getName() + " - " + resource.getClass());
			contentLength = -1l;
		}
				

		try {

			BufferingOutputStream bufOut = new BufferingOutputStream(100000);
			MultipleRangeWritingOutputStream multiOut = new MultipleRangeWritingOutputStream(contentLength, bufOut, ranges, multipartBoundary, contentType);

			// This will only write content to the buffer, not output to client
			resource.sendContent(multiOut, null, params, contentType); // do not pass ranges, we need full content to extract ranges
			
			response.setContentLengthHeader(bufOut.getSize());

			// Now we can finally transmit content
			IOUtils.copy(bufOut.getInputStream(), outputStream);

		} catch (IOException ex) {
			log.warn("IOException writing response: " + ex.getMessage());
			IOUtils.closeQuietly(outputStream);
		}
	}
}
