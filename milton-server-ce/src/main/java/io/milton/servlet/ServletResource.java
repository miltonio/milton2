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
package io.milton.servlet;

import io.milton.common.ContentTypeUtils;
import io.milton.http.Auth;
import io.milton.http.HttpManager;
import io.milton.http.LockToken;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.Response;
import io.milton.resource.GetableResource;
import io.milton.resource.Resource;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Used to provide access to servlet resources via milton. This just wraps the
 * normal processing of the resource in a milton compatible interface
 *
 *
 * @author brad
 */
public class ServletResource implements GetableResource {

	private final String localPath;
	private final File file;
	private final String name;
	private final HttpServletRequest req;
	private final HttpServletResponse response;

	public ServletResource(File file, String localPath, HttpServletRequest req, HttpServletResponse response) {
		this.file = file;
		this.name = file.getName();
		this.localPath = localPath;
		this.req = req;
		this.response = response;
	}

	public ServletResource(String localPath, HttpServletRequest req, HttpServletResponse response) {
		this.file = null;
		this.name = localPath.substring(localPath.lastIndexOf("/"));
		this.localPath = localPath;
		this.req = req;
		this.response = response;
	}

	@Override
	public String getUniqueId() {
		return null;
	}

	public int compareTo(Resource res) {
		return this.getName().compareTo(res.getName());
	}

	@Override
	public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
		try {
			MyResponse myResponse = new MyResponse(HttpManager.response(), out);
			req.getRequestDispatcher(localPath).include(req, myResponse);
		} catch (ServletException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object authenticate(String user, String password) {
		return "ok";
	}

	@Override
	public boolean authorise(Request request, Request.Method method, Auth auth) {
		return true;
	}

	@Override
	public String getRealm() {
		return "ettrema";   //TODO
	}

	@Override
	public Date getModifiedDate() {
		if (file != null) {
			Date dt = new Date(file.lastModified());
			return dt;
		} else {
			return null;
		}
	}

	@Override
	public Long getContentLength() {
		return null;
	}

	@Override
	public String getContentType(String preferredList) {
		if (file != null) {
			return ContentTypeUtils.findContentTypes(file);
		} else {
			return ContentTypeUtils.findContentTypes(name);
		}
	}

	@Override
	public String checkRedirect(Request request) {
		return null;
	}

	@Override
	public Long getMaxAgeSeconds(Auth auth) {
		Long ll = 315360000l; // immutable
		return ll;
	}

	public LockToken getLockToken() {
		return null;
	}

	private class MyResponse extends ServletOutputStream implements HttpServletResponse {

		private final Response response;
		private final OutputStream out;

		public MyResponse(Response response, OutputStream out) {
			this.response = response;
			this.out = out;
		}

		@Override
		public void addCookie(Cookie cookie) {
			response.setCookie(ServletRequest.toBeanCookie(cookie));
		}

		@Override
		public boolean containsHeader(String name) {
			return response.getHeaders().containsKey(name);
		}

		@Override
		public String encodeURL(String url) {
			return MiltonServlet.response().encodeURL(url);
		}

		@Override
		public String encodeRedirectURL(String url) {
			return MiltonServlet.response().encodeRedirectURL(url);
		}

		@Override
		public String encodeUrl(String url) {
			return MiltonServlet.response().encodeURL(url);
		}

		@Override
		public String encodeRedirectUrl(String url) {
			return MiltonServlet.response().encodeRedirectURL(url);
		}

		@Override
		public void sendError(int sc, String msg) throws IOException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void sendError(int sc) throws IOException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void sendRedirect(String location) throws IOException {
			MiltonServlet.response().sendRedirect(location);
		}

		@Override
		public void setDateHeader(String name, long date) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void addDateHeader(String name, long date) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void setHeader(String name, String value) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void addHeader(String name, String value) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void setIntHeader(String name, int value) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void addIntHeader(String name, int value) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void setStatus(int sc) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void setStatus(int sc, String sm) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public String getCharacterEncoding() {
			return MiltonServlet.response().getCharacterEncoding();
		}

		@Override
		public ServletOutputStream getOutputStream() throws IOException {
			return this;
		}

		@Override
		public PrintWriter getWriter() throws IOException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void setContentLength(int len) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void setContentType(String type) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void setBufferSize(int size) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public int getBufferSize() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void flushBuffer() throws IOException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void resetBuffer() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean isCommitted() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void reset() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void setLocale(Locale loc) {
			MiltonServlet.response().setLocale(loc);
		}

		@Override
		public Locale getLocale() {
			return MiltonServlet.response().getLocale();
		}

		@Override
		public void write(int b) throws IOException {
			out.write(b);
		}

		@Override
		public void write(byte[] b) throws IOException {
			out.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			out.write(b, off, len);
		}

		@Override
		public String getContentType() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void setCharacterEncoding(String charset) {
			throw new UnsupportedOperationException("Not supported yet.");
		}
	}
}
