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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 *
 * @author mcevoyb
 */
public class LockMethod extends HttpEntityEnclosingRequestBase {
    private final int timeout;

    public LockMethod(String uri, int timeout) throws URISyntaxException {
        setURI(new URI(uri));
        this.timeout = timeout;
    }

    @Override
    public String getMethod() {
        return "LOCK";
    }

    public String getLockToken(HttpResponse response) {
        try {
            Document document = getResponseAsDocument(response);
            if (document == null) {
                throw new RuntimeException("Got empty response to LOCK request");
            }
            Element root = document.getRootElement();
            List<Element> lockTokenEls = RespUtils.getElements(root, "locktoken");
            for (Element el : lockTokenEls) {
                String token = RespUtils.asString(el, "href");
                if (token == null) {
                    throw new RuntimeException("No href element in locktoken");
                }
                return token;
            }
            throw new RuntimeException("Didnt find a locktoken/href element in LOCK response");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Document getResponseAsDocument(HttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return null;
        }
        InputStream in = null;
        try {
            in = entity.getContent();
//        IOUtils.copy( in, out );
//        String xml = out.toString();
            Document document = RespUtils.getJDomDocument(in);
            return document;
        } catch (JDOMException ex) {
            throw new RuntimeException(ex);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public Header[] getAllHeaders() {
        addHeader("Timeout", getTimeoutValue());
        return super.getAllHeaders();
    }

    private String getTimeoutValue() {
        if (timeout == -1) {
            return "Infinite";
        }
        return "Second-" + timeout;
    } 
}
