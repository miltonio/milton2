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
