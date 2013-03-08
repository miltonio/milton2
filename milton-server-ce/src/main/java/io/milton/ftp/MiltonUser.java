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

import java.util.List;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiltonUser implements User {

    private static final Logger log = LoggerFactory.getLogger( MiltonUser.class );

    final Object user;
    final String name;
    final String domain;

    public MiltonUser( Object user, String miltonUserName, String domain) {
        super();
        if( user == null ) throw new IllegalArgumentException( "no user object provided");
        this.user = user;
        this.name = miltonUserName;
        this.domain = domain;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public List<Authority> getAuthorities() {
        return null;
    }

    /**
     *
     * @return - the security implementation specific user object returned
     * by authentication
     */
    public Object getUser() {
        return user;
    }

    /**
     * {@inheritDoc}
     */
    public List<Authority> getAuthorities( Class<? extends Authority> clazz ) {
        return null;
    }

    /**
     * Note that real authorisation is done by MiltonFtpFile
     * 
     * @param request
     * @return
     */
    public AuthorizationRequest authorize( AuthorizationRequest request ) {
        log.debug( "authorize: " + request.getClass() );
        return request;
    }

    public int getMaxIdleTime() {
        return 3600;
    }

    public boolean getEnabled() {
        return true;
    }

    public String getHomeDirectory() {
        return "/";
    }
}
