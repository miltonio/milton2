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

package io.milton.http.http11;

import io.milton.http.AuthenticationService;
import io.milton.http.Request;
import io.milton.resource.Resource;
import io.milton.http.Response;
import io.milton.http.Response.Status;
import io.milton.sso.ExternalIdentityProvider;
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
                PrintWriter pw = new PrintWriter(outputStream, true);
                pw.print(finalTemplate);
                pw.flush();
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
