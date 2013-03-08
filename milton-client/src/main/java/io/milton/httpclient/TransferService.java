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

import io.milton.common.LogUtils;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bradm
 */
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);
    private final HttpClient client;
    private final List<ConnectionListener> connectionListeners;
    private int timeout;

    public TransferService(HttpClient client, List<ConnectionListener> connectionListeners) {
        this.client = client;
        this.connectionListeners = connectionListeners;
    }

    public synchronized void get(String url, StreamReceiver receiver, List<Range> rangeList, ProgressListener listener, HttpContext context) throws io.milton.httpclient.HttpException, Utils.CancelledException, NotAuthorizedException, BadRequestException, ConflictException, NotFoundException {
        LogUtils.trace(log, "get: ", url);
        notifyStartRequest();
        HttpRequestBase m;
        if (rangeList != null) {
            try {
                m = new RangedGetMethod(url, rangeList);
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            m = new HttpGet(url);
        }
        InputStream in = null;
        NotifyingFileInputStream nin;
        try {
            HttpResponse resp = client.execute(m, context);
            if (resp.getEntity() == null) {
                log.warn("Did not receive a response entity for GET");
                return;
            }
            HttpEntity entity = resp.getEntity();
            in = entity.getContent();
            Utils.processResultCode(resp.getStatusLine().getStatusCode(), url);
            nin = new NotifyingFileInputStream(in, entity.getContentLength(), url, listener);
            receiver.receive(nin);
        } catch (Utils.CancelledException ex) {
            m.abort();
            throw ex;
        } catch (IOException ex) {
            m.abort();
            throw new RuntimeException(ex);
        } finally {
            Utils.close(in);
            notifyFinishRequest();
        }
    }

    /**
     * Attempt to PUT a file to the server.
     *
     * Now includes an etag check. If you intend to overwrite a file then
     * include a non-null etag. This will do an if-match check on the server to
     * ensure you're not overwriting someone else's changes. If the file in new,
     * the etag given should be null, this will result in an if-none-match: *
     * check, which will fail if a file already exists
     *
     *
     *
     * @param encodedUrl
     * @param content
     * @param contentLength
     * @param contentType
     * @param etag - expected etag on the server if overwriting, or null if a
     * new file
     * @param listener
     * @param context
     * @return
     */
    public HttpResult put(String encodedUrl, InputStream content, Long contentLength, String contentType, IfMatchCheck etagMatch, ProgressListener listener, HttpContext context) {
        LogUtils.trace(log, "put: ", encodedUrl);
        notifyStartRequest();
        String s = encodedUrl;
        HttpPut p = new HttpPut(s);
        p.addHeader(Request.Header.CONTENT_TYPE.code, contentType);
        p.addHeader(Request.Header.OVERWRITE.code, "T"); // we always allow overwrites
        if (etagMatch != null) {
            if (etagMatch.getEtag() != null) {
                p.addHeader(Request.Header.IF_MATCH.code, etagMatch.getEtag());                
            } else {
                p.addHeader(Request.Header.IF_NONE_MATCH.code, "*"); // this will fail if there is a file with the same name
            }
        }

        NotifyingFileInputStream notifyingIn = null;
        try {
            notifyingIn = new NotifyingFileInputStream(content, contentLength, s, listener);
            HttpEntity requestEntity;
            if (contentLength == null) {
                throw new RuntimeException("Content length for input stream is null, you must provide a length");
            } else {
                requestEntity = new InputStreamEntity(notifyingIn, contentLength);
                requestEntity = new BufferedHttpEntity(requestEntity); // wrap in a buffering thingo to allow repeated requests
            }
            p.setEntity(requestEntity);
            return Utils.executeHttpWithResult(client, p, null, context);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            IOUtils.closeQuietly(notifyingIn);
            notifyFinishRequest();
        }
    }

    private void notifyStartRequest() {
        for (ConnectionListener l : connectionListeners) {
            l.onStartRequest();
        }
    }

    private void notifyFinishRequest() {
        for (ConnectionListener l : connectionListeners) {
            l.onFinishRequest();
        }
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
