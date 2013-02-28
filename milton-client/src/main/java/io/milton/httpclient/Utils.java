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

package io.milton.httpclient;

import io.milton.common.Path;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mcevoyb
 */
public class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    /**
     * Convert the path to a url encoded string, by url encoding each path of the
     * path. Will not be suffixed with a slash
     * 
     * @param path
     * @return 
     */
    public static String buildEncodedUrl(Path path) {
        String url = "";
        String[] arr = path.getParts();
        for (int i = 0; i < arr.length; i++) {
            String s = arr[i];
            if (i > 0) {
                url += "/";
            }
            url += io.milton.common.Utils.percentEncode(s);
        }
        return url;
    }    
    
    /**
     * Execute the given request, populate any returned content to the outputstream,
     * and return the status code
     * 
     * @param client
     * @param m
     * @param out - may be null
     * @return
     * @throws IOException 
     * 
     */
    public static int executeHttpWithStatus(HttpClient client, HttpUriRequest m, OutputStream out, HttpContext context) throws IOException {
        HttpResult result = executeHttpWithResult(client, m, out, context);
        return result.getStatusCode();
    }
    
    public static HttpResult executeHttpWithResult(HttpClient client, HttpUriRequest m, OutputStream out, HttpContext context) throws IOException {
        HttpResponse resp = client.execute(m, context);
        HttpEntity entity = resp.getEntity();
        if( entity != null ) {
            InputStream in = null;
            try {
                in = entity.getContent();
                if( out != null ) {
                    IOUtils.copy(in, out);
                }
            } finally {
                IOUtils.closeQuietly(in);
            }
        }
        Map<String,String> mapOfHeaders = new HashMap<String, String>();
        Header[] respHeaders = resp.getAllHeaders();
        for( Header h : respHeaders) {
            mapOfHeaders.put(h.getName(), h.getValue()); // TODO: should concatenate multi-valued headers
        }
        HttpResult result = new HttpResult(resp.getStatusLine().getStatusCode(), mapOfHeaders);
        return result;
    }    
    
    public static void close(InputStream in) {
        try {
            if (in == null) {
                return;
            }
            in.close();
        } catch (IOException ex) {
            log.warn("Exception closing stream: " + ex.getMessage());
        }
    }

    public static void close(OutputStream out) {
        try {
            if (out == null) {
                return;
            }
            out.close();
        } catch (IOException ex) {
            log.warn("Exception closing stream: " + ex.getMessage());
        }
    }

    public static long write(InputStream in, OutputStream out, final ProgressListener listener) throws IOException {
        long bytes = 0;
        byte[] arr = new byte[1024];
        int s = in.read(arr);
        bytes += s;
        try {
            while (s >= 0) {
                if (listener != null && listener.isCancelled()) {
                    throw new CancelledException();
                }
                out.write(arr, 0, s);
                s = in.read(arr);
                bytes += s;
                if (listener != null) {
                    listener.onProgress(bytes, null, null);
                }
            }
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            log.error("exception copying bytes", e);
            throw new RuntimeException(e);
        }
        return bytes;
    }

    /**
     * Wraps the outputstream in a bufferedoutputstream and writes to it
     *
     * the outputstream is closed and flushed before returning
     *
     * @param in
     * @param out
     * @param listener
     * @throws IOException
     */
    public static long writeBuffered(InputStream in, OutputStream out, final ProgressListener listener) throws IOException {
        BufferedOutputStream bout = null;
        try {
            bout = new BufferedOutputStream(out);
            long bytes = Utils.write(in, out, listener);
            bout.flush();
            out.flush();
            return bytes;
        } finally {
            Utils.close(bout);
            Utils.close(out);
        }

    }

    public static void processResultCode(int result, String href) throws io.milton.httpclient.HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        if (result >= 200 && result < 300) {
            return;
        } else if (result >= 300 && result < 400) {
            switch (result) {
                case 301:
                    throw new RedirectException(result, href);
                case 302:
                    throw new RedirectException(result, href);
                case 304:
                    break;
                default:
                    throw new RedirectException(result, href);
            }
        } else if (result >= 400 && result < 500) {
            switch (result) {
                case 400:
                    throw new BadRequestException(href);
                case 401:
                    throw new NotAuthorizedException(href, null);
                case 403:
                    throw new NotAuthorizedException(href, null);
                case 404:
                    throw new NotFoundException(href);
                case 405:
                    throw new MethodNotAllowedException(result, href);
                case 409:
                    throw new ConflictException(href);
                default:
                    throw new GenericHttpException(result, href);
            }
        } else if (result >= 500 && result < 600) {
            throw new InternalServerError(href, result);
        } else {
            throw new GenericHttpException(result, href);
        }

    }

    public static class CancelledException extends IOException {
    }
    
    public static String format (Map<String,String> parameters, final String encoding) {
        final StringBuilder result = new StringBuilder();
        for ( Entry<String, String> p : parameters.entrySet()) {
            final String encodedName = encode(p.getKey(), encoding);
            final String value = p.getValue();
            final String encodedValue = value != null ? encode(value, encoding) : "";
            if (result.length() > 0) {
                result.append("&");
            }
            result.append(encodedName);
            result.append("=");
            result.append(encodedValue);
        }
        return result.toString();
    }

//    private static String decode (final String content, final String encoding) {
//        try {
//            return URLDecoder.decode(content,
//                    encoding != null ? encoding : HTTP.DEFAULT_CONTENT_CHARSET);
//        } catch (UnsupportedEncodingException problem) {
//            throw new IllegalArgumentException(problem);
//        }
//    }

    private static String encode (final String content, final String encoding) {
        try {
            return URLEncoder.encode(content, encoding != null ? encoding : HTTP.DEFAULT_CONTENT_CHARSET);
        } catch (UnsupportedEncodingException problem) {
            throw new IllegalArgumentException(problem);
        }
    }    
}
