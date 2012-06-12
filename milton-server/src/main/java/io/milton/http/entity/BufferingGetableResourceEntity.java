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
import io.milton.http.Response;
import io.milton.common.BufferingOutputStream;
import io.milton.common.ReadingException;
import io.milton.common.StreamUtils;
import io.milton.common.WritingException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class BufferingGetableResourceEntity extends GetableResourceEntity {

    private static final Logger log = LoggerFactory.getLogger(BufferingGetableResourceEntity.class);

    private Long contentLength;
    private int maxMemorySize;

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

        log.trace("sending buffered content...");
        InputStream in = tempOut.getInputStream();
        try {
            StreamUtils.readTo(in, outputStream);
        } catch (ReadingException ex) {
            throw new RuntimeException(ex);
        } catch (WritingException ex) {
            log.warn("exception writing, client probably closed connection", ex);
        } finally {
            IOUtils.closeQuietly(in); // make sure we close to delete temporary file
        }
    }
}
