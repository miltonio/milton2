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

import io.milton.http.CompressedResource;
import io.milton.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class CompressedResourceEntity implements Response.Entity {

    private static final Logger log = LoggerFactory.getLogger(CompressedResourceEntity.class);

    private CompressedResource resource;
    private Map<String, String> params;
    private String contentType;
    private String contentEncoding;

    public CompressedResourceEntity(CompressedResource resource, Map<String, String> params, String contentType, String contentEncoding) {
        this.resource = resource;
        this.params = params;
        this.contentType = contentType;
        this.contentEncoding = contentEncoding;
    }

    public CompressedResource getResource() {
        return resource;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getContentType() {
        return contentType;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    @Override
    public void write(Response response, OutputStream outputStream) throws Exception {
        try {
            resource.sendCompressedContent(contentEncoding, outputStream, null, params, contentType);
        } catch (IOException ex) {
            log.warn("IOException sending compressed content", ex);
        }
    }

}
