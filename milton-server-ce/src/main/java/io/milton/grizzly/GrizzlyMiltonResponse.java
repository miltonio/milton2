//Copyright Kademi 2015

package io.milton.grizzly;

import io.milton.http.AbstractResponse;
import io.milton.http.BeanCookie;
import io.milton.http.Cookie;
import io.milton.servlet.ServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.glassfish.grizzly.http.server.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class GrizzlyMiltonResponse extends AbstractResponse {

    private static final Logger log = LoggerFactory.getLogger(GrizzlyMiltonResponse.class);
    
    private final Response r;
    private final Map<String, String> headers = new HashMap<String, String>();

    public GrizzlyMiltonResponse(Response response) {
        this.r = response;
    }
    
    
    
    @Override
    public Status getStatus() {
        return Status.fromCode(r.getStatus()); 
    }

    @Override
    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    @Override
    public void setAuthenticateHeader(List<String> challenges) {
		for (String ch : challenges) {
			r.addHeader(io.milton.http.Response.Header.WWW_AUTHENTICATE.code, ch);
		}
    }

    @Override
    public void setStatus(Status status) {
        r.setStatus(status.code);
    }

    @Override
    public void setNonStandardHeader(String code, String value) {
		r.addHeader(code, value);
		headers.put(code, value);
    }

    @Override
    public String getNonStandardHeader(String code) {
        return headers.get(code);
    }

    @Override
    public OutputStream getOutputStream() {

			return r.getOutputStream();

    }

    @Override
    public void close() {
    }

    @Override
    public void sendError(Status status, String message) {
		try {
			r.sendError(status.code, message);
        }catch(java.lang.IllegalStateException e) {
            log.error("Failed to send error, response already commited", e.getMessage());
		} catch (IOException ex) {
			log.error("Failed to send error", ex);
		}
		try {
			r.getOutputStream().close();
			log.info("Closed outputstream after sendError");
		} catch (Throwable e) {
			log.warn("Failed to close outputstream after sendError");
		}
    }


	@Override
	public Cookie setCookie(Cookie cookie) {
		String h = BeanCookie.toHeader(cookie);
		r.addHeader("Set-Cookie", h);
		return cookie;		
	}

	@Override
	public Cookie setCookie(String name, String value) {
		BeanCookie c = new BeanCookie(name);
		c.setValue(value);
		c.setPath("/");
		setCookie(c);
		return c;
	}
    
}
