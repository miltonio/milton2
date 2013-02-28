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

package io.milton.http.http11;

import io.milton.http.Request;
import io.milton.resource.Resource;
import io.milton.http.Response;
import io.milton.http.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Just uses simple property values to generate error content
 *
 * @author brad
 */
public class SimpleContentGenerator implements ContentGenerator {

	private static final Logger log = LoggerFactory.getLogger(SimpleContentGenerator.class);
	private String methodNotAllowed = "<html><body><h1>Method Not Allowed</h1></body></html>";
	private String notFound = "<html><body><h1>${url} Not Found (404)</h1></body></html>";
	private String methodNotImplemented = "<html><body><h1>Method Not Implemented</h1></body></html>";
	private String conflict = "<html><body><h1>Conflict</h1></body></html>";
	private String serverError = "<html><body><h1>Server Error</h1></body></html>";
	private String unauthorised = "<html><body><h1>Not authorised</h1></body></html>";
	private String loginExternal = "<html><body><h1>Not authorised</h1><p>Please login with: ${externalProviders}</p></body></html>";
	private String unknown = "<html><body><h1>Unknown error</h1></body></html>";

	@Override
	public void generate(Resource resource, Request request, Response response, Status status) {
		String template;
		switch (status) {
			case SC_METHOD_NOT_ALLOWED:
				template = getMethodNotAllowed();
				break;
			case SC_NOT_FOUND:
				template = getNotFound();
				break;
			case SC_NOT_IMPLEMENTED:
				template = getMethodNotImplemented();
				break;
			case SC_CONFLICT:
				template = getConflict();
				break;
			case SC_INTERNAL_SERVER_ERROR:
				template = getServerError();
				break;
			case SC_UNAUTHORIZED:
				template = getUnauthorised();
				break;
			default:
				template = getUnknown();
		}
		final String finalTemplate = applyTemplates(template, request);
        response.setEntity(new Response.Entity() {
            @Override
            public void write(Response response, OutputStream outputStream) throws Exception {
				outputStream.write(finalTemplate.getBytes("UTF-8"));
                outputStream.flush();
            }
        });
	}

	private String applyTemplates(String template, Request request) {
		template = template.replace("${url}", request.getAbsolutePath());
		return template;
	}

	/**
	 * @return the methodNotAllowed
	 */
	public String getMethodNotAllowed() {
		return methodNotAllowed;
	}

	/**
	 * @param methodNotAllowed the methodNotAllowed to set
	 */
	public void setMethodNotAllowed(String methodNotAllowed) {
		this.methodNotAllowed = methodNotAllowed;
	}

	/**
	 * @return the notFound
	 */
	public String getNotFound() {
		return notFound;
	}

	/**
	 * @param notFound the notFound to set
	 */
	public void setNotFound(String notFound) {
		this.notFound = notFound;
	}

	/**
	 * @return the methodNotImplemented
	 */
	public String getMethodNotImplemented() {
		return methodNotImplemented;
	}

	/**
	 * @param methodNotImplemented the methodNotImplemented to set
	 */
	public void setMethodNotImplemented(String methodNotImplemented) {
		this.methodNotImplemented = methodNotImplemented;
	}

	/**
	 * @return the conflict
	 */
	public String getConflict() {
		return conflict;
	}

	/**
	 * @param conflict the conflict to set
	 */
	public void setConflict(String conflict) {
		this.conflict = conflict;
	}

	/**
	 * @return the serverError
	 */
	public String getServerError() {
		return serverError;
	}

	/**
	 * @param serverError the serverError to set
	 */
	public void setServerError(String serverError) {
		this.serverError = serverError;
	}

	/**
	 * @return the unauthorised
	 */
	public String getUnauthorised() {
		return unauthorised;
	}

	/**
	 * @param unauthorised the unauthorised to set
	 */
	public void setUnauthorised(String unauthorised) {
		this.unauthorised = unauthorised;
	}

	/**
	 * @return the unknown
	 */
	public String getUnknown() {
		return unknown;
	}

	/**
	 * @param unknown the unknown to set
	 */
	public void setUnknown(String unknown) {
		this.unknown = unknown;
	}
}
