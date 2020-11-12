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

/**
 * Represents a means of writing entities to the HTTP response. For most
 * containers this is trivial, simply write to the output stream on the Responsee
 * 
 * However, some containers have an architecture where the content transmission can
 * be deferred, and this abstraction exists to support that.
 * 
 * For example, Restlet uses an API with deferred content transmission. But also
 * SEDA (http://www.eecs.harvard.edu/~mdw/proj/seda/) servers generally will want
 * to use a seperate thread pool for generating content from that which processes
 * request headers etc
 *
 * @author brad
 */
public interface EntityTransport {
	/**
	 * Transmit the response to the client
	 * 
	 * @param response
	 * @throws Exception 
	 */
    void sendResponseEntity(Response response) throws Exception;

	/**
	 * Called after sending
	 * 
	 * @param response 
	 */
    void closeResponse(Response response);
}
