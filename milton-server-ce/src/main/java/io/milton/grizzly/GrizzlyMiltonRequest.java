//Copyright Kademi 2015
package io.milton.grizzly;

import io.milton.http.AbstractRequest;
import io.milton.http.Auth;
import io.milton.http.BeanCookie;
import io.milton.http.Cookie;
import io.milton.http.FileItem;
import io.milton.http.RequestParseException;
import io.milton.http.Response;
import io.milton.servlet.FileItemWrapper;
import io.milton.servlet.upload.MonitoredDiskFileItemFactory;
import io.milton.servlet.upload.UploadListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.glassfish.grizzly.http.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class GrizzlyMiltonRequest extends  AbstractRequest {
    
    private static final Logger log = LoggerFactory.getLogger(GrizzlyMiltonRequest.class);
    
    private static final Map<Response.ContentType, String> contentTypes = new EnumMap<Response.ContentType, String>(Response.ContentType.class);
    private static final Map<String, Response.ContentType> typeContents = new HashMap<String, Response.ContentType>();

    static {
        contentTypes.put(Response.ContentType.HTTP, Response.HTTP);
        contentTypes.put(Response.ContentType.MULTIPART, Response.MULTIPART);
        contentTypes.put(Response.ContentType.XML, Response.XML);
        for (Response.ContentType key : contentTypes.keySet()) {
            typeContents.put(contentTypes.get(key), key);
        }
    }    
    
	public static BeanCookie toBeanCookie(org.glassfish.grizzly.http.Cookie c) {
		BeanCookie bc = new BeanCookie(c.getName());
		bc.setDomain(c.getDomain());
		bc.setExpiry(c.getMaxAge());
		bc.setHttpOnly(true); // http only by default
		bc.setPath(c.getPath());
		bc.setSecure(c.isSecure());
		bc.setValue(c.getValue());
		bc.setVersion(c.getVersion());
		return bc;
	}	
	    
    
    private final Request wrapped;
    private Auth auth;
    private Map<String, String> mapOfHeaders;

    public GrizzlyMiltonRequest(Request wrapped) {
        this.wrapped = wrapped;
    }

	@Override
	public String getHostHeader() {
		String s = getRequestHeader(Header.HOST);
		if( s == null ) {
			s = "";
		}
		return s;
	}
	
	
    @Override
    public String getRequestHeader(Header header) {
        return wrapped.getHeader(header.code);
    }

    @Override
    public Map<String, String> getHeaders() {
        if( mapOfHeaders == null ) {
            mapOfHeaders = new HashMap<String, String>();
            for( String headerName : wrapped.getHeaderNames() ) {
                String s = wrapped.getHeader(headerName);
                mapOfHeaders.put(headerName, s);
            }
        }
        return mapOfHeaders;
    }

    @Override
    public String getFromAddress() {
        return wrapped.getRemoteHost();
    }

    @Override
    public Method getMethod() {
        return Method.valueOf(wrapped.getMethod().getMethodString());
    }

    @Override
    public Auth getAuthorization() {
        if (auth != null) {
            return auth;
        }
        String enc = getRequestHeader(io.milton.http.Request.Header.AUTHORIZATION);
        if (enc == null) {
			log.trace("getAuthorization: No http credentials in request headers");
            return null;
        }
        if (enc.length() == 0) {
            log.trace("getAuthorization: No http credentials in request headers; authorization header is not-null, but is empty");
            return null;
        }
        auth = new Auth(enc);
        if (log.isTraceEnabled()) {
            log.trace("creating new auth object {}", auth.getScheme());
        }
        return auth;
    }

    @Override
    public void setAuthorization(Auth auth) {
        this.auth = auth;
    }

    @Override
    public String getAbsoluteUrl() {
        return wrapped.getRequestURL().toString();
    }

    @Override
    public String getAbsolutePath() {
        return wrapped.getRequestURI();
    }
    
    

    @Override
    public InputStream getInputStream() throws IOException {
        return wrapped.getInputStream();
    }

    @Override
    public void parseRequestParameters(Map<String, String> params, Map<String, FileItem> files) throws RequestParseException {
        try {
            if (isMultiPart()) {
                log.trace("parseRequestParameters: isMultiPart");
                UploadListener listener = new UploadListener();
                MonitoredDiskFileItemFactory factory = new MonitoredDiskFileItemFactory(listener);
                ServletFileUpload upload = new ServletFileUpload(factory);
                List items = upload.parseRequest(new RequestContext() {

                    @Override
                    public String getCharacterEncoding() {
                        return wrapped.getCharacterEncoding();
                    }

                    @Override
                    public String getContentType() {
                        return wrapped.getContentType();
                    }

                    @Override
                    public int getContentLength() {
                        return wrapped.getContentLength();
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        return wrapped.getInputStream();
                    }
                });

                parseQueryString(params);

                for (Object o : items) {
                    org.apache.commons.fileupload.FileItem item = (org.apache.commons.fileupload.FileItem) o;
                    if (item.isFormField()) {
                        params.put(item.getFieldName(), item.getString());
                    } else {
                        // See http://jira.ettrema.com:8080/browse/MIL-118 - ServletRequest#parseRequestParameters overwrites multiple file uploads when using input type="file" multiple="multiple"                        
                        String itemKey = item.getFieldName();
                        if (files.containsKey(itemKey)) {
                            int count = 1;
                            while (files.containsKey(itemKey + count)) {
                                count++;
                            }
                            itemKey = itemKey + count;
                        }
                        files.put(itemKey, new FileItemWrapper(item));
                    }
                }
            } else {
                for (String nm : wrapped.getParameterNames()) {
					String[] vals = wrapped.getParameterValues(nm);
					if( vals.length == 1) {
						params.put(nm, vals[0]);
					} else {
						StringBuilder sb = new StringBuilder();
						for( String s : vals) {
							sb.append(s).append(",");
						}
						if( sb.length() > 0 ) {
							sb.deleteCharAt(sb.length()-1); // remove last comma
						}
						params.put(nm, sb.toString());
					}					
                }
            }
        } catch (FileUploadException ex) {
            throw new RequestParseException("FileUploadException", ex);
        } catch (Throwable ex) {
            throw new RequestParseException(ex.getMessage(), ex);
        }
    }

    @Override
    public Cookie getCookie(String name) {
        if (wrapped.getCookies() != null) {
            for ( org.glassfish.grizzly.http.Cookie c : wrapped.getCookies()) {
                if (c.getName().equals(name)) {
                    return toBeanCookie(c);
                }
            }
        }
        return null;
    }

    @Override
    public List<Cookie> getCookies() {
        ArrayList<Cookie> list = new ArrayList<Cookie>();
        if (wrapped.getCookies() != null) {
            for (org.glassfish.grizzly.http.Cookie c : wrapped.getCookies()) {
                list.add(toBeanCookie(c));

            }
        }
        return list;
    }

	/**
	 * Returns X-Forwarded-For if present
	 * 
	 * @return 
	 */
    @Override
    public String getRemoteAddr() {
//   if (getenv('HTTP_CLIENT_IP'))
//        $ipaddress = getenv('HTTP_CLIENT_IP');
//    else if(getenv('HTTP_X_FORWARDED_FOR'))
//        $ipaddress = getenv('HTTP_X_FORWARDED_FOR');
//    else if(getenv('HTTP_X_FORWARDED'))
//        $ipaddress = getenv('HTTP_X_FORWARDED');
//    else if(getenv('HTTP_FORWARDED_FOR'))
//        $ipaddress = getenv('HTTP_FORWARDED_FOR');
//    else if(getenv('HTTP_FORWARDED'))
//        $ipaddress = getenv('HTTP_FORWARDED');
//    else if(getenv('REMOTE_ADDR'))
//        $ipaddress = getenv('REMOTE_ADDR');
		
		String forewardFor = wrapped.getHeader("X-Forwarded-For");
		if( StringUtils.isNotBlank(forewardFor)) {
			return forewardFor;
		}
		forewardFor = wrapped.getHeader("x-forwarded-for");
		if( StringUtils.isNotBlank(forewardFor)) {
			return forewardFor;
		}		
        return wrapped.getRemoteAddr();
    }
    

    private void parseQueryString(Map<String, String> map) {
        String qs = wrapped.getQueryString();
        parseQueryString(map, qs);
    }

    public static void parseQueryString(Map<String, String> map, String qs) {
        if (qs == null) {
            return;
        }
        String[] nvs = qs.split("&");
        for (String nv : nvs) {
            String[] parts = nv.split("=");
            String key = parts[0];
            String val = null;
            if (parts.length > 1) {
                val = parts[1];
            }
            if (val != null) {
                try {
                    val = URLDecoder.decode(val, "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException(ex);
                }
            }
            map.put(key, val);
        }
    }
    
    protected boolean isMultiPart() {
        Response.ContentType ct = getRequestContentType();
        return (Response.ContentType.MULTIPART.equals(ct));
    }    
    
    protected Response.ContentType getRequestContentType() {
        String s = wrapped.getContentType();
        log.trace("request content type", s);
        if (s == null) {
            return null;
        }
        if (s.contains(Response.MULTIPART)) {
            return Response.ContentType.MULTIPART;
        }
        return typeContents.get(s);
    }    
}
