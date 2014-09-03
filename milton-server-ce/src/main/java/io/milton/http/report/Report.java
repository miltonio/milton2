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

package io.milton.http.report;

import io.milton.resource.Resource;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;

/**
 * Represents a known report type, is delegated to by the ReportHandler
 *
 * @author brad
 */
public interface Report {
    /**
     * The name of the report, as used in REPORT requests
     * 
     * @return
     */
    String getName();

    /**
     * Process the requested report body, and return a document containing the
     * response body.
     *
     * Must be a multistatus response.
     *
     * @param host 
     * @param r 
     * @param doc
     * @return the response body, usually xml
     */
    String process(String host, String path, Resource r, org.jdom.Document doc) throws BadRequestException, ConflictException, NotAuthorizedException;
}
