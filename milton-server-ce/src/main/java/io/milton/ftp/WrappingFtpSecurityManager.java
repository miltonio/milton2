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

package io.milton.ftp;

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.http11.auth.DigestResponse;
import io.milton.resource.Resource;



/**
 * Wraps a standard milton securitymanager to implement the UserService required
 * by FTP.
 *
 * Note that not all FTP methods are implemented, but enough for users to use
 * FTP.
 *
 * @author brad
 */
public class WrappingFtpSecurityManager implements io.milton.http.SecurityManager, UserService{
    private final io.milton.http.SecurityManager wrapped;

    public WrappingFtpSecurityManager( io.milton.http.SecurityManager wrapped ) {
        this.wrapped = wrapped;
    }

    /**
     * Not supported
     *
     * @param name
     */
    public void delete( String name ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    /**
     * Not supported
     *
     * @param name
     * @return
     */
    public boolean doesExist( String name ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    /**
     * Not supported
     *
     * @return
     */
    public String[] getAllUserNames() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public MiltonUser getUserByName( String name, String domain ) {
        return new MiltonUser( name, name, domain );
    }

    public void save( MiltonUser user ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Object authenticate( DigestResponse dr ) {
        return wrapped.authenticate( dr );
    }

    public Object authenticate( String string, String string1 ) {
        return wrapped.authenticate( string, string1 );
    }

    public boolean authorise( Request rqst, Method method, Auth auth, Resource rsrc ) {
        return wrapped.authorise( rqst, method, auth, rsrc );
    }

    public String getRealm( String string ) {
        return wrapped.getRealm( string );
    }

	public boolean isDigestAllowed() {
		return true;
	}

}
