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

package io.milton.http.http11.auth;

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;

/**
 * This class encapsulates all of the information from a client as a response
 * to a Digest authentication request.
 *
 * @author brad
 */
public class DigestResponse {
    private final Method method;
    private final String user;
    private final String realm;
    private final String nonce;
    private final String uri;
    private final String responseDigest;
    private final String qop;
    private final String nc;
    private final String cnonce;

    public DigestResponse( Auth auth, Request request ) {
        this.method = request.getMethod();
        user = auth.getUser();
        realm = auth.getRealm();
        nonce = auth.getNonce();
        uri = auth.getUri();
        responseDigest = auth.getResponseDigest();
        qop = auth.getQop();
        nc = auth.getNc();
        cnonce = auth.getCnonce();
    }

    public DigestResponse( Method method, String user, String realm, String nonce, String uri, String responseDigest, String qop, String nc, String cnonce ) {
        this.method = method;
        this.user = user;
        this.realm = realm;
        this.nonce = nonce;
        this.uri = uri;
        this.responseDigest = responseDigest;
        this.qop = qop;
        this.nc = nc;
        this.cnonce = cnonce;
    }



    public Method getMethod() {
        return method;
    }


    


    public String getUser() {
        return user;
    }


    public String getRealm() {
        return realm;
    }

    public String getNonce() {
        return nonce;
    }

    public String getUri() {
        return uri;
    }

    /**
     * This is the response to the challenge. It is effectively The Answer
     * from the user agent.
     *
     * Note the overloaded meanings of the word "response". This class is a response to a challenge, but is sent in a request from
     * the user agent.
     *
     * @return
     */
    public String getResponseDigest() {
        return responseDigest;
    }

    public String getQop() {
        return qop;
    }

    public String getNc() {
        return nc;
    }

    public String getCnonce() {
        return cnonce;
    }
}
