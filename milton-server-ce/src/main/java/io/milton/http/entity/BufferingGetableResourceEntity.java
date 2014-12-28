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
import io.milton.common.ReadingException;
import io.milton.common.WritingException;
import io.milton.http.Response;
import io.milton.resource.GetableResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BufferingGetableResourceEntity extends GetableResourceEntity {

    private static final Logger log = LoggerFactory.getLogger(BufferingGetableResourceEntity.class);

	private final Long contentLength;
	private final int maxMemorySize;

    public BufferingGetableResourceEntity(GetableResource resource,
                                          Map<String, String> params,
                                          String contentType,
                                          Long contentLength,
                                          int maxMemorySize) {
        super(resource, null, params, contentType);
        this.contentLength = contentLength;
        this.maxMemorySize = maxMemorySize;
    }

    public Long getContentLength() {
        return contentLength;
    }

    public int getMaxMemorySize() {
        return maxMemorySize;
    }

    @Override
    public void write(Response response, OutputStream outputStream) throws Exception {
        log.trace("buffering content...");
        BufferingOutputStream tempOut = new BufferingOutputStream(maxMemorySize);
        try {
            getResource().sendContent(tempOut, getRange(), getParams(), getContentType());
            tempOut.close();
        } catch (IOException ex) {
            tempOut.deleteTempFileIfExists();
            throw new RuntimeException("Exception generating buffered content", ex);
        }
        Long bufContentLength = tempOut.getSize();
        if (contentLength != null) {
            if (!contentLength.equals(bufContentLength)) {
                throw new RuntimeException("Content Length specified by resource: " + contentLength + " is not equal to the size of content when generated: " + bufContentLength + " This error can be suppressed by setting the buffering property to whenNeeded or never");
            }
        }
        response.setContentLengthHeader(bufContentLength);

		if( log.isTraceEnabled()) {
			log.trace("sending buffered content... " + tempOut.getSize() + " bytes contentLength=" + contentLength);
		}
        InputStream in = tempOut.getInputStream();
        try {
			//StreamUtils.readTo(in, outputStream);
			IOUtils.copy(in, outputStream);
        } catch (ReadingException ex) {
            throw new RuntimeException(ex);
        } catch (WritingException ex) {
            log.warn("exception writing, client probably closed connection", ex);
        } finally {
            IOUtils.closeQuietly(in); // make sure we close to delete temporary file
        }
    }
}
