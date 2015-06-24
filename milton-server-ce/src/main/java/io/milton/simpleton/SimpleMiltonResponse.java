
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

package io.milton.simpleton;


import io.milton.http.AbstractResponse;
import io.milton.http.Cookie;
import io.milton.http.Response.Status;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.simpleframework.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bradm (zfc1502)
 */
public class SimpleMiltonResponse extends AbstractResponse{
    private static final Logger log = LoggerFactory.getLogger(SimpleMiltonResponse.class);
    public final Response baseResponse;
    public final long started;

    public boolean disableClose;
    private final Map<String,String> headers = new HashMap<String, String>();

    private Status status;

    public SimpleMiltonResponse(Response baseResponse) {
        this.baseResponse = baseResponse;
        started = System.currentTimeMillis();
    }

    @Override
    public void setContentLengthHeader(Long totalLength) {
		if( totalLength != null ) {
			int i = (int) totalLength.longValue();
			baseResponse.setContentLength(i);		
		}
//        String s = totalLength==null ? null : totalLength.toString();		
//        setResponseHeader( Header.CONTENT_LENGTH,s);
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
        baseResponse.setCode(status.code);
    }

    @Override
    public void setNonStandardHeader(String code, String value) {
        headers.put( code, value);
        baseResponse.set(code, value);
    }

    @Override
    public String getNonStandardHeader(String code) {
        return baseResponse.getValue(code);
    }

    @Override
    public OutputStream getOutputStream() {
        try {
            return baseResponse.getOutputStream();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() {
        if( disableClose ) {
            return ;
        }
        try {
            baseResponse.close();
            log.debug("request completed in: " + (System.currentTimeMillis()-started));
        } catch (Exception ex) {
            log.error("exception closing response", ex);
        }
    }

    public void closeReally() {
        try {
//            log.debug("..doing close: " + baseResponse.getClass());
            baseResponse.close();
            if( log.isInfoEnabled()){
                log.info("request completed in: " + (System.currentTimeMillis()-started));
            }
        } catch (Throwable ex) {
            log.error("exception closing", ex);
        }
    }

    @Override
    public void setLocationHeader(String arg0) {
        super.setLocationHeader(arg0);
    }

	@Override
    public Map<String, String> getHeaders() {
        return headers;
    }

	@Override
    public void setAuthenticateHeader( List<String> challenges ) {
        for( String ch : challenges ) {
            baseResponse.add( Header.WWW_AUTHENTICATE.code, ch);
        }
    }


	@Override
    public Cookie setCookie( Cookie cookie ) {
        if( cookie instanceof SimpletonCookie) {
            SimpletonCookie sc = (SimpletonCookie) cookie;
            baseResponse.setCookie( sc.getWrapped());
            return cookie;
        } else {
            org.simpleframework.http.Cookie c = new org.simpleframework.http.Cookie( cookie.getName(), cookie.getValue());
            c.setDomain( cookie.getDomain());
            c.setExpiry( cookie.getExpiry());
            c.setPath( cookie.getPath());
            c.setSecure( cookie.getSecure());
            c.setVersion( cookie.getVersion());

            baseResponse.setCookie( c );
            return new SimpletonCookie( c );
        }
    }

	@Override
    public Cookie setCookie( String name, String value ) {
        org.simpleframework.http.Cookie c = baseResponse.setCookie( name, value );
        return new SimpletonCookie( c );
    }

	/**
	 * Just set the status and content, and close the connection
	 * 
	 * @param status
	 * @param message 
	 */
	@Override
	public void sendError(Status status, String message) {
		try {
			setStatus(status);
			getOutputStream().write(message.getBytes("UTF-8"));
		} catch (IOException iOException) {
			log.error("Exception sending error", iOException);
		} finally {
			closeReally();
		}
				
		
	}

}
