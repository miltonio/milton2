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

package io.milton.http;

import java.io.OutputStream;
import java.util.Map;

/**
 * Represents an item which has been uploaded in a form POST
 *
 * @author brad
 */
public interface FileItem {

    String getContentType();

    /**
     * The name of the field which declared the file control
     *
     * @return
     */
    String getFieldName();

    /**
     * To read the uploaded file
     *
     * @return
     */
    java.io.InputStream getInputStream();

    /**
     * The name of the uploaded file
     *
     * @return
     */
    java.lang.String getName();

    /**
     * The size of the uploaded file
     *
     * @return
     */
    long getSize();

    /**
     * To allow writing to the uploaded file. Not always supported
     *
     * @return
     */
    OutputStream getOutputStream();

    /**
     * A map of headers attached to the file.
     *
     * @return
     */
    Map<String, String> getHeaders();

    default String getPath() {
        return null;
    }
}
