/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * 
 */

package io.milton.sso;

import io.milton.http.Request;
import io.milton.http.Response;
import java.io.OutputStream;
import java.util.List;
//import org.opensaml.ws.transport.http.HTTPOutTransport;
//import org.opensaml.xml.security.credential.Credential;

/**
 *
 * @author brad
 */
public class MiltonHttpOutTransport { // implements HTTPOutTransport {

	private final Request request;
	
	private final Response response;

	public MiltonHttpOutTransport(Request request, Response response) {
		this.request = request;
		this.response = response;
	}
	

//	
//	@Override
//	public void setVersion(HTTP_VERSION h) {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//
//	@Override
//	public void setHeader(String name, String value) {
//		response.setNonStandardHeader(name, value);
//	}
//
//	@Override
//	public void addParameter(String string, String string1) {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//
//	@Override
//	public void setStatusCode(int i) {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//
//	@Override
//	public void sendRedirect(String url) {
//		response.sendRedirect(url);
//	}
//
//	@Override
//	public void setAttribute(String string, Object o) {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//
//	@Override
//	public void setCharacterEncoding(String charset) {
//		
//	}
//
//	@Override
//	public OutputStream getOutgoingStream() {
//		return response.getOutputStream();
//	}
//
//	@Override
//	public Object getAttribute(String string) {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//
//	@Override
//	public String getCharacterEncoding() {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//
//	@Override
//	public Credential getLocalCredential() {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//
//	@Override
//	public Credential getPeerCredential() {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//
//	@Override
//	public boolean isAuthenticated() {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//
//	@Override
//	public void setAuthenticated(boolean bln) {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//
//	@Override
//	public boolean isConfidential() {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//
//	@Override
//	public void setConfidential(boolean bln) {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//
//	@Override
//	public boolean isIntegrityProtected() {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//
//	@Override
//	public void setIntegrityProtected(boolean bln) {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//
//	@Override
//	public String getHeaderValue(String string) {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//
//	@Override
//	public String getHTTPMethod() {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//
//	@Override
//	public int getStatusCode() {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//
//	@Override
//	public String getParameterValue(String string) {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//
//	@Override
//	public List<String> getParameterValues(String string) {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//
//	@Override
//	public HTTP_VERSION getVersion() {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//	
}
