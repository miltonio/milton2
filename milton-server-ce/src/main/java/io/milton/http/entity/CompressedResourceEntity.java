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
