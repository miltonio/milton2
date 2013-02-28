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

import io.milton.http.Response;
import io.milton.common.ReadingException;
import io.milton.common.StreamUtils;
import io.milton.common.WritingException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;

public class InputStreamEntity implements Response.Entity {

    private static final Logger log = LoggerFactory.getLogger(InputStreamEntity.class);

    private InputStream inputStream;

    public InputStreamEntity(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public void write(Response response, OutputStream outputStream) throws Exception {
        try {
            StreamUtils.readTo(inputStream, outputStream);
        } catch (ReadingException ex) {
            throw new RuntimeException(ex);
        } catch (WritingException ex) {
            log.warn("exception writing, client probably closed connection", ex);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        log.trace("finished sending content");
    }
}
