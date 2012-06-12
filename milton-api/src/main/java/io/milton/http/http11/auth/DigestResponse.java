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
