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

package io.milton.http;

import io.milton.resource.Resource;
import io.milton.http.Request.Method;
import io.milton.http.http11.auth.DigestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class ReadOnlySecurityManager implements SecurityManager{

    private Logger log = LoggerFactory.getLogger( ReadOnlySecurityManager.class );

    private final String realm;

    public ReadOnlySecurityManager( String realm ) {
        this.realm = realm;
    }

    public ReadOnlySecurityManager() {
        this.realm = null;
    }



	@Override
    public Object authenticate( String user, String password ) {
        return "ok";
    }

	@Override
    public Object authenticate( DigestResponse digestRequest ) {
        return digestRequest.getUser();
    }



	@Override
    public boolean authorise( Request request, Method method, Auth auth, Resource resource ) {
        switch(method) {
            case GET: return true;
            case HEAD: return true;
            case OPTIONS: return true;
            case PROPFIND: return true;
        }
        log.debug("denying access to method {} on {}", method, request.getAbsolutePath());
        return false;
    }

    /**
     * Will return the configured realm if it is not null. Otherwise, will return
     * the requested hostname as the realm if it is not blank, otherwise will
     * return "ReadOnlyRealm"
     *
     * @param host - the requested host name
     * @return
     */
    public String getRealm(String host) {
        if( realm != null ) {
            return realm;
        } else {
            if( host != null && host.length() > 0 ) {
                return host;
            } else {
                return "ReadOnlyRealm";
            }
        }
    }



	public boolean isDigestAllowed() {
		return true;
	}

}
