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
import io.milton.httpclient.Utils.CancelledException;
import io.milton.common.LogUtils;
import io.milton.http.Range;
import io.milton.http.Response;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mcevoyb
 */
public class File extends Resource {

    private static final Logger log = LoggerFactory.getLogger(File.class);
    public final String contentType;
    public Long contentLength;
    private String etag;

    public File(Folder parent, PropFindResponse resp) {
        super(parent, resp);
        this.contentType = resp.getContentType();
        this.contentLength = resp.getContentLength();
        etag = resp.getEtag();
    }

    public File(Folder parent, String name, String contentType, Long contentLength, String etag) {
        super(parent, name);
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.etag = etag;
    }

    public void setContent(InputStream in, Long contentLength, ProgressListener listener) throws IOException, HttpException, NotAuthorizedException, ConflictException, BadRequestException, NotFoundException {
        String newUri = this.encodedUrl();
        log.trace("upload: " + newUri);
        HttpResult result = host().doPut(newUri, in, contentLength, contentType, new IfMatchCheck(etag), listener);
        etag = result.getHeaders().get(Response.Header.ETAG.code);
        int resultCode = result.getStatusCode();
        Utils.processResultCode(resultCode, newUri);

        this.contentLength = contentLength;
    }

    @Override
    public String toString() {
        return super.toString() + " (content type=" + this.contentType + ")";
    }

    @Override
    public java.io.File downloadTo(java.io.File destFolder, ProgressListener listener) throws FileNotFoundException, IOException, HttpException, CancelledException {
        if (!destFolder.exists()) {
            throw new FileNotFoundException(destFolder.getAbsolutePath());
        }
        java.io.File dest;
        if (destFolder.isFile()) {
            // not actually a folder
            dest = destFolder;
        } else {
            dest = new java.io.File(destFolder, name);
        }
        downloadToFile(dest, listener);
        return dest;
    }

    public void downloadToFile(java.io.File dest, ProgressListener listener) throws FileNotFoundException, HttpException, CancelledException {
        LogUtils.trace(log, "downloadToFile", this.name);
        if (listener != null) {
            listener.onProgress(0, dest.length(), this.name);
        }
        try {
            Path path = path();
            host().doGet(path, dest, listener);
        } catch (CancelledException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        if (listener != null) {
            long length = dest.length();
            listener.onProgress(length, length, this.name);
            listener.onComplete(this.name);
        }
    }

    public void download(final OutputStream out, final ProgressListener listener) throws HttpException, CancelledException {
        download(out, listener, null);
    }

    public void download(final OutputStream out, final ProgressListener listener, List<Range> rangeList) throws HttpException, CancelledException {
        if (listener != null) {
            listener.onProgress(0, null, this.name);
        }
        final long[] bytesArr = new long[1];
        try {
            host().doGet(encodedUrl(), new StreamReceiver() {

                @Override
                public void receive(InputStream in) throws IOException {
                    if (listener != null && listener.isCancelled()) {
                        throw new RuntimeException("Download cancelled");
                    }
                    try {
                        long bytes = Utils.write(in, out, listener);
                        bytesArr[0] = bytes;
                    } catch (CancelledException cancelled) {
                        throw cancelled;
                    } catch (IOException ex) {
                        throw ex;
                    }
                }
            }, rangeList, listener);
        } catch (CancelledException e) {
            throw e;
        } catch (Throwable e) {
        } finally {
            Utils.close(out);
        }
        if (listener != null) {
            long l = bytesArr[0];
            listener.onProgress(l, l, this.name);
            listener.onComplete(this.name);
        }
    }

    @Override
    public String encodedUrl() {
        return parent.encodedUrl() + encodedName(); // assume parent is correctly suffixed with a slash
    }

    public String getEtag() {
        return etag;
    }
}
