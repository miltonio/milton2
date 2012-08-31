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
