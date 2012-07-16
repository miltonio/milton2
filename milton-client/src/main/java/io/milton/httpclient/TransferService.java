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

package io.milton.httpclient;

import io.milton.common.LogUtils;
import io.milton.http.Range;
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
            if( resp.getEntity() == null ) {
                log.warn("Did not receive a response entity for GET");
                return ;
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

    public HttpResult put(String encodedUrl, InputStream content, Long contentLength, String contentType, ProgressListener listener, HttpContext context) {
        LogUtils.trace(log, "put: ", encodedUrl);
        notifyStartRequest();
        String s = encodedUrl;
        HttpPut p = new HttpPut(s);

        NotifyingFileInputStream notifyingIn = null;
        try {
            notifyingIn = new NotifyingFileInputStream(content, contentLength, s, listener);
            HttpEntity requestEntity;
            if (contentLength == null) {
                throw new RuntimeException("Content length for input stream is null, you must provide a length");                
            } else {
                requestEntity = new InputStreamEntity(notifyingIn, contentLength);
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
