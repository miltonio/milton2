/*
 *
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.milton.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 *
 * @author brad
 */
public class OutputStreamWrappingHttpServletResponse extends HttpServletResponseWrapper {

	private final HttpServletResponse response;
	private final OutputStream out;
	private final PrintWriter writer;

	public OutputStreamWrappingHttpServletResponse(HttpServletResponse response, OutputStream out) {
		super(response);
		this.response = response;
		this.out = out;
		writer = new PrintWriter(out);
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
	public void sendRedirect(String location) throws IOException {
		MiltonServlet.response().sendRedirect(location);
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return new WrappedOutputStream();
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return writer;
	}

	@Override
	public void flushBuffer() throws IOException {
		writer.flush();
		out.flush();
		response.flushBuffer();
	}

	@Override
	public void setLocale(Locale loc) {
		MiltonServlet.response().setLocale(loc);
	}

	@Override
	public Locale getLocale() {
		return MiltonServlet.response().getLocale();
	}

	private class WrappedOutputStream extends ServletOutputStream {

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
		public boolean isReady() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void setWriteListener(WriteListener writeListener) {

		}
	}
}
