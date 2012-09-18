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

package io.milton.http;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.output.ByteArrayOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.milton.common.StreamUtils;

/**
 *
 */
public class DebugFilter implements Filter{

    private static final Logger log = LoggerFactory.getLogger(DebugFilter.class);

    private static int counter = 0;

    private File logDir;

    public DebugFilter() {
        logDir = new File(System.getProperty("java.io.tmpdir"));
        log.info( "DebugFilter is logging requests to: " + logDir.getAbsolutePath());
    }

    public DebugFilter( File logDir ) {
        this.logDir = logDir;
        log.info( "DebugFilter is logging requests to: " + logDir.getAbsolutePath());
    }



	@Override
    public void process(FilterChain chain, Request request, Response response) {
		log.info("process: " + request.getMethod() + " " + request.getAbsolutePath());
        DebugRequest req2 = new DebugRequest(request);
        final DebugResponse resp2 = new DebugResponse(response);
		
        resp2.setEntity(new Response.Entity() {
            @Override
            public void write(Response response, OutputStream outputStream) throws Exception {
                try {
                    outputStream.write(resp2.out.toByteArray());
                    outputStream.flush();
                } catch (IOException ex) {
                    log.error("", ex);
                }
            }
        });		
		
        chain.process(req2, resp2);
        record(req2, resp2);
		System.out.println("set response entity on: " + response.getClass());

    }

    private synchronized void record(DebugRequest req2, DebugResponse resp2) {
        counter++;
        FileOutputStream fout = null;
        try {
            File f = new File(logDir, counter + "_" + req2.getMethod() + ".req");
			log.info("Save request to: " + f.getAbsolutePath());
            fout = new FileOutputStream(f);
            req2.record(fout);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } finally {
            StreamUtils.close( fout );
        }

        try {
			File f;
			if( resp2.getStatus() != null ) {
				f = new File(logDir, counter + "_" + resp2.getStatus().code + ".resp");
			} else {
				f = new File(logDir, counter + "_UNKNOWN" + ".resp");
			}
			log.info("Save response to: " + f.getAbsolutePath());
            fout = new FileOutputStream(f);
            resp2.record(fout);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } finally {
            StreamUtils.close( fout );
        }

    }

    public class DebugResponse extends AbstractResponse {
        final Response r;
        final ByteArrayOutputStream out;
        List<String> challenges;

        public DebugResponse(Response r) {
            this.r = r;
            out = new ByteArrayOutputStream();
        }

		@Override
        public Status getStatus() {
            return r.getStatus();
        }

		@Override
        public void setStatus(Status status) {
            r.setStatus(status);
        }

		@Override
        public void setNonStandardHeader(String code, String value) {
            r.setNonStandardHeader(code, value);
        }

		@Override
        public String getNonStandardHeader(String code) {
            return r.getNonStandardHeader(code);
        }

		@Override
        public OutputStream getOutputStream() {
            return out;
        }

		@Override
		public void sendError(Status status, String message) {
			r.sendError(status, message);
		}
		
		

		@Override
        public  Map<String,String> getHeaders() {
            return r.getHeaders();
        }

        private void record(FileOutputStream fout) {
            try {
                PrintWriter writer = new PrintWriter(fout);
                if( getStatus() != null ) {
                    writer.println("HTTP/1.1 " + getStatus().code);
                }
                for (Map.Entry<String, String> header : this.getHeaders().entrySet()) {
                    writer.println(header.getKey() + ": " + header.getValue());
                }
                if( challenges != null ) {
                    for( String ch : challenges) {
                        writer.println(Response.Header.WWW_AUTHENTICATE + ": " + ch);
                    }
                }
                writer.flush();
                
                // write to console
				log.info("request---");
                log.debug( out.toString());

                fout.write(out.toByteArray());
                fout.flush();
            } catch (IOException ex) {
                log.error("",ex);
            }
        }

		@Override
        public void setAuthenticateHeader( List<String> challenges ) {
            this.challenges = challenges;
            r.setAuthenticateHeader( challenges );
        }

		@Override
        public Cookie setCookie( Cookie cookie ) {
            return r.setCookie( cookie );
        }

		@Override
        public Cookie setCookie( String name, String value ) {
            return r.setCookie( name, value );
        }

		@Override
		public void close() {
			r.close();
		}


    }

    public class DebugRequest extends AbstractRequest {
        final Request r;
        final byte[] contentBytes;
        final ByteArrayInputStream content;

        public DebugRequest(Request r) {
            this.r = r;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                StreamUtils.readTo(r.getInputStream(), out);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            this.contentBytes = out.toByteArray();
            this.content = new ByteArrayInputStream(this.contentBytes);
            log.debug(out.toString());
        }

        public Map<String, String> getHeaders() {
            return r.getHeaders();
        }

        @Override
        public String getRequestHeader(Header header) {
            return r.getRequestHeader(header);
        }

        public String getFromAddress() {
            return r.getFromAddress();
        }

        public Method getMethod() {
            return r.getMethod();
        }

        public Auth getAuthorization() {
            return r.getAuthorization();
        }

        public void setAuthorization( Auth auth ) {
            r.setAuthorization( auth );
        }



        public String getAbsoluteUrl() {
            return r.getAbsoluteUrl();
        }

        public InputStream getInputStream() throws IOException {
            return content;
        }

        public void parseRequestParameters(Map<String, String> params, Map<String, FileItem> files) throws RequestParseException {
            r.parseRequestParameters(params, files);
        }

        public void record(OutputStream out) {
            PrintWriter writer = new PrintWriter(out);
            writer.println(getMethod() + " " + getAbsolutePath() + " HTTP/1.1");
            for(Map.Entry<String,String> header : this.getHeaders().entrySet()) {
                writer.println(header.getKey() + ": " + header.getValue());
            }
            writer.flush();
            try {
                out.write(contentBytes);
            } catch (IOException ex) {
                log.error("",ex);
            }
        }

        public Cookie getCookie( String name ) {
            return r.getCookie( name );
        }

        public List<Cookie> getCookies() {
            return r.getCookies();
        }

        public String getRemoteAddr() {
            return r.getRemoteAddr();
        }

    }

}
