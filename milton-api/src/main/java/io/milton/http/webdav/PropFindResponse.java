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

package io.milton.http.webdav;

import io.milton.http.Response;
import io.milton.http.Response.Status;
import io.milton.common.Utils;
import io.milton.http.values.ValueAndType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

public class PropFindResponse {
    private final Response.Status status; 

    private final String href;
    private Map<QName, ValueAndType> knownProperties;
    private Map<Response.Status, List<NameAndError>> errorProperties;

    public PropFindResponse(String href, Map<QName, ValueAndType> knownProperties, Map<Response.Status, List<NameAndError>> errorProperties) {
        super();
        this.href = Utils.stripServer(href);
        this.status = null;
        this.knownProperties = knownProperties;
        this.errorProperties = errorProperties;
    }

    /**
     * used in sync-collection response.
     */
    public PropFindResponse(String href, Response.Status status) {
        super();
        this.href = Utils.stripServer(href);
        this.status = status;
        this.knownProperties = Collections.emptyMap();
        this.errorProperties = Collections.emptyMap();
    }

    public String getHref() {
        return href;
    }

    public Response.Status getStatus() {
        return status;
    }

    public Map<QName, ValueAndType> getKnownProperties() {
        return knownProperties;
    }

    public Map<Status, List<NameAndError>> getErrorProperties() {
        return errorProperties;
    }
 
    /**
     * Carries the qualified name of a field in error, and an optional attribute
     * with textual information describing the error.
     *
     * This might be a validation error, for example
     *
     */
    public static class NameAndError {

        private final QName name;
        private final String error;

        public NameAndError(QName name, String error) {
            this.name = name;
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public QName getName() {
            return name;
        }
    }
}
