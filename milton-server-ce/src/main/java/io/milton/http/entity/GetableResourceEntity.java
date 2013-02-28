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

import io.milton.resource.GetableResource;
import io.milton.http.Range;
import io.milton.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class GetableResourceEntity implements Response.Entity {

	private static final Logger log = LoggerFactory.getLogger(GetableResourceEntity.class);
	private GetableResource resource;
	private Range range;
	private Map<String, String> params;
	private String contentType;

	public GetableResourceEntity(GetableResource resource, Map<String, String> params, String contentType) {
		this(resource, null, params, contentType);
	}

	public GetableResourceEntity(GetableResource resource, Range range, Map<String, String> params, String contentType) {
		this.resource = resource;
		this.range = range;
		this.params = params;
		this.contentType = contentType;
	}

	public GetableResource getResource() {
		return resource;
	}

	public Range getRange() {
		return range;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public String getContentType() {
		return contentType;
	}

	@Override
	public void write(Response response, OutputStream outputStream) throws Exception {
		long l = System.currentTimeMillis();
		log.trace("sendContent");
		try {
			resource.sendContent(outputStream, range, params, contentType);
			// TODO: The original code didn't flush for partial responses, not sure why...

			// BM: not sure, but i think flushing might be interfering with some connection management stuff
			//outputStream.flush();
			if (log.isTraceEnabled()) {
				l = System.currentTimeMillis() - l;
				log.trace("sendContent finished in " + l + "ms");
			}
		} catch (IOException ex) {
			log.warn("IOException writing to output, probably client terminated connection", ex);
			try {
				outputStream.close(); // attempt to close the stream
			} catch (Exception e) {
				// ignore
			}
			//throw new RuntimeException("IOException", ex); // throw so the container can catch and clean up the connection
		}
	}
}
