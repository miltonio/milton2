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
package com.mycompany;

import io.milton.http.Range;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.ReplaceableResource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * Holds binary files like PDFs, jpeg, etc
 *
 * Demonstrates implementing CustomPropertyResource
 *
 * @author brad
 */
public class TBinaryResource extends TResource implements ReplaceableResource{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TBinaryResource.class);
    byte[] bytes;
    String contentType;

    public TBinaryResource(TFolderResource parent, String name, byte[] bytes, String contentType) {
        super(parent, name);
        this.bytes = bytes;
    }

    @Override
    protected Object clone(TFolderResource newParent, String destName) {
        return new TBinaryResource(newParent, destName, bytes, contentType);
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
        System.out.println("writing binary resource:");
        out.write(bytes);
        System.out.println("wrote bytes: " + bytes.length);
    }

    @Override
    public Long getContentLength() {
        return (long) bytes.length;
    }

    @Override
    public String getContentType(String accept) {
        return contentType;
    }

    @Override
    public void replaceContent(InputStream in, Long length) throws BadRequestException, ConflictException, NotAuthorizedException {
        try {
            ByteArrayOutputStream bos = TFolderResource.readStream(in);
            this.bytes = bos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
