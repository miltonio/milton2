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
import java.util.Collection;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author brad
 */
public class OutputStreamWrappingHttpServletResponse extends ServletOutputStream implements HttpServletResponse {

	private final HttpServletResponse response;
	private final OutputStream out;
	private final PrintWriter writer;

	public OutputStreamWrappingHttpServletResponse(HttpServletResponse response, OutputStream out) {
		this.response = response;
		this.out = out;
		writer = new PrintWriter(out);
	}

	@Override
	public void addCookie(Cookie cookie) {
		response.addCookie(cookie);
	}

	@Override
	public boolean containsHeader(String name) {
		return response.containsHeader(name);
	}

	@Override
	public String encodeURL(String url) {
		return response.encodeURL(url);
	}

	@Override
	public String encodeRedirectURL(String url) {
		return response.encodeRedirectURL(url);
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
	}

	@Override
	public void sendError(int sc) throws IOException {
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		MiltonServlet.response().sendRedirect(location);
	}

	@Override
	public void setDateHeader(String name, long date) {
	}

	@Override
	public void addDateHeader(String name, long date) {
	}

	@Override
	public void setHeader(String name, String value) {
	}

	@Override
	public void addHeader(String name, String value) {
	}

	@Override
	public void setIntHeader(String name, int value) {
	}

	@Override
	public void addIntHeader(String name, int value) {
	}

	@Override
	public void setStatus(int sc) {
	}

	@Override
	public void setStatus(int sc, String sm) {
	}

	@Override
	public String getCharacterEncoding() {
		return response.getCharacterEncoding();
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return this;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return writer;
	}

	@Override
	public void setContentLength(int len) {
	}

	@Override
	public void setContentType(String type) {
	}

	@Override
	public void setBufferSize(int size) {
		response.setBufferSize(size);
	}

	@Override
	public int getBufferSize() {
		return response.getBufferSize();
	}

	@Override
	public void flushBuffer() throws IOException {
		writer.flush();
		out.flush();
		response.flushBuffer();
	}

	@Override
	public void resetBuffer() {
	}

	@Override
	public boolean isCommitted() {
		return response.isCommitted();
	}

	@Override
	public void reset() {
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
		return response.getContentType();
	}

	@Override
	public void setCharacterEncoding(String charset) {
	}
}
